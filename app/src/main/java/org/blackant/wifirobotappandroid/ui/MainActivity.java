package org.blackant.wifirobotappandroid.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import org.blackant.wifirobotappandroid.R;


public class MainActivity extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();

    private Button btnSettings;

    private final View.OnClickListener jumpToSettingsListener = v -> {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        btnSettings = findViewById(R.id.dummy_button);
        btnSettings.setOnClickListener(jumpToSettingsListener);
    }

}
