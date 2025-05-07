package com.diplomaproject.litefood.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.data.User;
import com.diplomaproject.litefood.databinding.ActivityVerificationCodeBinding;
import com.diplomaproject.litefood.dialogs.SuccessLoginDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class VerificationCodeActivity extends AppCompatActivity {

    private ActivityVerificationCodeBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private Button btnSubmit;
    private Toolbar toolbar;
    private EditText etVerificationCode;
    private InputMethodManager inputMethodManager;
    private ObjectAnimator animator;
    private FormatWatcher formatWatcher;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setupToolbar();
        setMaskOnEditText();
        setupListeners();
        setTextChangeListenerOnEditText();

        etVerificationCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(etVerificationCode);
                confirmSmsVerificationCode();
                return true;
            }
            return false;
        });
    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        btnSubmit = binding.btnSubmit;
        etVerificationCode = binding.etVerificationCode;
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
    }

    private void setTextChangeListenerOnEditText() {
        etVerificationCode.addTextChangedListener(new TextWatcher() {
            String textBefore = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textBefore = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    etVerificationCode.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textBefore.length() > s.length()) {
                    if (s.length() == 3) {
                        s.replace(1, 3, "");
                    }
                }
            }
        });
    }

    private void setMaskOnEditText() {
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("_  _  _  _  _  _");
        formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(slots));
        formatWatcher.installOn(etVerificationCode);
    }

    private void setupToolbar() {
        toolbar = binding.toolbar;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void confirmSmsVerificationCode() {
        String verificationId = getIntent().getStringExtra("verificationId");
        String enteredCode = etVerificationCode.getText().toString();
        String modifiedEnteredCode = enteredCode.replaceAll("\\D+", "");
        if (enteredCode.length() == 16) {
            btnSubmit.setClickable(false);
            assert verificationId != null;
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, modifiedEnteredCode);
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String phoneNumber = getIntent().getStringExtra("phone_number");
                            deleteAnonymousAccount();
                            databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {

                                    } else {
                                        createNewAccount(phoneNumber);
                                        createUserBasket();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                            showSuccessfulLoginDialog();
                        } else {
                            btnSubmit.setClickable(true);
                            showInvalidVerificationCodeError();
                        }
                    });
        } else {
            if (enteredCode.isEmpty()) {
                showNotEnteredVerificationCodeError();
            } else {
                showInvalidVerificationCodeError();
            }
        }
    }

    private void createNewAccount(String phoneNumber) {
        FirebaseUser newUser = firebaseAuth.getCurrentUser();
        String id = newUser.getUid();
        User user = new User(phoneNumber);
        databaseReference.child(id).setValue(user);
    }

    private void createUserBasket(){
        FirebaseUser newUser = firebaseAuth.getCurrentUser();
        String id = newUser.getUid();
        databaseReference.child(id).child("user_basket").setValue(null);
    }


    private void deleteAnonymousAccount() {
        if (currentUser != null && currentUser.isAnonymous()) {
            currentUser.delete().addOnCompleteListener(this, task1 -> {
                if (task1.isSuccessful()) {

                } else {
                    Toast.makeText(this, "Не удалось удалить анонимную запись",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showSuccessfulLoginDialog() {
        SuccessLoginDialog dialog = new SuccessLoginDialog();
        dialog.show(getSupportFragmentManager(), "SuccessLoginDialog");
        if (getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(VerificationCodeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            dialog.dismiss();
        }, 3000);
    }


    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            confirmSmsVerificationCode();
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                view.clearFocus();
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                float x = event.getRawX() + view.getLeft() - location[0];
                float y = event.getRawY() + view.getTop() - location[1];
                if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void showInvalidVerificationCodeAnimation() {
        animator = ObjectAnimator.ofFloat(etVerificationCode, "translationY", 0f, -2.5f, 2.5f, 0f);
        animator.setDuration(300);
        animator.setInterpolator(new BounceInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();
    }

    private void showNotEnteredVerificationCodeError() {
        btnSubmit.setClickable(false);
        etVerificationCode.setBackgroundResource(R.drawable.edit_text_error);
        showInvalidVerificationCodeAnimation();

        new Handler().postDelayed(() -> {
            etVerificationCode.setBackgroundResource(R.drawable.edit_text_background);
            animator.cancel();
            btnSubmit.setClickable(true);
        }, 600);
    }

    @SuppressLint("ResourceAsColor")
    private void showInvalidVerificationCodeError() {
        btnSubmit.setClickable(false);
        formatWatcher.removeFromTextView();
        String crosses = "×  ×  ×  ×  ×  ×";
        etVerificationCode.setText(crosses);
        btnSubmit.setBackgroundResource(R.drawable.btn_submitting_verification_code);
        if (getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        etVerificationCode.setBackgroundResource(R.drawable.edit_text_error);
        etVerificationCode.clearFocus();
        showInvalidVerificationCodeAnimation();

        new Handler().postDelayed(() -> {
            etVerificationCode.setBackgroundResource(R.drawable.edit_text_background);
            btnSubmit.setBackgroundResource(R.drawable.shape_btn_rounded);
            etVerificationCode.setText("");
            animator.cancel();
            btnSubmit.setClickable(true);
            formatWatcher.installOn(etVerificationCode);
        }, 600);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, AuthorizationActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}



