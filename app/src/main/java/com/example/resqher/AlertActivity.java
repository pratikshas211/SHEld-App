package com.example.resqher;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AlertActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Button btnSiren = findViewById(R.id.btn_siren);
        Button btnWhistle = findViewById(R.id.btn_whistle);
        Button btnCry = findViewById(R.id.btn_cry);
        Button btnStop = findViewById(R.id.btn_stop);

        btnSiren.setOnClickListener(v -> playSound(R.raw.siren));
        btnWhistle.setOnClickListener(v -> playSound(R.raw.whistle));
        btnCry.setOnClickListener(v -> playSound(R.raw.cry));
        btnStop.setOnClickListener(v -> stopSound());
    }

    private void playSound(int soundResId) {
        stopSound(); // Ensure previous sound stops before playing a new one

        mediaPlayer = MediaPlayer.create(this, soundResId);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> stopSound()); // Auto-release after completion
            mediaPlayer.start();
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound(); // Ensure release on activity destruction
    }
}
