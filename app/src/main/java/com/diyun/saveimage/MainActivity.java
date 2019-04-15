package com.diyun.saveimage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String imagePath = "http://klky.diyunkeji.com/Uploads/avatar/5cad92197c3b5.jpg";
    private Button button;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btn);
        imageView = findViewById(R.id.image);
        Glide.with(this).load(imagePath).into(imageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageCameraPermission();
            }
        });
    }

    /**
     * 获取权限 Permission
     */
    public void getImageCameraPermission() {
        //判断版本
        if (Build.VERSION.SDK_INT >= 23) {
            //检查权限是否被授予：
            int hasExternalPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasExternalPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
            } else {
                Glide.get(MainActivity.this).clearMemory();
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                Glide.with(MainActivity.this).load(imagePath).apply(requestOptions).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        saveImageToGallery(MainActivity.this, drawable2Bitmap(resource));
                    }
                });
            }
        } else {
            Glide.get(MainActivity.this).clearMemory();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.centerCrop();
            Glide.with(MainActivity.this).load(imagePath).apply(requestOptions).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    saveImageToGallery(MainActivity.this, drawable2Bitmap(resource));
                }
            });
        }
    }


    Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                               drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }


    public static String saveImageToGallery(Context context, Bitmap bmp) {
        Log.d(TAG, "saveImageToGallery: " + bmp);
        String imgpath = Environment.getExternalStorageDirectory().toString() + "/HeBeiNM/";
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory().toString(), "HeBeiNM");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
//        String fileName = System.currentTimeMillis() + "sc.jpg";
        String fileName = System.currentTimeMillis() + "";
        imgpath += fileName;
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imgpath)));
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
        return imgpath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //就像onActivityResult一样这个地方就是判断你是从哪来的。
            case 12:
                boolean permissionsIsAgree = false;// 许可
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permissionsIsAgree = true;
                }
                if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && permissionsIsAgree) {
                    getImageCameraPermission(); // 许可
                } else {
                    Toast.makeText(MainActivity.this, "很遗憾你把相机权限禁用了。", Toast.LENGTH_SHORT).show();
                }
                break;
            case 11:
            case 10:
                if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getImageCameraPermission();// 许可
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "很遗憾你把相机权限禁用了。", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageCameraPermission();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "很遗憾你把相册权限禁用了", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
