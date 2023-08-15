package com.example.imagepro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

import java.io.IOException;

public class storageRecognitionActivity extends AppCompatActivity {

    private Button select_image;
    //private ImageView recognizeImage_button;
    private ImageView image_view;
    //private TextView text_view;
    private ImageButton rec_btn;

    int Selected_Picture=200;

    private TextRecognizer textRecognizer;

    private String show_image_or_text="text";
    Bitmap bitmap = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_recognition);

        image_view=findViewById(R.id.image_view);

        select_image=findViewById(R.id.select_image);
        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_chooser();
            }
        });

        textRecognizer= TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
        //text_view = findViewById(R.id.text_view);
        //text_view.setVisibility(View.GONE);

//        recognizeImage_button=findViewById(R.id.recognizeImage_button);
//        recognizeImage_button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction()==MotionEvent.ACTION_DOWN){
//                    recognizeImage_button.setColorFilter(Color.DKGRAY);
//                    return true;
//                }
//                if(event.getAction()==MotionEvent.ACTION_UP){
//                    recognizeImage_button.setColorFilter(Color.WHITE);
//                    recognize(bitmap);
//                    if(show_image_or_text=="text"){
//                        //text_view.setVisibility(View.GONE);
//                        image_view.setVisibility(View.VISIBLE);
//                        show_image_or_text="image";
//
//                    }else{
//                        //text_view.setVisibility(View.VISIBLE);
//                        image_view.setVisibility(View.GONE);
//                        show_image_or_text="text";
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });

        rec_btn = findViewById(R.id.rec_btn);
        rec_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognize(bitmap);
            }
        });

    }

    void image_chooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Selected Picture"),Selected_Picture);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            if(requestCode==Selected_Picture){
                Uri selectedImageUri=data.getData();
                //CHANGE
//                String[] filepath={MediaStore.Images.Media.DATA};
//
//                Cursor cursor = getContentResolver().query(selectedImageUri,filepath,null,null,null);
//                cursor.moveToFirst();
//                int columnIndex=cursor.getColumnIndex(filepath[0]);
//                String picturepath = cursor.getString(columnIndex);
//                cursor.close();
                //CHANGE

                if(selectedImageUri != null){
                    Log.d("storage_Activity","Output Uri: "+selectedImageUri);

                    try{
                        bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImageUri);

                        image_view.setImageBitmap(bitmap);

                        show_image_or_text = "image";

                    }catch(IOException e){
                        e.printStackTrace();
                    }


                }
            }
        }
    }

    void recognize(Bitmap bitmap){
        if(show_image_or_text=="image") {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            Task<Text> result = textRecognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            //text_view.setText(text.getText());
                            //CHANGE
                            String str = text.getText().toString();
                            Intent intent = new Intent(getApplicationContext(), trActivity.class);
                            intent.putExtra("msg_key", str);
                            startActivity(intent);
                            //CHANGE
                            Log.d("Storage_activity", "Out: " + text.getText());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }else{
            Toast.makeText(storageRecognitionActivity.this,"Please select a photo",
                    Toast.LENGTH_LONG).show();
        }
    }

}