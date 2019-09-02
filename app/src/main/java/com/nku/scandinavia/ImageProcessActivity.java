package com.nku.scandinavia;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nku.scandinavia.helpers.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;

public class ImageProcessActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        initComponents();
    }

    private void initComponents() {
        imageView = findViewById(R.id.croppedImageView);
        imageView.setImageBitmap(Constants.croppedImageBitmap);
    }

}
