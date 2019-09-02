package com.nku.scandinavia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.database.Cursor;
import android.graphics.Matrix;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import pub.devrel.easypermissions.EasyPermissions;

import com.nku.scandinavia.helpers.Constants;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private Uri imageUri;
    private File cameraSavePath;//拍照照片名称
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_camera = findViewById(R.id.button_camera);
        Button button_gallery = findViewById(R.id.button_gallery);

        if (OpenCVLoader.initDebug()) {
            Toast.makeText(this, "openCv successfully loaded", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "openCv cannot be loaded", Toast.LENGTH_LONG).show();
        }
        button_camera.setOnClickListener(this);
        button_gallery.setOnClickListener(this);
        cameraSavePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + '/' + System.currentTimeMillis() + ".jpg");
        initAnimation();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_camera:
                if (EasyPermissions.hasPermissions(this, permissions)) {
                    //已经打开权限
                    Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
                    goCamera();
                } else {
                    //没有打开相关权限、申请权限
                    EasyPermissions.requestPermissions(this, "需要获取您的相册、照相使用权限", 1, permissions);
                }
                break;
            case R.id.button_gallery:
                goGallery();
                break;
        }
    }

    private void initAnimation() {
        ImageView imageView = (ImageView) findViewById(R.id.swing_play);
        imageView.setBackgroundResource(R.drawable.progress_animation);
        AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getBackground();
        frameAnimation.start();
    }

    // 调用相机
    private void goCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        imageUri = FileProvider.getUriForFile(MainActivity.this, "com.nku.scandinavia.file_provider", cameraSavePath);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        MainActivity.this.startActivityForResult(intent, 111);


    }

    // 调用相册
    private void goGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 222);
    }


    // 返回结果给EasyPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    //用户未同意权限

    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请同意相关权限，否则功能无法使用", Toast.LENGTH_SHORT).show();
    }

    //成功打开权限
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
        MainActivity.this.goCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String photoPath;
        int degree;
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 111:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "取消拍照", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    photoPath = valueOf(cameraSavePath);
                    degree = getBitmapDegree(photoPath);
                    loadImageUriToBitmap(imageUri, degree);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case 222:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Uri selectedImageUri = data.getData();
                    photoPath = getPath(selectedImageUri);
                    degree = getBitmapDegree(photoPath);
                    loadImageUriToBitmap(selectedImageUri, degree);
                    Log.e( "onActivityResult: ",photoPath );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void loadImageUriToBitmap(Uri selectedImageUri, int degree) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
            selectedImageBitmap = rotateBitmapByDegree(selectedImageBitmap, degree);
            Constants.selectedImageBitmap = selectedImageBitmap;
            // jump to ImageDisplayActivity
            Intent intent = new Intent(getApplicationContext(), ImageDisplayActivity.class);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * @param uri
     * @return
     */

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


}
