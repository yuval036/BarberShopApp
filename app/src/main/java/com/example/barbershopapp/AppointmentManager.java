package com.example.barbershopapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
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
        String[] parts = date.split("/");
        if (parts.length != 3) {
            callback.onResult(Collections.emptyList());
            return;
        }

        String day = parts[0];
        String month = parts[1];
        String year = parts[2];

        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
        String dayName = getDayName(calendar.get(Calendar.DAY_OF_WEEK)); // למשל: "monday"

        DatabaseReference scheduleRef = FirebaseDatabase.getInstance()
                .getReference("scheduleSettings")
                .child(dayName);

        scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean isOpen = snapshot.child("isOpen").getValue(Boolean.class);

                if (isOpen == null || !isOpen) {
                    callback.onResult(Collections.emptyList()); // יום סגור
                    return;
                }

                String openHourStr = snapshot.child("openHour").getValue(String.class);
                String closeHourStr = snapshot.child("closeHour").getValue(String.class);

                if (openHourStr == null || closeHourStr == null) {
                    callback.onResult(Collections.emptyList());
                    return;
                }

                int openHour = Integer.parseInt(openHourStr.split(":")[0]);
                int closeHour = Integer.parseInt(closeHourStr.split(":")[0]);

                List<String> allHours = new ArrayList<>();
                for (int hour = openHour; hour < closeHour; hour++) {
                    allHours.add(String.format("%02d:00", hour));
                }

                DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                        .getReference("appointments")
                        .child(day)
                        .child(month)
                        .child(year);

                appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onResult(Collections.emptyList());
            }
        });
    }

    private static String getDayName(int dayOfWeek) {
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
