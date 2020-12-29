package com.app.ark.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class display3dModel extends AppCompatActivity {
    String modelName = null;
    MediaPlayer player;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display3d_model);

        //get model name from previous activity

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            modelName = bundle.getString("modelName");
        } else {
            modelName = null;
        }

        //firebase storage
        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();


        // get models from firebase Database
        getModels();

        // AR fragment to diaplay 3d model
        final ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        assert arFragment != null;

        //tap on AR plane to render model on it
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            //display models based on numbers

            if (modelName.equals("3")) {
                for (int i = 0; i < 3; ++i) {
                    displayModel(hitResult, arFragment);
                }
                //background sound of animals
                playSounds("tiger");

            } else if (modelName.equals("5")) {
                for (int i = 0; i < 5; ++i) {
                    displayModel(hitResult, arFragment);
                }
                //background sound of animals
                playSounds("elephent");

            } else if (modelName.equals("8")) {

                for (int i = 0; i < 8; ++i) {
                    displayModel(hitResult, arFragment);
                }

            } else {
                // single model display
                displayModel(hitResult, arFragment);
                //background sound of animals
                playSounds(modelName.toLowerCase());
            }


        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

    void getModels() {
        try {
            if (modelName != null) {

                //model of Numbers

                if (modelName.equals("3")) {
                    Toast.makeText(this, "" + modelName.toLowerCase(), Toast.LENGTH_SHORT).show();
                    File file = File.createTempFile("tiger", "glb");
                    String modl = "tiger" + ".glb";
                    StorageReference modelRef = storage.getReference().child(modl);
                    modelRef.getFile(file).addOnSuccessListener(taskSnapshot -> buildModel(file));
                } else if (modelName.equals("5")) {
                    Toast.makeText(this, "" + modelName.toLowerCase(), Toast.LENGTH_SHORT).show();
                    File file = File.createTempFile("elephent", "glb");
                    String modl = "elephent" + ".glb";
                    StorageReference modelRef = storage.getReference().child(modl);
                    modelRef.getFile(file).addOnSuccessListener(taskSnapshot -> buildModel(file));
                } else if (modelName.equals("8")) {
                    Toast.makeText(this, "" + modelName.toLowerCase(), Toast.LENGTH_SHORT).show();
                    File file = File.createTempFile("cap", "glb");
                    String modl = "cap" + ".glb";
                    StorageReference modelRef = storage.getReference().child(modl);
                    modelRef.getFile(file).addOnSuccessListener(taskSnapshot -> buildModel(file));
                }

                //model other than numbers

                else {

                    Toast.makeText(this, "" + modelName.toLowerCase(), Toast.LENGTH_SHORT).show();

                    File file = File.createTempFile(modelName.toLowerCase(), "glb");
                    String modl = modelName.toLowerCase() + ".glb";
                    StorageReference modelRef = storage.getReference().child(modl);

                    modelRef.getFile(file).addOnSuccessListener(taskSnapshot -> buildModel(file));
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void playSounds(String name) {
        if (name.equals("fox")) {
            startPlayer(R.raw.fox);
        } else if (name.equals("elephent")) {
            startPlayer(R.raw.elephant);
        } else if (name.equals("tiger")) {
            startPlayer(R.raw.tiger);
        }
    }

    private void displayModel(HitResult hitResult, ArFragment arFragment) {

        //create anchor point where we tapped

        AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());

        // Create the transformable andy and add it to the anchor.
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        //sacle size of 3d Model on screen

        node.getScaleController().setMaxScale(0.2f);
        node.getScaleController().setMinScale(0.1f);

        //finally set node with 3d model on AR fragment
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        node.select();
        arFragment.getArSceneView().getScene().addChild(anchorNode);

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

    //start playing sound
    private void startPlayer(int sound) {

        if (player == null) {
            player = MediaPlayer.create(this, sound);
            player.setOnCompletionListener(mp -> stopPlayer());
        }
        player.start();
    }

    private ModelRenderable renderable;

    private void buildModel(File file) {

        //create renderable source

        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        //intialize renderable 3d model
        ModelRenderable
                .builder()
                .setSource(this, renderableSource)
                .setRegistryId(Uri.parse(file.getPath()))
                .build()
                .thenAccept(modelRenderable -> {
                    Toast.makeText(display3dModel.this, "Model built", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;

                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
