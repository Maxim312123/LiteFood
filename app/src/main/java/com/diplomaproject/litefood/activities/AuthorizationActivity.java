package com.diplomaproject.litefood.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.diplomaproject.litefood.MyApplication;
import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.data.User;
import com.diplomaproject.litefood.databinding.ActivityAuthorizationBinding;
import com.diplomaproject.litefood.dialogs.SuccessLoginDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

;

public class AuthorizationActivity extends AppCompatActivity {

    private static final String TAG = "AuthorizationActivity";
    private static final String serverClientId = "737753600596-tn6uh9j8bniul8hpbjkpce0lj0pjh8r9.apps.googleusercontent.com";
    private ActivityAuthorizationBinding binding;
    private TextView tvError;
    private EditText etPhoneNumber;
    private FirebaseAuth firebaseAuth;
    private Button btnSendSms, btnSignInWithGoogle;
    private Toolbar toolbar;
    private FirebaseUser currentUser;
    private InputMethodManager inputMethodManager;
    private ProgressDialog progressDialog;
    private GoogleSignInOptions gso;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    //  private FirebaseRealtimeDatabaseRepository realtimeDatabaseRepository;

    private final ActivityResultLauncher<Intent> signInWithGoogleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account.getEmail());
                }
            } catch (ApiException apiException) {
                Log.d(TAG, Objects.requireNonNull(apiException.getMessage()));
                Toast.makeText(AuthorizationActivity.this, apiException.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setupToolbar();
        setMaskOnEditText();
        setInputChangingOfEditText();
        setupViewListeners();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.useAppLanguage();
        currentUser = firebaseAuth.getCurrentUser();
        etPhoneNumber = binding.edPhoneNumber;
        btnSendSms = binding.btnSendSms;
        btnSignInWithGoogle = binding.btnSignInWithGoogle;
        tvError = binding.tvError;
        toolbar = binding.toolbar;
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        progressDialog = new ProgressDialog(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users/");
        //  realtimeDatabaseRepository = new FirebaseRealtimeDatabaseRepository();
    }

    private GoogleSignInClient getGoogleClient() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();

        return GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        GoogleSignInClient googleClient = getGoogleClient();
        signInWithGoogleLauncher.launch(googleClient.getSignInIntent());
    }

    private void firebaseAuthWithGoogle(String idToken, String userEmail) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (currentUser != null && currentUser.isAnonymous()) {
                    deleteOldUser(this.currentUser.getUid());
                    deleteAnonymousAccount();
                }
                createNewAccount(userEmail);
                showSuccessLoginDialog();
            } else {
                Toast.makeText(AuthorizationActivity.this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showSuccessLoginDialog() {
        SuccessLoginDialog dialog = new SuccessLoginDialog();
        dialog.show(getSupportFragmentManager(), "SuccessLoginDialog");
        new Handler().postDelayed(() -> {
            openMainActivity();
            dialog.dismiss();
        }, 3000);
    }

    private void deleteAnonymousAccount() {
        currentUser.delete().addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "Cannot delete anonymous account: " + task.getException().getMessage());
            }
        });
    }

    private void openMainActivity() {
        Intent intent = new Intent(AuthorizationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void createNewAccount(String userEmail) {
        currentUser = firebaseAuth.getCurrentUser();
        String userUid = currentUser.getUid();

        User user = new User();
        user.setEmail(userEmail);

        createNewUser(userUid, user);
    }

    public void createNewUser(String id, User user) {
        databaseReference.child(id).setValue(user);
    }

    public void deleteOldUser(String id) {
        databaseReference.child(id).removeValue();
    }

    private void setupToolbar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void setMaskOnEditText() {
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("+375 (__) ___-__-__");
        FormatWatcher formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(slots));
        formatWatcher.installOn(etPhoneNumber);
    }

    private void setInputChangingOfEditText() {
        setFocusChangeListenerOnEditText();
        setTextChangeListenerOnEditText();
    }

    private void setTextChangeListenerOnEditText() {
        etPhoneNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!tvError.getText().toString().isEmpty()) {
                    tvError.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }


        });
    }

    private void setFocusChangeListenerOnEditText() {
        etPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etPhoneNumber.setBackgroundResource(R.drawable.edit_text_background);
            }
        });
    }


    private void setupViewListeners() {
        btnSendSms.setOnClickListener(this::handleSendSmsClick);
        btnSignInWithGoogle.setOnClickListener(view -> {
            signInWithGoogle();
        });
    }

    private void handleSendSmsClick(View view) {
        if (!MyApplication.isNetworkAvailable(this)) {
            Toast.makeText(AuthorizationActivity.this, "Нет доступа к сети", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etPhoneNumber.length() != 19) {
            showErrorIfInvalidPhoneNumberFormat();
            return;
        }
        if (getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        showProgressDialog();
        String phoneNumber = etPhoneNumber.getText().toString();
        String tel = phoneNumber.replaceAll("[^\\d+]", "");
        String operatorCode = tel.substring(4, 6);
        List<String> validOperatorCode = Arrays.asList("25", "29", "33", "44");

        if (!validOperatorCode.contains(operatorCode)) {
            showErrorIfInvalidOperatorCode();
            return;
        }
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(tel)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        linkWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException exception) {
                        if (exception instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(AuthorizationActivity.this, "Слишком много попыток", Toast.LENGTH_SHORT).show();
                        } else if (exception instanceof FirebaseNetworkException) {
                            Toast.makeText(AuthorizationActivity.this, "Проблемы с сетью", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AuthorizationActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        etPhoneNumber.setBackgroundResource(R.drawable.edit_text_error);
                        etPhoneNumber.clearFocus();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Intent intent = new Intent(AuthorizationActivity.this, VerificationCodeActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phone number", phoneNumber);
                        startActivity(intent);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Toast.makeText(AuthorizationActivity.this, "Время ожидания истекло", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void linkWithCredential(PhoneAuthCredential credential) {
        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(AuthorizationActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(AuthorizationActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showErrorIfInvalidPhoneNumberFormat() {
        if (etPhoneNumber.hasFocus()) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        tvError.setText(getResources().getString(R.string.invalid_phone_number));
        etPhoneNumber.setBackgroundResource(R.drawable.edit_text_error);
        etPhoneNumber.clearFocus();
    }

    private void showErrorIfInvalidOperatorCode() {
        if (etPhoneNumber.hasFocus()) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        tvError.setText(getResources().getString(R.string.invalid_operator_code));
        etPhoneNumber.setBackgroundResource(R.drawable.edit_text_error);
        etPhoneNumber.clearFocus();
        progressDialog.dismiss();
    }

    private void showProgressDialog() {
        progressDialog.setMessage("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_authorization_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.close) {
            if (currentUser == null) {
                signInAnoAnonymously();
            } else {
                onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentUser == null) {
            signInAnoAnonymously();
        } else {
            super.onBackPressed();
        }
    }

    private void signInAnoAnonymously() {
        firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    createNewUserNode(task.getResult().getUser().getUid());
                    openMainActivity();
                    finish();
                }
            }
        });
    }

    private void createNewUserNode(String userId) {
        User user = new User();
        databaseReference.child(userId).setValue(user);
    }
}

