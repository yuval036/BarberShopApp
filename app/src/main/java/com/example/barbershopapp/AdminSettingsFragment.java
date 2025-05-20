package com.example.barbershopapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class AdminSettingsFragment extends Fragment {

    private EditText etStartHour, etEndHour;
    private Button btnSaveSettings, btnLogoutAdmin;
    private Map<String, CheckBox> dayCheckBoxes = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        etStartHour = view.findViewById(R.id.etStartHour);
        etEndHour = view.findViewById(R.id.etEndHour);
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings);
        btnLogoutAdmin = view.findViewById(R.id.btnLogoutAdmin);

        dayCheckBoxes.put("Sunday", view.findViewById(R.id.cbSunday));
        dayCheckBoxes.put("Monday", view.findViewById(R.id.cbMonday));
        dayCheckBoxes.put("Tuesday", view.findViewById(R.id.cbTuesday));
        dayCheckBoxes.put("Wednesday", view.findViewById(R.id.cbWednesday));
        dayCheckBoxes.put("Thursday", view.findViewById(R.id.cbThursday));
        dayCheckBoxes.put("Friday", view.findViewById(R.id.cbFriday));
        dayCheckBoxes.put("Saturday", view.findViewById(R.id.cbSaturday));

        btnSaveSettings.setOnClickListener(v -> saveSettingsToFirebase());

        btnLogoutAdmin.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminSettings_to_login);
        });
    }

    private void saveSettingsToFirebase() {
        String startHour = etStartHour.getText().toString().trim();
        String endHour = etEndHour.getText().toString().trim();

        if (startHour.isEmpty() || endHour.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in both hours", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("scheduleSettings");

        for (Map.Entry<String, CheckBox> entry : dayCheckBoxes.entrySet()) {
            String day = entry.getKey().toLowerCase();
            boolean isOpen = !entry.getValue().isChecked();

            Map<String, Object> daySettings = new HashMap<>();
            daySettings.put("isOpen", isOpen);

            if (isOpen) {
                daySettings.put("openHour", startHour);
                daySettings.put("closeHour", endHour);
            }

            scheduleRef.child(day).setValue(daySettings);
        }

        Toast.makeText(getContext(), "Settings saved successfully", Toast.LENGTH_SHORT).show();
    }
}