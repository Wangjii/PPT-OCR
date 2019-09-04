package com.nku.scandinavia;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.nku.scandinavia.baidu.Base64Util;
import com.nku.scandinavia.baidu.GsonUtils;
import com.nku.scandinavia.baidu.HttpUtil;
import com.nku.scandinavia.baidu.OCResult;
import com.nku.scandinavia.helpers.Constants;
import com.nku.scandinavia.helpers.TransApi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class ImageProcessActivity extends AppCompatActivity {
    ImageView imageView;
    String apiPath = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    TextView ocr_result_tv;
    TextView ocr_result_google;
    TessBaseAPI baseApi = new TessBaseAPI();
    private BottomNavigationView bottom_navigation;
    private static final String DEFAULT_LANGUAGE = "eng";
    //    private static final String DEFAULT_LANGUAGE = "chi_sim";
    String recognizedText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        initComponents();
        new Thread(runnable).start();
        new Thread(runnable_google).start();
        ocr_result_tv.setText("Loading...");
        ocr_result_google.setText("Loading...");
    }

    private void initComponents() {

        imageView = findViewById(R.id.croppedImageView);
        imageView.setImageBitmap(Constants.croppedImageBitmap);
        ocr_result_tv = findViewById(R.id.ocr_result);
        ocr_result_google = findViewById(R.id.googel_ocr_result);

        bottom_navigation = findViewById(R.id.bottom_navigation);

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(
                    @NonNull
                            MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.buttom_enhance:

                        break;
                    case R.id.buttom_ocr:

                        break;
                    case R.id.buttom_trans:
                        new Thread(runnable_trans).start();
                        try {
                            Thread.sleep(10);
                            ocr_result_google.setText("正在翻译……");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return true;
            }
        });
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

    Runnable runnable_google = new Runnable() {
        @Override
        public void run() {
            try {
                final String result = initTessBaseAPI();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isEmpty()) {
                            ocr_result_google.setText("识别结果为空");
                        } else {
                            ocr_result_google.setText("");
                            ocr_result_google.append("识别结果为:\n" + result.replace('\n', ' '));
                        }
                    }
                });
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    };

    private String initTessBaseAPI() throws IOException {
        String datapath = Environment.getExternalStorageDirectory() + "/tesseract";
        Log.e("datapath: ", datapath);
        File dir = new File(datapath + "/tessdata");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
//            InputStream input = getResources().openRawResource(R.raw.chi_sim);
//            File file = new File(dir, "chi_sim.traineddata");
                InputStream input = getResources().openRawResource(R.raw.eng);
                File file = new File(dir, "eng.traineddata");
                FileOutputStream output = new FileOutputStream(file);
                byte[] buff = new byte[1024];
                int len = 0;
                while ((len = input.read(buff)) != -1) {
                    output.write(buff, 0, len);
                }
                input.close();
                output.close();

            }

        }
        boolean success = baseApi.init(datapath, DEFAULT_LANGUAGE);
        if (success) {
            baseApi.setImage(Constants.croppedImageBitmap);
            recognizedText = baseApi.getUTF8Text();
            recognizedText=recognizedText.replace('\n', ' ');
            return recognizedText;
        } else {
            recognizedText = "WARNING:could not initialize Tesseract data ...";
            return recognizedText;
        }
    }


    private static final String APP_ID = "20190903000331566";
    private static final String SECURITY_KEY = "RNuPntEEoO09xCaTA2HX";
    Runnable runnable_trans=new Runnable() {
        @Override
        public void run() {
            TransApi api = new TransApi(APP_ID, SECURITY_KEY);


            try {
//                JSONObject jsonObject = new JSONObject(api.getTransResult(recognizedText,"auto","zh"));
                JSONObject jsonObject = new JSONObject(api.getTransResult(ocr_result_google.getText().toString().replace('\n',' '),"auto","zh"));
                jsonObject = jsonObject.getJSONArray("trans_result").getJSONObject(0);
                final String result=jsonObject.getString("dst");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ocr_result_google.setText("");
                        ocr_result_google.setText(result);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };


    private void setOCResult(List<Map<String, String>> ocr_result_list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ocr_result_list.size(); i++) {
            if (ocr_result_list.get(i).get("words") != null) {
                stringBuilder.append(ocr_result_list.get(i).get("words") + '\n');
            }
        }
        ocr_result_tv.setText(stringBuilder.toString());
    }


}
