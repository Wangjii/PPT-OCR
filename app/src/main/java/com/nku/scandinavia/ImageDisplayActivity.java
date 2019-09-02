package com.nku.scandinavia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nku.scandinavia.helpers.Constants;
import com.nku.scandinavia.libs.NativeClass;
import com.nku.scandinavia.libs.PolygonView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageDisplayActivity extends AppCompatActivity {
    ImageView selectedImageView;
    FloatingActionButton nextButton;
    FloatingActionButton prevButton;
    FrameLayout holderImageCrop;
    Bitmap selectedImageBitmap;

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
        selectedImageView = (ImageView) findViewById(R.id.imageView);
        selectedImageView.setImageBitmap(Constants.selectedImageBitmap);
        nextButton = findViewById(R.id.button_next);
        prevButton = findViewById(R.id.button_prev);
        polygonView = findViewById(R.id.polygon);
        holderImageCrop = findViewById(R.id.holderImageCrop);

        holderImageCrop.post(new Runnable() {
            @Override
            public void run() {
                initCrop();
            }
        });
    }

    private void initCrop() {
        selectedImageBitmap = Constants.selectedImageBitmap;
        Constants.selectedImageBitmap = null;
        Bitmap scaledBitmap = scaleBitmap(selectedImageBitmap, holderImageCrop.getWidth(), holderImageCrop.getHeight());
        selectedImageView.setImageBitmap(scaledBitmap);
        Bitmap tmp = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tmp);

        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scan_padding);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(tmp.getWidth() + 2 * padding, tmp.getHeight() + 2 * padding);
        polygonView.setLayoutParams(layoutParams);
    }

    private void initEvents() {
        nextButton.setOnClickListener(this.nextButtonClick);
        prevButton.setOnClickListener(this.prevButtonClick);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap bitmap) {
        List<PointF> pointFs = getContourEdgePoints(bitmap);
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(bitmap, pointFs);
        return orderedPoints;
    }

    private List<PointF> getContourEdgePoints(Bitmap bitmap) {
        MatOfPoint2f point2f = nativeClass.getPoint(bitmap);
        List<Point> points;
        if (point2f == null) {
            Point[] array_points = new Point[4];
            array_points[0] = new Point(0, 0);
            array_points[1] = new Point(0, bitmap.getWidth());
            array_points[2] = new Point(bitmap.getHeight(), 0);
            array_points[3] = new Point(bitmap.getHeight(), bitmap.getWidth());
            points = Arrays.asList(array_points);
            Toast.makeText(this, "未找到轮廓，请手动选择", Toast.LENGTH_SHORT).show();
        } else {
            points = Arrays.asList(point2f.toArray());
        }

        List<PointF> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            result.add(new PointF(((float) points.get(i).x), ((float) points.get(i).y)));
        }
        return result;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap bitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(bitmap);
        }
        return orderedPoints;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap bitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(bitmap.getHeight(), 0));
        outlinePoints.put(2, new PointF(0, bitmap.getWidth()));
        outlinePoints.put(3, new PointF(bitmap.getHeight(), bitmap.getWidth()));
        return outlinePoints;
    }

    private Bitmap scaleBitmap(Bitmap image, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, image.getWidth(), image.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
    }

    protected Bitmap getCroppedImage() {
        Map<Integer, PointF> points = polygonView.getPoints();
        float xRatio = (float) selectedImageBitmap.getWidth() / selectedImageView.getWidth();
        float yRatio = (float) selectedImageBitmap.getHeight() / selectedImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;

        return nativeClass.getScannedBitmap(selectedImageBitmap, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    private View.OnClickListener nextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Constants.croppedImageBitmap = getCroppedImage();
            Intent intent = new Intent(getApplicationContext(), ImageProcessActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener prevButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
}
