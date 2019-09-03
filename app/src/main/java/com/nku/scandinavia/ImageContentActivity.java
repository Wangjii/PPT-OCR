package com.nku.scandinavia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.nku.scandinavia.helpers.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageContentActivity extends AppCompatActivity {
    TextView textView;
    TessBaseAPI baseApi = new TessBaseAPI();
    private static final String DEFAULT_LANGUAGE = "chi_sim";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_content);
        initContent();
    }

    private void initContent() {
        textView = findViewById(R.id.content);
        try {
            initTessBaseAPI();
        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    private void initTessBaseAPI() throws IOException {
        String datapath = Environment.getExternalStorageDirectory() + "/tesseract";
        Log.e( "datapath: ", datapath);
        File dir = new File(datapath + "/tessdata");
//        File dir = new File(datapath);
        if (!dir.exists()) {
            dir.mkdirs();
            InputStream input = getResources().openRawResource(R.raw.chi_sim);
            File file = new File(dir, "chi_sim.traineddata");
            FileOutputStream output = new FileOutputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = input.read(buff)) != -1) {
                output.write(buff,0,len);
            }
            input.close();
            output.close();
        }
        boolean success = baseApi.init(datapath,DEFAULT_LANGUAGE);
        if(success){
            Log.e("output","load Tesseract OCR Engine successfully");
        }else {
            Log.e("output","WARNING:could not initialize Tesseract data ...");
        }
        recognizeTextImage();
    }

    private void recognizeTextImage(){
        baseApi.setImage(Constants.croppedImageBitmap);
        String recognizedText = baseApi.getUTF8Text();
        if(!recognizedText.isEmpty()){
            textView.append("识别结果为:\n"+recognizedText);
        }
    }


}
