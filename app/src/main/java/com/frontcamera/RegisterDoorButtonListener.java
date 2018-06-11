package com.frontcamera;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RegisterDoorButtonListener implements Button.OnClickListener {
    private static RegisterDoorButtonListener _instance;

    // 通过 surfaceViewCallBack 来获取最近的图像帧
    private SurfaceViewCallback surfaceViewCallBack;
    private RegisterDoorButtonListener(){
        super();
    }

    public static RegisterDoorButtonListener getInstance(){
        if (_instance == null)
            _instance = new RegisterDoorButtonListener();

        return _instance;
    }

    public void rigsterSurfaceViewCallback(SurfaceViewCallback callback){
        surfaceViewCallBack = callback;
    }
    @Override
    public void onClick(View v){
        Toast toast = Toast.makeText(v.getContext(),"注册中……",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        ApiFaceSample.getInstance().register(SurfaceViewCallback.getInstacne().mCurrentFrame64,"anon");
    }


}
