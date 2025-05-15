package com.example.barbershopapp;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AppointmentsFragment extends Fragment {

    private ListView lvAppointments;
    private Button btnHome;
    private List<Map.Entry<String,String>> userAppointments= new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        lvAppointments= view.findViewById(R.id.lvAppointments);
        btnHome=view.findViewById(R.id.btnHome);

        loadAppointments();

        btnHome.setOnClickListener(v-> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_appointments_to_home);
        });
    }

    private void loadAppointments() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointments");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userAppointments.clear();

                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    String day = daySnapshot.getKey();

                    for (DataSnapshot monthSnapshot : daySnapshot.getChildren()) {
                        String month = monthSnapshot.getKey();

                        for (DataSnapshot yearSnapshot : monthSnapshot.getChildren()) {
                            String year = yearSnapshot.getKey();

                            for (DataSnapshot hourSnapshot : yearSnapshot.getChildren()) {
                                String hour = hourSnapshot.getKey();
                                Object value = hourSnapshot.getValue();

                                if (value instanceof String) {
                                    String bookedUid = (String) value;
                                    if (uid.equals(bookedUid)) {
                                        String formattedDate = String.format("%02d/%02d/%04d",
                                                Integer.parseInt(day),
                                                Integer.parseInt(month),
                                                Integer.parseInt(year));
                                        userAppointments.add(new AbstractMap.SimpleEntry<>(formattedDate, hour));
                                    }
                                }
                            }
                        }
                    }
                }

                if (userAppointments.isEmpty()) {
                    Toast.makeText(getContext(), "No appointments found", Toast.LENGTH_SHORT).show();
                }

                lvAppointments.setAdapter(new AppointmentsAdapter());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AppointmentsAdapter extends BaseAdapter{

        @Override
        public int getCount(){
            return userAppointments.size();
        }

        @Override
        public Object getItem(int position){
            return userAppointments.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row= getLayoutInflater().inflate(R.layout.appointment_item, parent, false);
            TextView tvInfo= row.findViewById(R.id.tvAppointmentInfo);
            Button btnCancel= row.findViewById(R.id.btnCancel);

            Map.Entry<String, String> appointment= userAppointments.get(position);
            String date= appointment.getKey();
            String hour= appointment.getValue();

            tvInfo.setText((date+"-"+hour));

            btnCancel.setOnClickListener(v-> {
                new AlertDialog.Builder(requireContext()).setTitle("Cancel the appointment")
                        .setMessage("Are you sure you want to cancel the appointment on"+date+ "at"+ hour+"?")
                        .setPositiveButton("Yes", (dialog,wich)->{
                            cancelAppointment(date, hour);
                        })
                        .setNegativeButton("No", null).show();
            });
            return row;
        }
    }

    private void cancelAppointment(String date, String hour) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String[] parts = date.split("/");
        String day = parts[0];
        String month = parts[1];
        String year = parts[2];

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(day)
                .child(month)
                .child(year)
                .child(hour);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String bookedUid = snapshot.getValue(String.class);
                if (uid.equals(bookedUid)) {
                    ref.removeValue();
                    Toast.makeText(getContext(), "The appointment has been canceled.", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Error cancelling appointment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}