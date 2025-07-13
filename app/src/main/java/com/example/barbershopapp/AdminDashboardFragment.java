package com.example.barbershopapp;

import android.app.DatePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTodayAppointmentsTitle;
    private Button btnPickDate, btnSetHours, btnLogout;
    private ListView lvTodayAppointments;
    private List<String> appointmentsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_admin_dashboard,
                container,
                false);

        tvTodayAppointmentsTitle = view.findViewById(R.id.tvTodayAppointmentsTitle);
        btnPickDate             = view.findViewById(R.id.btnPickDate);
        lvTodayAppointments     = view.findViewById(R.id.lvTodayAppointments);
        btnSetHours             = view.findViewById(R.id.btnManageSettings);
        btnLogout               = view.findViewById(R.id.btnLogout);

        adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                appointmentsList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(16f);
                bg.setColor(0xFFE0F7FA);
                row.setBackground(bg);
                row.setPadding(24, 16, 24, 16);
                TextView tv = row.findViewById(android.R.id.text1);
                tv.setTextSize(16f);
                tv.setTextColor(0xFF00796B);
                return row;
            }
        };
        lvTodayAppointments.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        updateDateAndLoad(
                today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.YEAR)
        );

        btnPickDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (DatePicker dp, int y, int m, int d) ->
                            updateDateAndLoad(d, m + 1, y),
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnSetHours.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboard_to_adminSettings)
        );

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminDashboard_to_login);
        });

        return view;
    }

    private void updateDateAndLoad(int day, int month, int year) {
        String formatted = String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d", day, month, year
        );
        tvTodayAppointmentsTitle.setText(getString(R.string.Appointments) + formatted);
        loadAppointmentsForDate(formatted);
    }

    private void loadAppointmentsForDate(String date) {
        appointmentsList.clear();
        adapter.notifyDataSetChanged();

        String[] parts = date.split("/");
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(parts[0])
                .child(parts[1])
                .child(parts[2]);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean has = false;
                for (DataSnapshot hourSnap : snapshot.getChildren()) {
                    has = true;
                    String hour = hourSnap.getKey();
                    String uid  = hourSnap.getValue(String.class);
                    if (uid != null) {
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .child("name")
                                .addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot ns) {
                                                String name = ns.getValue(String.class);
                                                appointmentsList.add(
                                                        (name != null ? name : getString(R.string.User_not_found))
                                                                + " — " + hour);
                                                adapter.notifyDataSetChanged();
                                            }
                                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                                        }
                                );
                    } else {
                        appointmentsList.add("— " + hour);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (!has) {
                    appointmentsList.add(getString(R.string.No_available_appointments));
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.Error_loading_appointments, Toast.LENGTH_SHORT).show();
            }
        });
    }
}