package com.app.ark.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.tensorflow.lite.Interpreter;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    TextView textView, textView2;
    Button btnSelect;
    ImageLabeler labeler;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // intiallizing views

        image = findViewById(R.id.image);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        btnSelect = findViewById(R.id.btnSelect);


        //Build ML Model of TFLITE
        AutoMLImageLabelerLocalModel localModel =
                new AutoMLImageLabelerLocalModel.Builder()
                        .setAssetFilePath("model/manifest.json")
                        .build();

        AutoMLImageLabelerOptions autoMLImageLabelerOptions =
                new AutoMLImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.0f)
                        .build();
        labeler = ImageLabeling.getClient(autoMLImageLabelerOptions);


        // take image from camera

        btnSelect.setOnClickListener(v -> {
            //from camera
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

    }

    // system permissions to capture image
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    // to get back image from camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            image.setImageBitmap(photo); //set image to image view

            InputImage image;
            image = InputImage.fromBitmap(photo, 0);

            final List<String> labelList = new ArrayList<>();
            final List<Float> confidenceList = new ArrayList<>();


            //process image in ML model

            labeler.process(image)
                    .addOnSuccessListener(labels -> {

                        textView2.setText("");
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();

                            labelList.add(text);
                            confidenceList.add(confidence);
                        }
                        // find max confidence
                        float max = confidenceList.get(0);
                        int index = 0;
                        for (int i = 0; i < confidenceList.size(); ++i) {
                            if (confidenceList.get(i) > max) {
                                max = confidenceList.get(i);
                                index = i;
                            }
                        }

                        // Go to next activy to display 3d Models
                        Intent intent = new Intent(getApplicationContext(), display3dModel.class);
                        intent.putExtra("modelName", labelList.get(index));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Task failed with an exception
                        // ...
                    });
        }

    }


}
