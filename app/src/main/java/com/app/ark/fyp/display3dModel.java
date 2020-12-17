package com.app.ark.fyp;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display3d_model);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            modelName = bundle.getString("modelName");
        } else {
            modelName = null;
        }

        FirebaseApp.initializeApp(this);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        Button btn = findViewById(R.id.btn);

        final ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        btn.setOnClickListener(v -> {
            try {
                if (modelName != null) {
                    Toast.makeText(this, "" + modelName.toLowerCase(), Toast.LENGTH_SHORT).show();

                    File file = File.createTempFile(modelName.toLowerCase(), "glb");
                    String modl = modelName.toLowerCase() + ".glb";
                    StorageReference modelRef = storage.getReference().child(modl);

                    modelRef.getFile(file).addOnSuccessListener(taskSnapshot -> buildModel(file));
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        assert arFragment != null;
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Log.d("andaragaya", "yes planne");

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
//            anchorNode.setRenderable(renderable);


            // Create the transformable andy and add it to the anchor.
            TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
// Maxscale must be greater than minscale
            node.getScaleController().setMaxScale(0.2f);
            node.getScaleController().setMinScale(0.1f);

            node.setParent(anchorNode);
            node.setRenderable(renderable);
            node.select();
            arFragment.getArSceneView().getScene().addChild(anchorNode);

        });
    }

    private ModelRenderable renderable;

    private void buildModel(File file) {

        Log.d("andaragaya", "yes");
        Log.d("andaragaya", String.valueOf(Uri.parse(file.getPath())));

        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable
                .builder()
                .setSource(this, renderableSource)
                .setRegistryId(Uri.parse(file.getPath()))
                .build()
                .thenAccept(modelRenderable -> {
                    Log.d("andaragaya", "yes again");

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