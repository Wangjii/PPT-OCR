package com.nku.scandinavia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import org.opencv.android.OpenCVLoader;

import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private Uri imageUri;
    private File cameraSavePath;//拍照照片名称
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


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

        cameraSavePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+'/'+System.currentTimeMillis() + ".jpg");

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

    // 调用相机
    private void goCamera() {
        Uri uri;
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
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 111:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "取消拍照", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    photoPath = String.valueOf(cameraSavePath);
                    Log.d("拍照返回图片路径:", photoPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case 222:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    imageUri = data.getData();
//                    imgShow.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
