package com.frontcamera;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class OpenDoorListener implements Button.OnClickListener {
    private static OpenDoorListener _instance;

    // 通过 surfaceViewCallBack 来获取最近的图像帧
    private SurfaceViewCallback surfaceViewCallBack;
    private OpenDoorListener(){
        super();
    }

    public static OpenDoorListener getInstance(){
        if (_instance != null)
            return _instance;
        else{
            _instance = new OpenDoorListener();
            return _instance;
        }
    }

    public void rigsterSurfaceViewCallback(SurfaceViewCallback callback){
        surfaceViewCallBack = callback;
    }
    @Override
    public void onClick(View v){
        Toast toast = Toast.makeText(v.getContext(),"开门中……",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        ApiFaceSample.getInstance().faceverify(SurfaceViewCallback.getInstacne().mCurrentFrame64);
    }

}
