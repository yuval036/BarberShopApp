package com.example.barbershopapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
public class AppointmentManager {

    public static void bookAppointment(String date, String hour) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String[] parts = date.split("/");
        if (parts.length != 3) return;
        String day = parts[0];
        String month = parts[1];
        String year = parts[2];

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(day)
                .child(month)
                .child(year)
                .child(hour);

        ref.setValue(uid);
    }

    public static void cancelAppointment(String date, String hour) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(date)
                .child(hour);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object value = snapshot.getValue();

                if (value instanceof String) {
                    String bookedUid = (String) value;
                    if (uid.equals(bookedUid)) {
                        ref.removeValue();
                    }
                } else if (value instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) value;
                    Object uidValue = map.get("uid");
                    if (uidValue instanceof String && uid.equals(uidValue)) {
                        ref.removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    public interface AppointmentsCallback {
        void onResult(List<Map.Entry<String, String>> appointments);
    }

    public static void getUserAppointments(AppointmentsCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointments");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Map.Entry<String, String>> list = new ArrayList<>();

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    for (DataSnapshot hourSnapshot : dateSnapshot.getChildren()) {
                        String hour = hourSnapshot.getKey();
                        Object value = hourSnapshot.getValue();

                        if (value instanceof String) {
                            String bookedUid = (String) value;
                            if (uid.equals(bookedUid)) {
                                list.add(new AbstractMap.SimpleEntry<>(date, hour));
                            }
                        } else if (value instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) value;
                            Object uidValue = map.get("uid");
                            if (uidValue instanceof String && uid.equals(uidValue)) {
                                list.add(new AbstractMap.SimpleEntry<>(date, hour));
                            }
                        }
                    }
                }

                callback.onResult(list);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onResult(Collections.emptyList());
            }
        });
    }

    public interface AvailableHoursCallback {
        void onResult(List<String> availableHours);
    }

    public static void getAvailableHours(String date, AvailableHoursCallback callback) {
        List<String> allHours = Arrays.asList("9:00", "10:00", "11:00", "12:00",
                "13:00", "14:00", "15:00", "16:00");

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .child(date);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> available = new ArrayList<>(allHours);

                for (DataSnapshot child : snapshot.getChildren()) {
                    String takenHour = child.getKey();
                    available.remove(takenHour);
                }

                callback.onResult(available);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onResult(allHours);
            }
        });
    }
}
