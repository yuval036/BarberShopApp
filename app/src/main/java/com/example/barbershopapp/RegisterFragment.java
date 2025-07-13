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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment {
    private EditText etUsername, etPassword, etConfPass;
    Button btnCreateAccount, btnToLogin;
    FirebaseAuth regAuth;


    public RegisterFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        etUsername= view.findViewById(R.id.etUsername);
        etPassword= view.findViewById(R.id.etPassword);
        etConfPass= view.findViewById(R.id.etConfPass);
        btnCreateAccount=view.findViewById(R.id.btnCreateAcount);
        btnToLogin= view.findViewById(R.id.btnToLogin);

        regAuth= FirebaseAuth.getInstance();

        btnCreateAccount.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confPass = etConfPass.getText().toString().trim();
            String email = username + "@example.com";

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confPass)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confPass)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            regAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = regAuth.getCurrentUser();
                            String uid = user.getUid();

                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            usersRef.child(uid).child("name").setValue(username)
                                    .addOnCompleteListener(nameTask -> {
                                        if (nameTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();

                                            NavHostFragment.findNavController(RegisterFragment.this)
                                                    .navigate(R.id.action_register_to_login);
                                        } else {
                                            Toast.makeText(getContext(), "Failed to save user name", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(getContext(), "Registration failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}