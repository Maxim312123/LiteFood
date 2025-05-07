package com.diplomaproject.litefood.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.diplomaproject.litefood.SharedPreferencesManager;
import com.diplomaproject.litefood.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    ActivityWelcomeBinding binding;
    private Button start;
    private TextView policyNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        setupListeners();
    }

    private void initViews() {
        start = binding.start;
        policyNotice = binding.policyNotice;
    }

    private void setupListeners() {
        start.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, AuthorizationActivity.class);
            startActivity(intent);
            finish();
            SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
            sharedPreferencesManager.saveBoolean("AppliedPolicy", true);
        });

        String policyText = policyNotice.getText().toString();
        SpannableString spannableString = new SpannableString(policyText);

        int linkColor = Color.YELLOW;

        int termsOfUseStart = 48;
        int termsOfUseEnd = 71;

        ClickableSpan termsOfUseClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://doc-hosting.flycricket.io/litefood-terms-of-use/cfe0a265-56e1-45e0-b4b4-cca77ded0baf/terms"));
                startActivity(intent);

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(linkColor);
            }
        };
        spannableString.setSpan(termsOfUseClickableSpan, termsOfUseStart, termsOfUseEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int privacyPolicyStart = 74;
        int privacyPolicyEnd = policyText.length();

        ClickableSpan privacyPolicyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://doc-hosting.flycricket.io/litefood-privacy-policy/4f282f4c-f9c2-4cc9-876d-15e10045ba61/privacy"));
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(linkColor);
            }
        };
        spannableString.setSpan(privacyPolicyClickableSpan, privacyPolicyStart, privacyPolicyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        policyNotice.setText(spannableString);
        policyNotice.setMovementMethod(new LinkMovementMethod());
    }


}