package com.example.resqher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_CALL_PERMISSION = 3;

    private LocationManager locationManager;
    private ImageView profileIcon;
    private Button btnSOS, btnSafety, btnAlert, btnLogout;
    private DatabaseReference databaseReference;
    private String contact1 = "", contact2 = "";
    private final String emergencyText = "I am in danger! My current location is: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profileIcon = findViewById(R.id.profileIcon);
        btnSOS = findViewById(R.id.btnsos);
        btnSafety = findViewById(R.id.btnSafety);
        btnAlert = findViewById(R.id.btnAlert);
        btnLogout = findViewById(R.id.btnLogout);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("Profile");

        checkSmsPermission();
        checkCallPermission();
        loadEmergencyContacts();

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnSOS.setOnClickListener(v -> sendEmergencySms());

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnSafety.setOnClickListener(v -> navigateTo(SafetyActivity.class));
        btnAlert.setOnClickListener(v -> navigateTo(AlertActivity.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(HomeActivity.this, targetActivity);
        startActivity(intent);
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        }
    }

    private void checkCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    private void loadEmergencyContacts() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    contact1 = snapshot.child("emergencyContact1").getValue(String.class);
                    contact2 = snapshot.child("emergencyContact2").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmergencySms() {
        if (contact1.isEmpty() && contact2.isEmpty()) {
            Toast.makeText(this, "No emergency contacts found!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String locationUrl = "http://maps.google.com/?q=" + latitude + "," + longitude;
                String message = emergencyText + locationUrl;

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    if (!contact1.isEmpty()) {
                        smsManager.sendTextMessage(contact1, null, message, null, null);
                    }
                    if (!contact2.isEmpty()) {
                        smsManager.sendTextMessage(contact2, null, message, null, null);
                    }
                    Toast.makeText(HomeActivity.this, "SOS message sent!", Toast.LENGTH_SHORT).show();

                    makeEmergencyCalls();

                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Failed to send SOS message", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) {}
        }, null);
    }

    private void makeEmergencyCalls() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            return;
        }

        Handler handler = new Handler();

        // Call contact1 immediately
        if (!contact1.isEmpty()) {
            handler.postDelayed(() -> {
                Intent callIntent1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact1));
                startActivity(callIntent1);
            }, 1000); // 1 second delay
        }

        // Call contact2 after 5 seconds
        if (!contact2.isEmpty()) {
            handler.postDelayed(() -> {
                Intent callIntent2 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact2));
                startActivity(callIntent2);
            }, 6000); // 6 seconds total
        }

        // Call police after 10 seconds
        handler.postDelayed(() -> {
            Intent policeCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:100"));
            startActivity(policeCall);
        }, 11000); // 11 seconds total
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendEmergencySms();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCalls();
            } else {
                Toast.makeText(this, "Call Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
