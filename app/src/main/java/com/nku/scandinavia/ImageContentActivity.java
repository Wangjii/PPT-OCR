package com.nku.scandinavia;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nku.scandinavia.helpers.TransApi;


import androidx.appcompat.app.AppCompatActivity;
import android.os.StrictMode;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.nku.scandinavia.helpers.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageContentActivity extends AppCompatActivity {
    TextView textView;
    String recognizedText;
    TessBaseAPI baseApi = new TessBaseAPI();

    FloatingActionButton transButton;
//    private static final String DEFAULT_LANGUAGE = "chi_sim";
    private static final String DEFAULT_LANGUAGE = "eng";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_content);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        initContent();
    }

    private void initContent() {
        textView = findViewById(R.id.content);
        transButton = findViewById(R.id.button_trans);
        transButton.setOnClickListener(this.transButtonClick);
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
        if (!dir.exists()) {
            dir.mkdirs();
//            InputStream input = getResources().openRawResource(R.raw.chi_sim);
//            File file = new File(dir, "chi_sim.traineddata");
            InputStream input = getResources().openRawResource(R.raw.eng);
            File file = new File(dir, "eng.traineddata");
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
        recognizedText = baseApi.getUTF8Text();
        if(recognizedText.isEmpty()){
            textView.append("识别结果为空");
        }else {
            recognizedText = recognizedText.replace('\n',' ');
            textView.append("识别结果为:\n"+recognizedText);
        }
    }

    // 在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
    private static final String APP_ID = "20190903000331566";
    private static final String SECURITY_KEY = "RNuPntEEoO09xCaTA2HX";

    public void trans() {
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);

        textView.setText("正在翻译……");

        try {
            JSONObject jsonObject = new JSONObject(api.getTransResult(recognizedText,"auto","zh"));
            jsonObject = jsonObject.getJSONArray("trans_result").getJSONObject(0);
            String result=jsonObject.getString("dst");
            textView.setText(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener transButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            trans();
        }
    };






}
