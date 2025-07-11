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

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgot;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgot = findViewById(R.id.tvForgot);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Handle Login Button Click
        btnLogin.setOnClickListener(v -> loginUser());

        // Navigate to Sign Up Activity
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        // Navigate to Forgot Password Activity
        tvForgot.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate Phone Number
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Enter your phone number");
            etPhone.requestFocus();
            return;
        }

        if (!isValidPhone(phone)) {
            etPhone.setError("Enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        authenticateUser(phone, password);
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9]{10}$");
    }

    private void authenticateUser(String phone, String password) {
        databaseReference.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.password.equals(password)) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();  // Prevent going back to Login Activity
                            return;
                        } else {
                            etPassword.setError("Incorrect password");
                            etPassword.requestFocus();
                        }
                    }
                } else {
                    etPhone.setError("User not found");
                    etPhone.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
