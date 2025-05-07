package com.diplomaproject.litefood.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.diplomaproject.litefood.MyApplication;
import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.databinding.ActivityAuthorizationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class AuthorizationActivity extends AppCompatActivity {

    private ActivityAuthorizationBinding binding;
    private TextView tvError;
    private EditText etPhoneNumber;
    private FirebaseAuth firebaseAuth;
    private Button btnSendSms;
    private Toolbar toolbar;
    private FirebaseUser currentUser;
    private InputMethodManager inputMethodManager;
    private ProgressDialog progressDialog;

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
        currentUser = firebaseAuth.getCurrentUser();
        etPhoneNumber = binding.edPhoneNumber;
        btnSendSms = binding.btnSendSms;
        tvError = binding.tvError;
        toolbar = binding.toolbar;
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        progressDialog = new ProgressDialog(this);
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
        String phone = etPhoneNumber.getText().toString();
        String tel = phone.replaceAll("[^\\d+]", "");
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
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(AuthorizationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        etPhoneNumber.setBackgroundResource(R.drawable.edit_text_error);
                        etPhoneNumber.clearFocus();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Intent intent = new Intent(AuthorizationActivity.this, VerificationCodeActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phone_number", phone);
                        startActivity(intent);
                        progressDialog.dismiss();
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
                    Intent intent = new Intent(AuthorizationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}

