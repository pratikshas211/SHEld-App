package com.example.resqher;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_CONTACT = 1;

    private TextView emergencyContact1, emergencyContact2;
    private EditText nameEditText, phoneEditText;
    private Button saveButton;
    private ImageButton modifyPhoneButton;
    private DatabaseReference databaseReference;

    private int selectedContact = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emergencyContact1 = findViewById(R.id.emergencyContact1);
        emergencyContact2 = findViewById(R.id.emergencyContact2);
        saveButton = findViewById(R.id.saveButton);
        modifyPhoneButton = findViewById(R.id.modifyPhoneButton);

        String fullName = getIntent().getStringExtra("user_name");
        String phoneNumber = getIntent().getStringExtra("phone_number");

        nameEditText.setText(fullName);
        phoneEditText.setText(phoneNumber);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("Profile");

        loadProfileData();

        saveButton.setOnClickListener(v -> saveProfileData());
        modifyPhoneButton.setOnClickListener(v -> pickContact());
    }

    private void saveProfileData() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        boolean isValid = true;

        if (!isValidName(name)) {
            nameEditText.setError("Only alphabets allowed (Max 25 chars)");
            isValid = false;
        }

        if (!isValidPhoneNumber(phone)) {
            phoneEditText.setError("Enter a valid 10-digit phone number");
            isValid = false;
        }

        if (emergencyContact1.getTag() == null || emergencyContact2.getTag() == null) {
            Toast.makeText(this, "Please select both emergency contacts", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) return;

        String contact1Number = (String) emergencyContact1.getTag();
        String contact2Number = (String) emergencyContact2.getTag();

        UserProfile profile = new UserProfile(
                name,
                phone,
                contact1Number,
                contact2Number
        );

        databaseReference.setValue(profile).addOnSuccessListener(aVoid ->
                Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(ProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show()
        );
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z ]{1,25}$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^[0-9]{10}$");
    }

    private void loadProfileData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        nameEditText.setText(profile.getName());
                        phoneEditText.setText(profile.getPhone());

                        emergencyContact1.setText(profile.getEmergencyContact1());
                        emergencyContact1.setTag(profile.getEmergencyContact1());

                        emergencyContact2.setText(profile.getEmergencyContact2());
                        emergencyContact2.setTag(profile.getEmergencyContact2());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(numberIndex).replaceAll("\\s+", "");
                    cursor.close();

                    if (selectedContact == 1) {
                        emergencyContact1.setText(number);
                        emergencyContact1.setTag(number);
                        selectedContact = 2;
                    } else {
                        emergencyContact2.setText(number);
                        emergencyContact2.setTag(number);
                        selectedContact = 1;
                    }
                }
            }
        }
    }

    // Modified UserProfile class
    public static class UserProfile {
        private String name, phone;
        private String emergencyContact1, emergencyContact2;

        public UserProfile() {
        }

        public UserProfile(String name, String phone, String emergencyContact1, String emergencyContact2) {
            this.name = name;
            this.phone = phone;
            this.emergencyContact1 = emergencyContact1;
            this.emergencyContact2 = emergencyContact2;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmergencyContact1() {
            return emergencyContact1;
        }

        public String getEmergencyContact2() {
            return emergencyContact2;
        }
    }
}
