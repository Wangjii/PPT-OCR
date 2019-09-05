package com.nku.scandinavia;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.button.MaterialButton;

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
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.bilateralFilter;

public class ImageProcessActivity extends AppCompatActivity {
    ImageView imageView;
    String apiPath = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    EditText ocr_result_tv;
    EditText ocr_result_trans;
    TextView ocr_result_google;
    TessBaseAPI baseApi = new TessBaseAPI();

    private MaterialButton btn_denoise, btn_deblurr, btn_sharpen, btn_original;
    private HorizontalScrollView roll_option;
    private boolean isVisible = false;
    private boolean mark = false;


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
        //设置按钮不可见
        btn_denoise = findViewById(R.id.button_denoise);
        btn_denoise.setVisibility(View.GONE);
        btn_denoise.setOnClickListener(this.btn_denoiseClick);
        btn_deblurr = findViewById(R.id.button_deblurr);
        btn_deblurr.setVisibility(View.GONE);
        btn_deblurr.setOnClickListener(this.btn_deblurrClick);
        btn_sharpen = findViewById(R.id.button_sharpen);
        btn_sharpen.setVisibility(View.GONE);
        btn_sharpen.setOnClickListener(this.btn_sharpenClick);
        btn_original = findViewById(R.id.button_original);
        btn_original.setVisibility(View.GONE);
        btn_original.setOnClickListener(this.btn_originalClick);
        //
        roll_option = findViewById(R.id.roll_process_option);
        btn_sharpen.setVisibility(View.GONE);
        imageView.setImageBitmap(Constants.croppedImageBitmap);
        ocr_result_tv = findViewById(R.id.ocr_result);
        ocr_result_google = findViewById(R.id.googel_ocr_result);

        ocr_result_trans = findViewById(R.id.trans_result);
        ocr_result_trans.setVisibility(View.GONE);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(
                    @NonNull
                            MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.buttom_enhance:

                        if (isVisible) {
                            btn_denoise.setVisibility(View.GONE);
                            btn_deblurr.setVisibility(View.GONE);
                            btn_sharpen.setVisibility(View.GONE);
                            btn_original.setVisibility(View.GONE);
                            roll_option.setVisibility(View.GONE);
                            isVisible = false;
                        } else {
                            btn_denoise.setVisibility(View.VISIBLE);
                            btn_deblurr.setVisibility(View.VISIBLE);
                            btn_sharpen.setVisibility(View.VISIBLE);
                            btn_original.setVisibility(View.VISIBLE);
                            roll_option.setVisibility(View.VISIBLE);
                            isVisible = true;
                        }

                        break;
                    case R.id.buttom_ocr:
                        new Thread(runnable).start();
                        ocr_result_tv.setText("Loading...");
                        break;
                    case R.id.buttom_trans:
                        try {
                            if (isVisible) {
                                ocr_result_trans.setVisibility(View.GONE);
                                isVisible = false;
                            } else {
                                Thread.sleep(10);
                                ocr_result_trans.setVisibility(View.VISIBLE);
                                ocr_result_trans.setText("正在翻译……");
                                new Thread(runnable_trans).start();
                                isVisible = true;
                            }

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
        Bitmap image;
        @Override
        public void run() {
            if(mark){
                image = Constants.processedImageBitmap;
            }else {
                image = Constants.croppedImageBitmap;
            }
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
                            ocr_result_google.setText("null");
                        } else {
                            ocr_result_google.setText("");
                            ocr_result_google.append(result.replace('\n', ' '));
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
            recognizedText = recognizedText.replace('\n', ' ');
            return recognizedText;
        } else {
            recognizedText = "WARNING:could not initialize Tesseract data ...";
            return recognizedText;
        }
    }


    private static final String APP_ID = "20190903000331566";
    private static final String SECURITY_KEY = "RNuPntEEoO09xCaTA2HX";

    Runnable runnable_trans = new Runnable() {
        @Override
        public void run() {
            TransApi api = new TransApi(APP_ID, SECURITY_KEY);
            try {
//                JSONObject jsonObject = new JSONObject(api.getTransResult(recognizedText,"auto","zh"));
                JSONObject jsonObject = new JSONObject(api.getTransResult(ocr_result_tv.getText().toString().replace('\n', ' '), "auto", "zh"));
                jsonObject = jsonObject.getJSONArray("trans_result").getJSONObject(0);
                final String result = jsonObject.getString("dst");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ocr_result_trans.setText("");
                        ocr_result_trans.setText(result);
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


    private View.OnClickListener btn_denoiseClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Mat src = new Mat();
            if(mark){
                Utils.bitmapToMat(Constants.processedImageBitmap,src);
            }else{
                Utils.bitmapToMat(Constants.croppedImageBitmap, src);
            }
            Mat dst = new Mat();
            GaussianBlur(src, dst, new Size(5,5), 3, 3);
            Bitmap img_denoise = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, img_denoise);
            imageView.setImageBitmap(img_denoise);
            //储存
            Constants.processedImageBitmap = img_denoise;
            Toast.makeText(getApplicationContext(), "去除噪声", Toast.LENGTH_SHORT).show();
            src.release();
            dst.release();
            mark = true;
        }
    };

    private View.OnClickListener btn_deblurrClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    private View.OnClickListener btn_sharpenClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Mat src = new Mat();
            if(mark){
                Utils.bitmapToMat(Constants.processedImageBitmap,src);
            }else{
                Utils.bitmapToMat(Constants.croppedImageBitmap, src);
            }
            Mat dst = new Mat();
            //锐化算子
            Mat k = new Mat(3, 3, CvType.CV_32FC1);
            float[] data = new float[]{0,-1,0,-1,5,-1,0,-1,0};
            k.put(0,0,data);
            //锐化
            Imgproc.filter2D(src, dst, -1, k);
            //
            Bitmap img_sharpen = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, img_sharpen);
            imageView.setImageBitmap(img_sharpen);
            //储存
            Constants.processedImageBitmap = img_sharpen;
            Toast.makeText(getApplicationContext(), "锐化", Toast.LENGTH_SHORT).show();
            src.release();
            dst.release();
            mark = true;
        }
    };

    private View.OnClickListener btn_originalClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            imageView.setImageBitmap(Constants.croppedImageBitmap);
            Toast.makeText(getApplicationContext(), "返回原图", Toast.LENGTH_SHORT).show();
            mark = false;
        }
    };

}
