package com.example.resqher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etPhone, etPass1, etPass2;
    private TextView tvSignup;
    private Button btnConfirm;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        etPass1 = findViewById(R.id.etPass1);
        etPass2 = findViewById(R.id.etPass2);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvSignup = findViewById(R.id.tvSignUp);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnConfirm.setOnClickListener(v -> registerUser());

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SuccessActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPass1.getText().toString().trim();
        String confirmPassword = etPass2.getText().toString().trim();

        if (!isValidUsername(username)) {
            etUsername.setError("Only alphabets & spaces allowed (Max 25 characters)");
            etUsername.requestFocus();
            return;
        }

        if (!isValidPhone(phone)) {
            etPhone.setError("Enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            etPass1.setError("Password must be 6-12 chars, include uppercase, lowercase, number & special char");
            etPass1.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etPass2.setError("Passwords do not match");
            etPass2.requestFocus();
            return;
        }

        databaseReference.orderByChild("phone").equalTo(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etPhone.setError("Phone number already registered. Try logging in.");
                            etPhone.requestFocus();
                        } else {
                            saveUserToDatabase(username, phone, password);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignUpActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String username, String phone, String password) {
        String userId = databaseReference.push().getKey();
        User user = new User(username, phone, password);

        assert userId != null;
        databaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
                intent.putExtra("phone_number", phone);
                intent.putExtra("user_name",username);

                startActivity(intent);
                finish();

                // If you want to use SuccessActivity later, move it to OTP success
                // startActivity(new Intent(SignUpActivity.this, SuccessActivity.class));
            } else {
                Toast.makeText(SignUpActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z ]{1,25}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9]{10}$");
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,12}$");
    }
}

// ✅ User class
class User {
    public String username, phone, password;

    public User() {
        // Required for Firebase
    }

    public User(String username, String phone, String password) {
        this.username = username;
        this.phone = phone;
        this.password = password;
    }
}
