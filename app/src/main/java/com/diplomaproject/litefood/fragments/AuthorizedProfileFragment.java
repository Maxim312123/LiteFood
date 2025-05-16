package com.diplomaproject.litefood.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.diplomaproject.litefood.FirebaseService;
import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.UserViewModel;
import com.diplomaproject.litefood.activities.ProfileEditingActivity;
import com.diplomaproject.litefood.data.User;
import com.diplomaproject.litefood.databinding.FragmentAuthorizedProfileBinding;
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;


public class AuthorizedProfileFragment extends Fragment implements MenuProvider {

    private FragmentAuthorizedProfileBinding binding;
    private EditText etPhoneNumber, etDateOfBirth, etGender, etEmail, etPaymentMethod;
    private TextView tvName;
    private UserViewModel userViewModel;
    private MaterialToolbar toolbar;
    private FirebaseRealtimeDatabaseRepository realtimeDatabaseRepository;
    private FirebaseAuth firebaseAuth = FirebaseService.INSTANCE.getAuth();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuthorizedProfileBinding.inflate(inflater, container, false);
        init();
        bindUserDataToUI();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner());
        setupToolbar();
    }

    private void setupToolbar() {
        toolbar = binding.materialToolbar;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }


    private void bindUserDataToUI() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            tvName.setText(user.getName() != null ? user.getName() : getString(R.string.authorized_profile_name_not_added));
            etPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : getString(R.string.authorized_profile_phone_number_not_added));
            etDateOfBirth.setText(user.getDateOfBirth() != null ? user.getDateOfBirth() : getString(R.string.authorized_profile_date_of_birth_not_added));
            etGender.setText(user.getGender() != null ? user.getGender() : getString(R.string.authorized_profile_gender_not_added));
            etEmail.setText(user.getEmail() != null ? user.getEmail() : getString(R.string.authorized_profile_email_not_added));
            if (user.getPaymentMethod() != null) {
                HashMap<String, Object> mainCard = user.getPaymentMethod().get("main");
                String last4Digits = (String) mainCard.get("last4Digits");
                String cardBrand = (String) mainCard.get("cardBrand");

                etPaymentMethod.setText("**** " + last4Digits);
                if (cardBrand.equals("Visa")) {
                    etPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.visa24), null, null, null);
                } else if (cardBrand.equals("MasterCard")) {
                    etPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.mastercard24), null, null, null);
                }
            } else {
                etPaymentMethod.setText(getString(R.string.authorized_profile_payment_method_not_added));
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void init() {
        tvName = binding.name;
        etPhoneNumber = binding.etPhoneNumber;
        etDateOfBirth = binding.etDateOfBirth;
        etGender = binding.etGender;
        etEmail = binding.etEmail;
        etPaymentMethod = binding.etPaymentMethod;
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        realtimeDatabaseRepository = FirebaseService.INSTANCE.getRealtimeDatabaseRepository();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.toolbar_authorized_profile_fragment, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.exit) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Подтверждение")
                    .setMessage("Вы действительно хотите выйти из учетной записи?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (!currentUser.isAnonymous()) {
                                firebaseAuth.signOut();

                                firebaseAuth.signInAnonymously()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                realtimeDatabaseRepository.createNewUser(firebaseAuth.getCurrentUser().getUid(), new User());
                                                getFragmentManager().beginTransaction()
                                                        .replace(R.id.fragment_container, new AnonymousProfileFragment())
                                                        .commit();
                                            } else {
                                                Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        if (item.getItemId() == R.id.delete_account) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Подтверждение")
                    .setMessage("Вы действительно хотите удалить текущий аккаунт?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (!currentUser.isAnonymous()) {
                                realtimeDatabaseRepository.deleteOldUser(currentUser.getUid());
                                currentUser.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().signInAnonymously()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        String newUserId = firebaseAuth.getCurrentUser().getUid();
                                                        realtimeDatabaseRepository.createNewUser(newUserId, new User());
                                                        getFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_container, new AnonymousProfileFragment())
                                                                .commit();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        Toast.makeText(getActivity(), "Учетная запись удалена", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d("Error", task.getException().getMessage());
                                    }
                                });
                            }
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if (item.getItemId() == R.id.edit_profile) {
            Intent intent = new Intent(getActivity(), ProfileEditingActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
