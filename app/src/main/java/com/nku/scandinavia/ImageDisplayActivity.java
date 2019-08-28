package com.nku.scandinavia;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nku.scandinavia.helpers.Constants;
import com.nku.scandinavia.libs.NativeClass;
import com.nku.scandinavia.libs.PolygonView;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;

public class ImageDisplayActivity extends AppCompatActivity {
    ImageView selectedImageView;
    FloatingActionButton nextButton;
    FloatingActionButton prevButton;

    NativeClass nativeClass;
    PolygonView polygonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        initComponents();
        initEvents();
    }

    private void initComponents() {
        nativeClass = new NativeClass();
        selectedImageView = (ImageView) findViewById(R.id.selectedImage);
        selectedImageView.setImageBitmap(Constants.selectedImageBitmap);
        nextButton = findViewById(R.id.button_next);
        prevButton = findViewById(R.id.button_prev);
        polygonView = findViewById(R.id.polygon);
    }

    private void initEvents() {
        nextButton.setOnClickListener(this.nextButtonClick);
        nextButton.setOnClickListener(this.prevButtonClick);
    }

    private View.OnClickListener nextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    private View.OnClickListener prevButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
}
