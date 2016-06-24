package com.example.user.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//        Intent intent =new Intent(this,VideoPlayerActivity.class);
//        startActivity(intent);
        Button skip= (Button) findViewById(R.id.button);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MainActivity.this,VideoPlayerActivity.class);
                startActivity(intent);
            }
        });
    }
}
