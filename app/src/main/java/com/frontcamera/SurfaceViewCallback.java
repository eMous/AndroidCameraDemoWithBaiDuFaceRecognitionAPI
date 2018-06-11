package com.frontcamera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Created by zhousong on 2016/9/19.
 * 相机界面SurfaceView的回调类
 */
public final class SurfaceViewCallback implements android.view.SurfaceHolder.Callback, Camera.PreviewCallback {

    // 每隔多少毫秒缓存一张照片
    static final int  mInterval = 300;
    // 一共缓存多少张照片
    static final int mPhotoCount = 5;

    static SurfaceViewCallback _instance;
    Long  mLastTime = new Long(0);

    public Queue<String> getmFramesBase64() {
        return mFramesBase64;
    }

    public void setmFramesBase64(Queue<String> mFramesBase64) {
        this.mFramesBase64 = mFramesBase64;
    }

    public String getmCurrentFrame64() {
        return mCurrentFrame64;
    }

    public void setmCurrentFrame64(String mCurrentFrame64) {
        this.mCurrentFrame64 = mCurrentFrame64;
    }

    Queue<String> mFramesBase64 = new LinkedList<>();
    String mCurrentFrame64;



    Context context;
    static final String TAG = "Camera";
    FrontCamera mFrontCamera = new FrontCamera();
    boolean previewing = mFrontCamera.getPreviewing();
    Camera mCamera;
    FaceTask mFaceTask;

    public static SurfaceViewCallback getInstacne() {
        if (_instance != null)
            return _instance;
        else {
            _instance = new SurfaceViewCallback();
            return _instance;
        }
    }

    private SurfaceViewCallback() {
        super();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        if (previewing) {
            mCamera.stopPreview();
            Log.i(TAG, "停止预览");
        }

        try {
            mCamera.setPreviewDisplay(arg0);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            Log.i(TAG, "开始预览");

            OpenDoorListener.getInstance().rigsterSurfaceViewCallback(this);
            //调用旋转屏幕时自适应
            //setCameraDisplayOrientation(MainActivity.this, mCurrentCamIndex, mCamera);
        } catch (Exception e) {
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //初始化前置摄像头
        mFrontCamera.setCamera(mCamera);
        mCamera = mFrontCamera.initCamera();
        mCamera.setPreviewCallback(this);
        //适配竖排固定角度
        Log.i(TAG, "context: " + context.toString());
        Log.i(TAG, "mFrontCamera: " + mFrontCamera.toString());
        Log.i(TAG, "mCamera: " + mCamera.toString());
        FrontCamera.setCameraDisplayOrientation((Activity) context, mFrontCamera.getCurrentCamIndex(), mCamera);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mFrontCamera.StopCamera(mCamera);
    }

    /**
     * 相机实时数据的回调
     *
     * @param data   相机获取的数据，格式是YUV
     * @param camera 相应相机的对象
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {



        if (mFaceTask != null) {
            switch (mFaceTask.getStatus()) {
                case RUNNING:
                    return;
                case PENDING:
                    mFaceTask.cancel(false);
                    break;
            }

        }
        mFaceTask = new FaceTask(data, camera);
        mFaceTask.execute((Void) null);
        //Log.i(TAG, "onPreviewFrame: 启动了Task");

    }
    /**
     * 把当前帧选择性的加入到存储容器里
     *
     * @param data64   相机获取的数据
     */
    public void addFrameToQueue(String data64){
        Long nowTime = System.currentTimeMillis();
        if (nowTime - mLastTime > mInterval){
            if (mFramesBase64.size() >= mPhotoCount)
                mFramesBase64.remove();

            mFramesBase64.add(data64);
            mLastTime = nowTime;
        }
    }


//
//    private void alertText(final String title, final String message) {
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                alertDialog.setTitle(title)
//                        .setMessage(message)
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                Intent intent = new Intent();
//                                intent.putExtra("bestimage_path", bestImagePath);
//                                setResult(Activity.RESULT_OK, intent);
//                                finish();
//                            }
//                        })
//                        .show();
//            }
//        });
//    }


    /**
     * cai hua shuai
     * bitmap转为base64
     * @param bitmap
     * @return
     */
     public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}