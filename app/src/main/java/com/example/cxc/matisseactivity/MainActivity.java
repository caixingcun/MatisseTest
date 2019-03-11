package com.example.cxc.matisseactivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private TextView mTvTakePhoto;
    private TextView mTvChoosePic;
    private ImageView mIv;
    private AppCompatActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        mTvTakePhoto = findViewById(R.id.take_photo);
        mTvChoosePic = findViewById(R.id.choose_pic);
        mIv = findViewById(R.id.iv);

        mTvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RxPermissions(mActivity).request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                takePhoto();

                            }
                        })
                ;
            }
        });
        mTvChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(1);
            }
        });
    }

    private File mPhotoFile;
    /**
     * 拍照请求码
     */
    public static final int REQUEST_TAKE_PHOTO = 1000;

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            mPhotoFile = null;
            try {
                mPhotoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mActivity, "com.example.cxc.matisseactivity.fileprovider", mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    /**
     * 选择图库请求码
     */
    public static final int REQUEST_PIC = 1001;

    private void chooseImage(int imageSize) {

        Matisse.from(mActivity)
                .choose(new HashSet<MimeType>() {{
                    add(MimeType.JPEG);
                    add(MimeType.PNG);
                }}) // 选择 mime 的类型
                .showSingleMediaType(true)
                .countable(true)
                .maxSelectable(imageSize) // 图片选择的最多数量
//                                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f) // 缩略图的比例
                .imageEngine(new MyGlideEngin()) // 使用的图片加载引擎
                .forResult(REQUEST_PIC); // 设置作为标记的请求码

        //选择视频库
//        Matisse.from(mActivity)
//                .choose(new HashSet<MimeType>() {{
//                    add(MimeType.MP4);
//                }}) // 选择 mime 的类型
//                .showSingleMediaType(true)
//                .countable(true)
//                .maxSelectable(1) // 图片选择的最多数量
////                                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
//                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                .thumbnailScale(0.85f) // 缩略图的比例
//                .imageEngine(new GlideEngine()) // 使用的图片加载引擎
//                .forResult(request_code); // 设置作为标记的请求码
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_TAKE_PHOTO) {
            String mIdentifyPath = mPhotoFile.getAbsolutePath();
            Glide.with(mActivity)
                    .load(mIdentifyPath)
                    .into(mIv);
            return;
        }

        if (requestCode == REQUEST_PIC) {
            String picPath = Matisse.obtainPathResult(data).get(0);
            Glide.with(mActivity)
                    .load(picPath)
                    .into(mIv);
            return;
        }

    }
}
