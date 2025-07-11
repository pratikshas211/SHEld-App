package com.example.resqher;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etPhone, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private DatabaseReference databaseReference;

    // Password regex pattern: Min 6 - Max 12, must include uppercase, lowercase, digit, and special character
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,12}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users"); // Change "Users" to your node name

        // Initialize UI elements
        etPhone = findViewById(R.id.etPhone);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Reset Password Click Listener
        btnResetPassword.setOnClickListener(view -> resetPassword());

        // Back to Login Click Listener
        tvBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void resetPassword() {
        String phone = etPhone.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate phone number
        if (!validatePhoneNumber(phone)) {
            etPhone.setError("Enter a valid registered phone number (10 digits)");
            return;
        }

        // Validate new password
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            etNewPassword.setError("Password must be 6-12 chars, include uppercase, lowercase, digit & special char");
            return;
        }

        // Confirm password check
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Check if the phone number exists in the database
        databaseReference.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Update password in Firebase
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        userSnapshot.getRef().child("password").setValue(newPassword);
                    }
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_LONG).show();

                    // Redirect to Login
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                    finish();
                } else {
                    etPhone.setError("Phone number not found");
                    Toast.makeText(ForgotPasswordActivity.this, "This phone number is not registered!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ForgotPasswordActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validatePhoneNumber(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() == 10 && TextUtils.isDigitsOnly(phone);
    }
}
