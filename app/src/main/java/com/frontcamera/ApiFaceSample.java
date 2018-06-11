package com.frontcamera;

import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.FaceVerifyRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ApiFaceSample {

    private static final double PASS_SCORE = 85;
    private static final double ALIVE_CHECK_THRESHOLD = 0.95;

    private static final int PIC_ALIVE = 755;
    private static final int PIC_MATCH = 427;
    private static final int FACE_REGISTER = 800;
    //设置APPID/AK/SK
    public static final String APP_ID = SecretConfig.APP_ID;
    public static final String API_KEY = SecretConfig.API_KEY;
    public static final String SECRET_KEY = SecretConfig.SECRET_KEY;

    public AipFace mClient;

    private static ApiFaceSample _instance;
    Handler mHandler;

    private ApiFaceSample() {
        mClient = new AipFace(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        mClient.setConnectionTimeoutInMillis(2000);
        mClient.setSocketTimeoutInMillis(60000);

        //// 可选：设置代理服务器地址, http和socket二选一，或者均不设置
        //mClient.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
        //mClient.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

//        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
//        // 也可以直接通过jvm启动参数设置此环境变量
//        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    JSONObject json = (JSONObject)msg.obj;
                    switch (msg.what) {
                        case PIC_ALIVE:

                            int ret = json.getInt("error_code");
                            if(ret == 0) {
                                double live_score = json.getJSONObject("result").getDouble("face_liveness");
                                if(live_score > ALIVE_CHECK_THRESHOLD){
                                    Toast toast = Toast.makeText(SurfaceViewCallback.getInstacne().context,"活体识别成功，人脸识别开门中……"  + Double.toString(live_score),Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    ApiFaceSample.getInstance().match(SurfaceViewCallback.getInstacne().mCurrentFrame64,"anon");
                                }
                                else{
                                    Toast toast = Toast.makeText(SurfaceViewCallback.getInstacne().context,"活体识别失败，请再次尝试开门" + Double.toString(live_score),Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            }else {
                                Toast toast = Toast.makeText(SurfaceViewCallback.getInstacne().context,"活体识别失败，请再次尝试开门",Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            break;
                        case PIC_MATCH:
                            JSONArray jsonArray = json.getJSONObject("result").getJSONArray("user_list");
                            int size = jsonArray.length();
                            if(size == 0){
                                Toast toast = Toast.makeText(SurfaceViewCallback.getInstacne().context,"人脸识别失败0，请再次尝试开门",Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }else{
                                double score =  ((JSONObject)jsonArray.get(0)).getDouble("score");
                                if(score > PASS_SCORE){
                                    Toast toast = Toast.makeText(SurfaceViewCallback.getInstacne().context,"人脸识别成功，门已开",Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }else{
                                    Toast toast = Toast.makeText(SurfaceViewCallback.getInstacne().context,"人脸识别失败1，请再次尝试开门" + Double.toString(score) ,Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            }
                            break;
                    }
                }catch (Exception e){

                }
            }
        };
    }

    public static ApiFaceSample getInstance() {
        if (_instance == null)
            _instance = new ApiFaceSample();

        return _instance;
    }

    public void faceverify(final String base64) {

        new Thread(new Runnable(){
            @Override
            public void run() {
                String image = base64;
                FaceVerifyRequest req = new FaceVerifyRequest(image, "BASE64");
                ArrayList<FaceVerifyRequest> list = new ArrayList<FaceVerifyRequest>();
                list.add(req);
                JSONObject res = mClient.faceverify(list);
                System.out.println(res.toString());

                Message msg = Message.obtain();
                msg.what = PIC_ALIVE;
                msg.obj = res;
                mHandler.sendMessage(msg);
            }
        }).start();
    }
    public void register(final String base64, final String user_id){


        new Thread(new Runnable(){
            @Override
            public void run() {

                // debug
                // 传入可选参数调用接口
                HashMap<String, String> options0 = new HashMap<String, String>();
                String groupId0 = "facegroup";
                String userId0 = user_id;
                // 删除用户
                JSONObject res0 = mClient.deleteUser(groupId0, userId0, options0);


                // 传入可选参数调用接口
                HashMap<String, String> options1 = new HashMap<String, String>();
                // 用户信息查询
                JSONObject res1 = mClient.getUser(userId0, groupId0, options1);


                // 获取用户人脸列表
                JSONObject res2 = mClient.faceGetlist(userId0, groupId0, options1);

                // 获取用户列表
                JSONObject res3 = mClient.getGroupUsers(groupId0, options1);
                // debug


                // 传入可选参数调用接口
                HashMap<String, String> options = new HashMap<String, String>();
                options.put("user_info", "user's info");
                options.put("quality_control", "NORMAL");
                options.put("liveness_control", "LOW");

                String image = base64;
                String imageType = "BASE64";
                String groupId = "facegroup";
                String userId = user_id;

                // 人脸注册
                JSONObject res = mClient.addUser(image, imageType, groupId, userId, options);

                Message msg = Message.obtain();
                msg.what = FACE_REGISTER;
                msg.obj = res;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    public void match(final String base64, final String userId){
        new Thread(new Runnable(){
            @Override
            public void run() {
                // 传入可选参数调用接口
                HashMap<String, String> options = new HashMap<String, String>();
                options.put("quality_control", "NORMAL");
                options.put("liveness_control", "LOW");
                options.put("user_id", userId);
                options.put("max_user_num", "1");

                String image = base64;
                String imageType = "BASE64";
                String groupIdList = "facegroup";

                // 人脸搜索
                JSONObject res = mClient.search(image, imageType, groupIdList, options);

                Message msg = Message.obtain();
                msg.what = PIC_MATCH;
                msg.obj = res;
                mHandler.sendMessage(msg);
            }
        }).start();
    }
}
