package com.example.barbershopapp;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

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

public class HomeFragment extends Fragment {

    private CalendarView calendarView;
    private ListView hoursListView;
    private Button btnAppointment, btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        calendarView = view.findViewById(R.id.calendarView);
        hoursListView = view.findViewById(R.id.lvAvailableHours);
        btnAppointment = view.findViewById(R.id.btnAppointments);
        btnLogout = view.findViewById(R.id.btnLogout);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
            String dayName = getDayName(dayOfWeek); // למשל "monday"

            DatabaseReference settingsRef = FirebaseDatabase.getInstance()
                    .getReference("scheduleSettings")
                    .child(dayName);

            settingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot snapshot) {
                    Boolean isOpen = snapshot.child("isOpen").getValue(Boolean.class);

                    if (isOpen == null || !isOpen) {
                        Toast.makeText(getContext(), "The barbershop is closed on this day.", Toast.LENGTH_SHORT).show();
                        hoursListView.setAdapter(null);
                        return;
                    }

                    String openHourStr = snapshot.child("openHour").getValue(String.class);
                    String closeHourStr = snapshot.child("closeHour").getValue(String.class);

                    if (openHourStr == null || closeHourStr == null) {
                        Toast.makeText(getContext(), "Missing opening hours.", Toast.LENGTH_SHORT).show();
                        hoursListView.setAdapter(null);
                        return;
                    }

                    int openHour = Integer.parseInt(openHourStr.split(":")[0]);
                    int closeHour = Integer.parseInt(closeHourStr.split(":")[0]);

                    List<String> allHours = new ArrayList<>();
                    for (int hour = openHour; hour < closeHour; hour++) {
                        allHours.add(String.format("%02d:00", hour));
                    }

                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    AppointmentManager.getAvailableHours(date, availableHours -> {
                        List<String> filtered = new ArrayList<>();
                        for (String hour : allHours) {
                            if (availableHours.contains(hour)) {
                                filtered.add(hour);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                filtered
                        );
                        hoursListView.setAdapter(adapter);
                        hoursListView.setTag(date);
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load schedule.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        hoursListView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            String hour = (String) parent.getItemAtPosition(position);
            String date = (String) hoursListView.getTag();

            new AlertDialog.Builder(requireContext())
                    .setTitle("Make an appointment")
                    .setMessage("Would you like to make an appointment for " + date + " at " + hour + "?")
                    .setPositiveButton("Set", (dialog, which) -> {
                        AppointmentManager.bookAppointment(date, hour);
                        Toast.makeText(getContext(), "The appointment was successfully scheduled.", Toast.LENGTH_SHORT).show();

                        AppointmentManager.getAvailableHours(date, updatedHours -> {
                            ArrayAdapter<String> newAdapter = new ArrayAdapter<>(
                                    getContext(),
                                    android.R.layout.simple_list_item_1,
                                    updatedHours
                            );
                            hoursListView.setAdapter(newAdapter);
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnAppointment.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_to_appointments);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_to_login);
        });
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "sunday";
            case Calendar.MONDAY: return "monday";
            case Calendar.TUESDAY: return "tuesday";
            case Calendar.WEDNESDAY: return "wednesday";
            case Calendar.THURSDAY: return "thursday";
            case Calendar.FRIDAY: return "friday";
            case Calendar.SATURDAY: return "saturday";
        }
        return "";
    }
}