package com.diplomaproject.litefood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diplomaproject.litefood.R;
import com.diplomaproject.litefood.activities.AuthorizationActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnonymousProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnonymousProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button login;

    private String mParam1;
    private String mParam2;

    public AnonymousProfileFragment() {
        // Required empty public constructor
    }
    public static AnonymousProfileFragment newInstance(String param1, String param2) {
        AnonymousProfileFragment fragment = new AnonymousProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anonymous_profile, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        login=view.findViewById(R.id.login);
        login.setOnClickListener(v -> {
            Intent intent=new Intent(getActivity(), AuthorizationActivity.class);
            startActivity(intent);
        });
    }
}