package com.nku.scandinavia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nku.scandinavia.helpers.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class ImageProcess2Activity extends AppCompatActivity {
    ImageView imageView;
    FloatingActionButton contentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process2);
        initComponents();
    }

    private void initComponents() {
        imageView = findViewById(R.id.croppedImageView);
        contentButton = findViewById(R.id.button_content);
        imageView.setImageBitmap(Constants.croppedImageBitmap);

        contentButton.setOnClickListener(this.contentButtonClick);
    }

    private View.OnClickListener contentButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), ImageContentActivity.class);
            startActivity(intent);
        }
    };


}
