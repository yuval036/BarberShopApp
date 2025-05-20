package com.example.barbershopapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

    private ListView lvAllAppointments;
    private Button btnSetHours, btnLogout;
    private List<String> todayAppointments = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), "Loaded admin dashboard", Toast.LENGTH_SHORT).show();
        lvAllAppointments = view.findViewById(R.id.lvTodayAppointments);
        btnSetHours = view.findViewById(R.id.btnManageSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, todayAppointments);
        lvAllAppointments.setAdapter(adapter);

        loadTodayAppointments();

        btnSetHours.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminDashboard_to_adminSettings);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminDashboard_to_login);
        });
    }

    private void loadTodayAppointments() {
        todayAppointments.clear();
        Calendar calendar = Calendar.getInstance();
        String day = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH));
        String month = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1); // חודשים מ-0
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(day)
                .child(month)
                .child(year);

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasAppointments = false;

                for (DataSnapshot hourSnapshot : snapshot.getChildren()) {
                    hasAppointments = true;

                    String hour = hourSnapshot.getKey();
                    String uid = hourSnapshot.getValue(String.class);

                    if (uid != null) {
                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .child("name");

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                                String name = nameSnapshot.getValue(String.class);
                                String display = (name != null ? name : "Unknown user") + " - " + hour;
                                todayAppointments.add(display);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    } else {
                        todayAppointments.add("Unknown user - " + hour);
                        adapter.notifyDataSetChanged();
                    }
                }

                if (!hasAppointments) {
                    todayAppointments.add("No appointments for today.");
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}