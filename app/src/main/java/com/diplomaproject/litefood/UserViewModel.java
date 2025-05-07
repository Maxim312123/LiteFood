package com.diplomaproject.litefood;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.diplomaproject.litefood.data.CartProduct;
import com.diplomaproject.litefood.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class UserViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData;

    public LiveData<User> getUser() {
        if (userLiveData == null) {
            userLiveData = new MutableLiveData<>();
            loadUserData();
        }
        return userLiveData;
    }


    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userLiveData.setValue(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }

    public HashMap<String, CartProduct> getCurrentUser() {
        return userLiveData.getValue().getBasket();
    }


}
