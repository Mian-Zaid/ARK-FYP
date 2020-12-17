package com.app.ark.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class playSound extends AppCompatActivity {

    Button btnPlay, btnpause, btnStop;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_sound);

        btnPlay = findViewById(R.id.btnPlay);
        btnpause = findViewById(R.id.btnpause);
        btnStop = findViewById(R.id.btnStop);

        btnPlay.setOnClickListener(v -> startPlayer());
        btnpause.setOnClickListener(v -> pausePlayer());
        btnStop.setOnClickListener(v -> stopPlayer());
    }

    private void stopPlayer() {
        if (player != null) {
            player.release();
            player = null;

        }
    }


    private void pausePlayer() {

        if (player != null) {
            player.pause();
        }
    }

    private void startPlayer() {

        if (player == null) {
            player = MediaPlayer.create(this, R.raw.elephant);
            player.setOnCompletionListener(mp -> stopPlayer());
        }
        player.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }
}