package com.example.resqher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_FIRST_TIME = "isFirstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getStartedButton = findViewById(R.id.button);

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean isFirstTime = preferences.getBoolean(KEY_FIRST_TIME, true);

                if (isFirstTime) {
                    // Navigate to RegisterActivity for the first time
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                    preferences.edit().putBoolean(KEY_FIRST_TIME, false).apply();
                } else {
                    // Navigate to HomeActivity from the second time onwards
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }
                finish();
            }
        });
    }
}
