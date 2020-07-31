package com.alisoft.StoneVPN.speed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.alisoft.StoneVPN.speed.R;

public class SettingsActivity extends AppCompatActivity {

    ImageView backToActivity;
    TextView activity_name;
    CardView cardView_about;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        cardView_about = (CardView) findViewById(R.id.about);

        activity_name = (TextView) findViewById(R.id.activity_name);
        backToActivity = (ImageView) findViewById(R.id.finish_activity);

        activity_name.setText("Settings");
        backToActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cardView_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "About", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SettingsActivity.this, About.class));

            }
        });
    }
}
