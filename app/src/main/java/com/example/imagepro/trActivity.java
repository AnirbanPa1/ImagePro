package com.example.imagepro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;


public class trActivity extends AppCompatActivity {

    TextView received_value_id;
    TextToSpeech textToSpeech;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            Log.v("close", "KEYCODE_HOME");
            onUserLeaveHint();

            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                onBackPressed();
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_MENU)) {

            return true;
        }
        return false;
    }


    public void onUserLeaveHint() { // this only executes when Home is selected.

        super.onUserLeaveHint();
        if (!isFinishing()) {
            Log.v("close", "if condition working");
            if(textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
            finish();
        }

    }

//    @Override
//    protected void onDestroy() {

//        super.onDestroy();
//    }

    @Override
    public void onBackPressed() {
        textToSpeech.stop();
        textToSpeech.shutdown();
        finish();
        startActivity(new Intent(this, MainActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tr);

        received_value_id = findViewById(R.id.received_value_id);
        Intent intent = getIntent();
        String str = intent.getStringExtra("msg_key");
        received_value_id.setText(str);
        Log.d("MSG RECEIVED","OUT"+str);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.getDefault());

                }
            }
        });

        FloatingActionButton speak_button = findViewById(R.id.speak_button);
        speak_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(str,TextToSpeech.QUEUE_FLUSH,null);
            }

        });


    }



}