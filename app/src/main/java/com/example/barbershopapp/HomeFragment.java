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

import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private CalendarView calendarView;
    private ListView hoursListView;
    private Button btnAppointment, btnLogout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container
                , false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        calendarView = view.findViewById(R.id.calendarView);
        hoursListView = view.findViewById(R.id.lvAvailableHours);
        btnAppointment= view.findViewById(R.id.btnAppointments);
        btnLogout= view.findViewById(R.id.btnLogout);


        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.TUESDAY || dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) {
                Toast.makeText(getContext(), "The barbershop is closed on this day.", Toast.LENGTH_SHORT).show();
                hoursListView.setAdapter(null);
            } else {
                String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                AppointmentManager.getAvailableHours(date, availableHours -> {
                    ArrayAdapter<String> newAdapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_list_item_1,
                            availableHours
                    );
                    hoursListView.setAdapter(newAdapter);
                    hoursListView.setTag(date);
                });
            }
        });

        hoursListView.setOnItemClickListener((AdapterView<?> parent, View v,
                                              int position, long id) -> {
            String hour = (String) parent.getItemAtPosition(position);
            String date = (String) hoursListView.getTag();

            new AlertDialog.Builder(requireContext())
                    .setTitle("make an appointment")
                    .setMessage("Would you like to make an appointment for"
                            + date + " At " + hour + "?")
                    .setPositiveButton("set", (dialog, which) -> {
                        AppointmentManager.bookAppointment(date, hour);
                        Toast.makeText(getContext(),
                                "The appointment was successfully scheduled.",
                                Toast.LENGTH_SHORT).show();

                        AppointmentManager.getAvailableHours(date, availableHours -> {
                            ArrayAdapter<String> newAdapter = new ArrayAdapter<>(
                                    getContext(),
                                    android.R.layout.simple_list_item_1,
                                    availableHours
                            );
                            hoursListView.setAdapter(newAdapter);
                        });
                    })
                    .setNegativeButton("cancel", null)
                    .show();
        });

        btnAppointment.setOnClickListener(v-> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_to_appointments);
        });

        btnLogout.setOnClickListener(v-> {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_to_login);
        });


    }
}