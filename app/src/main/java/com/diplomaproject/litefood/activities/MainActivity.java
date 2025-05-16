package com.diplomaproject.litefood.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.SharedPreferencesManager;
import com.diplomaproject.litefood.UserViewModel;
import com.diplomaproject.litefood.data.User;
import com.diplomaproject.litefood.databinding.ActivityMainBinding;
import com.diplomaproject.litefood.fragments.AddressFragment;
import com.diplomaproject.litefood.fragments.AnonymousProfileFragment;
import com.diplomaproject.litefood.fragments.AuthorizedProfileFragment;
import com.diplomaproject.litefood.fragments.CartFragment;
import com.diplomaproject.litefood.fragments.FoodCategoryFragment;
import com.diplomaproject.litefood.fragments.MainFragment;
import com.diplomaproject.litefood.interfaces.BottomNavigationViewSelectedItem;
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements
        BottomNavigationViewSelectedItem {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private SharedPreferencesManager sharedPreferencesManager;
    private FirebaseAuth firebaseAuth;
    @SuppressLint("RestrictedApi")
    private BottomNavigationView bottomNavigationItemView;
    private MaterialToolbar toolbar;
    private MainFragment mainFragment;
    private FirebaseUser firebaseCurrentUser;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private UserViewModel userViewModel;
    private FirebaseRealtimeDatabaseRepository realtimeDatabaseRepository;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setLanguage(sharedPreferences);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setupMainFragment();
        setupListeners();
       // checkPolicyAndAuthenticateUser();
        if (firebaseCurrentUser != null && !firebaseCurrentUser.isAnonymous()) {
            readUserData();
        }
        setupBottomNavigationView();
    }


    private void setLanguage(SharedPreferences sharedPreferences) {
        String language = sharedPreferences.getString("list", "ru");
        setLocale(language);
    }

    private void init() {
        toolbar = (MaterialToolbar) binding.toolbar;
        setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        bottomNavigationItemView = binding.bottomNavigationView;
        firebaseCurrentUser = firebaseAuth.getCurrentUser();
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;
        // realtimeDatabaseRepository = new FirebaseRealtimeDatabaseRepository();
    }

    private void setupListeners() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.paymentMethods) {
                Intent intent = new Intent(MainActivity.this, PaymentMethodsActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.favorites) {
                Intent intent = new Intent(MainActivity.this, FavoriteProductActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.addresses) {
                Fragment addressFragment = new AddressFragment();
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container, addressFragment
                ).addToBackStack("AddressFragment").commit();
            } else if (itemId == R.id.settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                View headerView = navigationView.getHeaderView(0);
                TextView userName = headerView.findViewById(R.id.user_name);
                TextView phoneNumber = headerView.findViewById(R.id.phone_number);
                userViewModel.getUser().observe(MainActivity.this, user -> {
                    String name = user.getName();
                    String userPhoneNumber = user.getPhoneNumber();

                    if (name != null) {
                        userName.setText(name);
                    } else {
                        userName.setText(getResources().getString(R.string.name));
                    }
                    if (userPhoneNumber != null) {
                        phoneNumber.setText(userPhoneNumber);
                    } else {
                        phoneNumber.setText(getResources().getString(R.string.phone_number));
                    }
                });
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });
    }


    private void checkPolicyAndAuthenticateUser() {
        if (isAppliedPolicy()) {
            if (firebaseCurrentUser == null) {
                firebaseAuthSignInAnonymously();
            }
        } else if (!isAppliedPolicy()) {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
        }
    }

    private boolean isAppliedPolicy() {
        boolean policy = sharedPreferencesManager.getBoolean("AppliedPolicy", false);
        return policy;
    }

    private void readUserData() {
        userViewModel.getUser().observe(this, user -> {
            this.user = user;
        });
    }

    private void firebaseAuthSignInAnonymously() {
        firebaseAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = task.getResult().getUser();
                createNewUserNode(currentUser.getUid());
            }
        });
    }

    private void createNewUserNode(String userId) {
        User user = new User();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + userId);
        databaseReference.setValue(user);
    }

    private void setupMainFragment() {
        mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainFragment)
                .commit();
    }

    private void setupBottomNavigationView() {
        bottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                int selectedItemId = item.getItemId();

                Fragment selectedFragment = null;

                if (selectedItemId == R.id.main) {
                    selectedFragment = new MainFragment();
                } else if (selectedItemId == R.id.categories) {
                    selectedFragment = new FoodCategoryFragment();
                } else if (selectedItemId == R.id.basket) {
                    selectedFragment = new CartFragment();
                } else if (selectedItemId == R.id.profile) {
                    if (currentUser.isAnonymous()) {
                        selectedFragment = new AnonymousProfileFragment();
                    } else if (!currentUser.isAnonymous()) {
                        selectedFragment = new AuthorizedProfileFragment();
                    }
                }

                if (selectedFragment != null) {
                    fragmentTransaction.replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Configuration config = getResources().getConfiguration();
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        } else {
            Configuration config = getResources().getConfiguration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }


    @Override
    public void changeItemState(int itemId) {
        bottomNavigationItemView.setSelectedItemId(itemId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void toggleBottomNavigationViewVisibility(Boolean isVisible) {
        int visibility = (isVisible) ? View.VISIBLE : View.GONE;
        bottomNavigationItemView.setVisibility(visibility);
    }
}