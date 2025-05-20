package com.example.barbershopapp;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnToRegister;
    private FirebaseAuth loginAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnToRegister = view.findViewById(R.id.btnToRegister);

        loginAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.equals("admin") && password.equals("admin123")) {
                Toast.makeText(getContext(), "Welcome, admin!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_login_to_adminDashboard);
            } else {
                String fakeEmail = username + "@example.com";
                loginAuth.signInWithEmailAndPassword(fakeEmail, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                NavHostFragment.findNavController(this)
                                        .navigate(R.id.action_login_to_home);
                            } else {
                                Toast.makeText(getContext(), "Login failed: " +
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btnToRegister.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_login_to_register);
        });
    }
}