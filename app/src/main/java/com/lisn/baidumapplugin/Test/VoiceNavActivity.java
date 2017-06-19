package com.lisn.baidumapplugin.Test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lisn.baidumapplugin.R;

public class VoiceNavActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_nav);
        Button button = (Button) findViewById(R.id.bt_voiceNav);
        final NavigationHelper navigationHelper = new NavigationHelper(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VoiceNavActivity.this, "路线规划中。。。", Toast.LENGTH_LONG).show();
                navigationHelper.routeplanToNavi(110, 31, 118.805325, 32.094316);
            }
        });
    }
}
