package com.dgonlam.library;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DgonlamCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final int IDCARD_PERSON = 1;
    public static final int IDCARD_ELBEM = 2;
    public static final int RESULT_ERROR = 110;
    public static final String mode_idCard = "mode_idCard";
    public static final String mode = "mode";
    public static final String mode_other = "mode_other";
    public static final String mode_idCard_person = "mode_idCard_person";
    public static final String mode_idCard_elbem = "mode_idCard_elbem";
    public static final String strFilePath = "filePath";
    public static final String result = "result";
    public static final String shade_pic = "shade_pic";
    public static final String btn_pic = "btn_pic";

    private boolean isPreview = false;
    private SurfaceView mPreviewSV = null; //预览SurfaceView
    private SurfaceHolder mySurfaceHolder = null;
    private ImageView mPhotoImgBtn = null;
    private Camera myCamera = null;
    private Bitmap mBitmap = null;
    private AutoFocusCallback myAutoFocusCallback = null;
    private String filePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        setContentView(R.layout.activity_dgonlam_camera);
        initParams();
        //初始化SurfaceVie
        mPreviewSV = (SurfaceView) findViewById(R.id.previewSV);
        mySurfaceHolder = mPreviewSV.getHolder();
        mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);//translucent半透明 transparent透明
        mySurfaceHolder.addCallback(this);
        mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mPhotoImgBtn = (ImageView) findViewById(R.id.dgonlam_camera_iv_takePhoto);
        mPhotoImgBtn.setOnClickListener(new PhotoOnClickListener());
        findViewById(R.id.dgonlam_camera_tv_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.dgonlam_camera_iv_shadePic).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect focusRect = DgonlamDensiityUtil.calculateTapArea(myCamera, event.getRawX(), event.getRawY(), 1f);
                Rect meteringRect = DgonlamDensiityUtil.calculateTapArea(myCamera,event.getRawX(), event.getRawY(), 1.5f);

                Camera.Parameters parameters = myCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                if (focusRect.isEmpty()){
                    focusRect.bottom = 674;
                    focusRect.left =-594;
                    focusRect.top = 374;
                    focusRect.right=-294;
                }
                if (meteringRect.isEmpty()){
                    meteringRect.bottom = 749;
                    meteringRect.left =-669;
                    meteringRect.top = 299;
                    meteringRect.right=-219;
                }

                if (parameters.getMaxNumFocusAreas() > 0) {
                    List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                    focusAreas.add(new Camera.Area(focusRect, 1000));

                    parameters.setFocusAreas(focusAreas);
                }

                if (parameters.getMaxNumMeteringAreas() > 0) {
                    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                    meteringAreas.add(new Camera.Area(meteringRect, 1000));

                    parameters.setMeteringAreas(meteringAreas);
                }
                myCamera.setParameters(parameters);
                myCamera.autoFocus(null);
                return true;
            }
        });
    }

    private void initParams() {
        String strResult = checkParams();
        if (strResult.equals("")) {
            filePath = getIntent().getExtras().getString(strFilePath);
            if (getIntent().getExtras().getString(mode).equals(mode_idCard)) {
                if (getIntent().getExtras().getString(mode_idCard).equals(mode_idCard_person)){

                }else if (getIntent().getExtras().getString(mode_idCard).equals(mode_idCard_elbem)){
                    ((ImageView)findViewById(R.id.dgonlam_camera_iv_shadePic)).setBackgroundResource(R.drawable.dgonlam_ic_idcard_elbem);
                }
            }else if (getIntent().getExtras().getString(mode).equals(mode_other)){
                ((ImageView)findViewById(R.id.dgonlam_camera_iv_shadePic)).setBackgroundResource(getIntent().getExtras().getInt(shade_pic,R.drawable.dgonlam_ic_idcard_person));
                ((ImageView)findViewById(R.id.dgonlam_camera_iv_takePhoto)).setImageResource(getIntent().getExtras().getInt(btn_pic, R.drawable.dgonlam_ic_camera_white_72));
            }
        }else {
            Intent intent = new Intent();
            intent.putExtra(result,strResult);
            setResult(RESULT_ERROR,intent);
            finish();
        }
    }

    private String checkParams() {
        if (getIntent().getExtras().containsKey(strFilePath))
            if (getIntent().getExtras().containsKey(mode))
                return "";
            else
                return "缺少mode";
        else
            return "缺少filePath";
    }


    /*下面三个是SurfaceHolder.Callback创建的回调函数*/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    // 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
    {
        //使用最佳比例配置重启相机
        try {
            myCamera.setPreviewDisplay(mySurfaceHolder);
            final Camera.Parameters parameters = myCamera.getParameters();
            final Camera.Size size = getBestPreviewSize(width, height);
            parameters.setPreviewSize(size.width, size.height);
            myCamera.setParameters(parameters);
            myCamera.startPreview();
        } catch (Exception e) {

        }
        // TODO Auto-generated method stub
        myCamera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    myCamera.cancelAutoFocus();
                    doAutoFoces();
                }
            }
        });
    }

    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;
        final Camera.Parameters p = myCamera.getParameters();
        //特别注意此处需要规定rate的比是大的比小的，不然有可能出现rate = height/width，但是后面遍历的时候，current_rate = width/height,所以我们限定都为大的比小的。
        float rate = (float) Math.max(width, height)/ (float)Math.min(width, height);
        float tmp_diff;
        float min_diff = -1f;
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            float current_rate = (float) Math.max(size.width, size.height)/ (float)Math.min(size.width, size.height);
            tmp_diff = Math.abs(current_rate-rate);
            if( min_diff < 0){
                min_diff = tmp_diff ;
                result = size;
            }
            if( tmp_diff < min_diff ){
                min_diff = tmp_diff ;
                result = size;
            }
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    // SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用。
    {
        // TODO Auto-generated method stub
        myCamera = Camera.open();
        try {
            myCamera.setPreviewDisplay(mySurfaceHolder);
            initCamera();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (null != myCamera) {
                myCamera.release();
                myCamera = null;
            }
            e.printStackTrace();
        }


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    //销毁时被调用
    {
        // TODO Auto-generated method stub
        if (null != myCamera) {
            myCamera.setPreviewCallback(null); /*在启动PreviewCallback时这个必须在前不然退出出错。
            这里实际上注释掉也没关系*/

            myCamera.stopPreview();
            isPreview = false;
            myCamera.release();
            myCamera = null;
        }

    }

    private Camera.Parameters myParam;

    private void doAutoFoces() {
        myParam = myCamera.getParameters();
        myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        myCamera.setParameters(myParam);
        myCamera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    if (!Build.MODEL.equals("KORIDY H30")) {
                        myParam = camera.getParameters();
                        myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                        camera.setParameters(myParam);
                    } else {
                        myParam = camera.getParameters();
                        myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        camera.setParameters(myParam);
                    }
                }
            }
        });
        SensorControler.getInstance(this, new SensorFocusListener() {
            @Override
            public void onFocusChanged() {
                Log.d("long","sesor changed");
                doAutoFoces();
            }
        });
    }

    //初始化相机
    public void initCamera() {
        if (isPreview) {
            myCamera.stopPreview();
        }
        if (null != myCamera) {
            Camera.Parameters myParam = myCamera.getParameters();
            List<Camera.Size> list = myParam.getSupportedPictureSizes();
            //          //查询屏幕的宽和高

            myParam.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式

            //设置大小和方向等参数
            myParam.setPictureSize(1920, 1080);
            myCamera.setDisplayOrientation(90);
//            myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            if (!Build.MODEL.equals("KORIDY H30")) {
                myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
            } else {
                myParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            myCamera.setParameters(myParam);
            myCamera.startPreview();
            myCamera.cancelAutoFocus();
            isPreview = true;
        }
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    ShutterCallback myShutterCallback = new ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {

        public void onShutter() {
            // TODO Auto-generated method stub

        }
    };
    PictureCallback myRawCallback = new PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

        }
    };
    PictureCallback myJpegCallback = new PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if (null != data) {
                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                myCamera.stopPreview();
                isPreview = false;
            }
            //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。图片竟然不能旋转了，故这里要旋转下
            Matrix matrix = new Matrix();
            matrix.postRotate((float) 90.0);
            Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
            //保存图片到sdcard

            if (null != rotaBitmap) {
                onSuccess(rotaBitmap);
            }

            //再次进入预览
            myCamera.startPreview();
            isPreview = true;
        }
    };

    //拍照按键的监听
    public class PhotoOnClickListener implements OnClickListener {
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (isPreview && myCamera != null) {
                myCamera.takePicture(myShutterCallback, null, myJpegCallback);
            }

        }

    }

    /*给定一个Bitmap，进行保存*/
    public void onSuccess(Bitmap bm) {
        try {
            File file = new File(filePath);
            if (!file.exists())
                file.createNewFile();
            DgonlamDensiityUtil dgonlamDensiityUtil = new DgonlamDensiityUtil(this);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            Bitmap cutBitmap = Bitmap.createBitmap(bm, dgonlamDensiityUtil.dp2px(this, 20), dgonlamDensiityUtil.dp2px(this, 20), bm.getWidth() - dgonlamDensiityUtil.dp2px(this, 40)
                    , bm.getHeight() - dgonlamDensiityUtil.dp2px(this, 100));
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            Bitmap roateBitmap = Bitmap.createBitmap(cutBitmap, 0, 0, cutBitmap.getWidth(), cutBitmap.getHeight(), matrix, false);
            roateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

    }


    @Override
    public void onBackPressed()
    //无意中按返回键时要释放内存
    {
        // TODO Auto-generated method stub
        super.onBackPressed();
        this.finish();
    }
}
