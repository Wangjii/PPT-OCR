package com.nku.scandinavia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nku.scandinavia.baidu.Base64Util;
import com.nku.scandinavia.baidu.GsonUtils;
import com.nku.scandinavia.baidu.HttpUtil;
import com.nku.scandinavia.baidu.OCResult;
import com.nku.scandinavia.helpers.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class ImageProcessActivity extends AppCompatActivity {
    ImageView imageView;
    String apiPath = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    TextView ocr_result_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        initComponents();
        new Thread(runnable).start();
        ocr_result_tv.setText("Loading...");
    }

    private void initComponents() {
        imageView = findViewById(R.id.croppedImageView);
        imageView.setImageBitmap(Constants.croppedImageBitmap);
        ocr_result_tv = findViewById(R.id.ocr_result);
    }

    private void setOCResult(List<Map<String, String>> ocr_result_list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ocr_result_list.size(); i++) {
            if (ocr_result_list.get(i).get("words") != null) {
                stringBuilder.append(ocr_result_list.get(i).get("words") + '\n');
            }
        }
        ocr_result_tv.setText(stringBuilder.toString());
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Bitmap image = Constants.croppedImageBitmap;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            try {
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String imgBase64 = Base64Util.encode(byteArray);
                String params = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(imgBase64, "UTF-8");
                String accessToken = "24.a09ae297b4d6c2162ef5701a2f962034.2592000.1570083344.282335-17167573";
                final String result = HttpUtil.post(apiPath, accessToken, params);
                Log.e("run: ", result);
                OCResult ocrResult = GsonUtils.fromJson(result, OCResult.class);
                final List<Map<String, String>> result_array = ocrResult.words_result;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setOCResult(result_array);
                    }
                });
            } catch (Exception e) {
                Log.e("run: ", e.toString());
                e.printStackTrace();
            }
        }
    };


}
