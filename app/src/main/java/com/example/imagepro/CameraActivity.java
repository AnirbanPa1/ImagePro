package com.example.imagepro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Locale;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;

    private ImageButton translate_button;
    private ImageButton take_pic_button;
    private ImageButton show_image_button;

    private ImageView current_image;
    private TextView text_view;

    private TextRecognizer textRecognizer;

    private String Camera_or_recognizeText = "camera";

    private Bitmap bitmap = null;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        textRecognizer= TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());

        text_view=findViewById(R.id.text_view);
        text_view.setVisibility(View.GONE);

        take_pic_button=findViewById(R.id.take_pic_button);
        take_pic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Camera_or_recognizeText == "camera"){
                    take_pic_button.setColorFilter(Color.DKGRAY);
                    Mat a = mRgba.t();
                    Core.flip(a,mRgba,1);
                    a.release();
                    bitmap=Bitmap.createBitmap(mRgba.cols(),mRgba.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mRgba,bitmap);
                    mOpenCvCameraView.disableView();
                    Camera_or_recognizeText="recognizeText";

                }else{
                    take_pic_button.setColorFilter(Color.WHITE);
                    text_view.setVisibility(View.GONE);
                    current_image.setVisibility(View.GONE);
                    mOpenCvCameraView.enableView();
                    text_view.setText("");
                    Camera_or_recognizeText="camera";
                }
            }
        });
//        take_pic_button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                    return true;
//                }
//
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                    if(Camera_or_recognizeText == "camera"){
//                        take_pic_button.setColorFilter(Color.DKGRAY);
//                        Mat a = mRgba.t();
//                        Core.flip(a,mRgba,1);
//                        a.release();
//                        bitmap=Bitmap.createBitmap(mRgba.cols(),mRgba.rows(),Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(mRgba,bitmap);
//                        mOpenCvCameraView.disableView();
//                        Camera_or_recognizeText="recognizeText";
//
//                    }else{
//                        take_pic_button.setColorFilter(Color.WHITE);
//                        text_view.setVisibility(View.GONE);
//                        current_image.setVisibility(View.GONE);
//                        mOpenCvCameraView.enableView();
//                        text_view.setText("");
//                        Camera_or_recognizeText="camera";
//                    }
//
//                    return true;
//                }
//
//                return false;
//            }
//        });

        translate_button=findViewById(R.id.translate_button);
        translate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Camera_or_recognizeText=="recognizeText"){

                    //text_view.setVisibility(View.VISIBLE);

                    InputImage image = InputImage.fromBitmap(bitmap,0);
                    Task<Text> result = textRecognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {

                                    text_view.setText(text.getText());
                                    //CHANGE
                                    String str = text.getText().toString();
                                    Intent intent = new Intent(getApplicationContext(),trActivity.class);
                                    intent.putExtra("msg_key",str);
                                    startActivity(intent);
                                    //CHANGE
                                    Log.d("CameraActivity","Out"+text.getText());

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }else{
                    Toast.makeText(CameraActivity.this,"Please take a photo",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
//        translate_button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                    translate_button.setColorFilter(Color.DKGRAY);
//                    return true;
//                }
//
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                    translate_button.setColorFilter(Color.WHITE);
//                    if(Camera_or_recognizeText=="recognizeText"){
//
//                        //text_view.setVisibility(View.VISIBLE);
//
//                        InputImage image = InputImage.fromBitmap(bitmap,0);
//                        Task<Text> result = textRecognizer.process(image)
//                                .addOnSuccessListener(new OnSuccessListener<Text>() {
//                                    @Override
//                                    public void onSuccess(Text text) {
//
//                                        text_view.setText(text.getText());
//                                        //CHANGE
//                                        String str = text.getText().toString();
//                                        Intent intent = new Intent(getApplicationContext(),trActivity.class);
//                                        intent.putExtra("msg_key",str);
//                                        startActivity(intent);
//                                        //CHANGE
//                                        Log.d("CameraActivity","Out"+text.getText());
//
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                    }
//                                });
//                    }else{
//                        Toast.makeText(CameraActivity.this,"Please take a photo",
//                                Toast.LENGTH_LONG).show();
//                    }
//                    return true;
//                }
//
//                return false;
//            }
//        });

        show_image_button=findViewById(R.id.show_image_button);
        show_image_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    show_image_button.setColorFilter(Color.DKGRAY);
                    return true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP){
                    show_image_button.setColorFilter(Color.WHITE);
                    if(Camera_or_recognizeText=="recognizeText"){
                        text_view.setVisibility(View.GONE);
                        current_image.setVisibility(View.VISIBLE);
                        current_image.setImageBitmap(bitmap);
                    }else{
                        Toast.makeText(CameraActivity.this,"Please take a photo",
                                Toast.LENGTH_LONG).show();
                    }
                    return true;
                }

                return false;
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();

        return mRgba;

    }



}