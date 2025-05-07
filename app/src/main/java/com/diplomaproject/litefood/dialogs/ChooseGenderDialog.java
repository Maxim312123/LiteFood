package com.diplomaproject.litefood.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.databinding.DialogChooseGenderBinding;

public class ChooseGenderDialog extends DialogFragment {

    public interface OnGenderSelectedListener {
        void onGenderSelected(String gender);
    }

    private DialogChooseGenderBinding binding;
    private OnGenderSelectedListener listener;
    private TextView tvMaleGender, tvFemaleGender;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogChooseGenderBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_LiteFood_ChooseGenderDialog);
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        init();
        setListeners();
        return dialog;
    }

    private void init() {
        tvMaleGender = binding.tvMaleGender;
        tvFemaleGender = binding.tvFemaleGender;
    }

    private void setListeners() {
        tvMaleGender.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenderSelected("Мужской");
            }
            dismiss();
        });
        tvFemaleGender.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenderSelected("Женский");
            }
            dismiss();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnGenderSelectedListener) context;
    }

}
