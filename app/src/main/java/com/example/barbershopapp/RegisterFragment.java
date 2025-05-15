package com.example.barbershopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {
    private EditText etUsername, etPassword, etConfPass;
    Button btnCreateAccount, btnToLogin;
    FirebaseAuth regAuth;


    public RegisterFragment() {
        // Required empty public constructor
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

        btnCreateAccount.setOnClickListener(v-> {
            String username= etUsername.getText().toString().trim();
            String password= etPassword.getText().toString().trim();
            String confPass= etConfPass.getText().toString().trim();
            String email = username + "@example.com";

            if (username.isEmpty() || password.isEmpty() || confPass.isEmpty()){
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confPass)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                regAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Intent intent= new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Registration failed."+task.getException()
                                .getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        btnToLogin.setOnClickListener(v-> {
            NavHostFragment.findNavController(RegisterFragment.this)
                    .navigate(R.id.action_register_to_login);
        });
    }
}