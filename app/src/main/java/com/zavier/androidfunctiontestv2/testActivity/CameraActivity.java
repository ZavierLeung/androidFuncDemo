package com.zavier.androidfunctiontestv2.testActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.LinearLayout;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.CameraUtils;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private final String TAG = "CamTestActivity";
    private int mCameraNum = 0;
    private static final int mTextureViewWidth = 320;
    private static final int mTextureViewHeight = 240;
    private TextViewTitleUtils mTitleUtile;
    private ExecutorService mThreadPool;

    private ArrayList<TextureView> mTextureView = new ArrayList<>();
    private ArrayList<String> mCameraId = new ArrayList<>();
    private ArrayList<Size> mPreviewSize = new ArrayList<>();
    private ArrayList<CaptureRequest.Builder> mCaptureRequestBuilder = new ArrayList<>();
    private ArrayList<CaptureRequest> mCaptureRequest = new ArrayList<>();
    private ArrayList<CameraCaptureSession> mCameraCaptureSession = new ArrayList<>();
    private ArrayList<ImageReader> mImageReader = new ArrayList<>();
    private ArrayList<Size> mCaptureSize = new ArrayList<>();
    private ArrayList<Handler> mCameraHandler = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        LogUtils.LogD(TAG, "onResume width: " + width + " height: " + height);
        LinearLayout linearLayout = findViewById(R.id.camera_test_llayout);
        if(width > height){
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            linearLayout.setOrientation(LinearLayout.VERTICAL);
        }
        initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mThreadPool != null){
            mThreadPool.shutdown();
        }
        clearArray();
    }

    private void clearArray(){
        for (int i = 0; i < mImageReader.size(); i++){
            mImageReader.get(i).close();
            mCameraCaptureSession.get(i).close();
        }
        for (int i = 0; i < mCameraCaptureSession.size(); i++){
            mCameraCaptureSession.get(i).close();
        }
        mImageReader.clear();
        mCameraCaptureSession.clear();
        mTextureView.clear();
        mCameraId.clear();
        mPreviewSize.clear();
        mCaptureSize.clear();
        mCaptureRequestBuilder.clear();
        mCaptureRequest.clear();
        mCameraHandler.clear();
    }

    private void initView() {
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(CameraActivity.this, false, false);
        mTitleUtile.setTitle(R.string.main_func_camera);
    }

    private void initCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取所有摄像头
            if (manager != null) {
                mCameraId.addAll(Arrays.asList(manager.getCameraIdList()));
            }
            mCameraNum = mCameraId.size();
            LogUtils.LogD(TAG, "initCamera CameraNum:" + mCameraNum);
            if(mCameraNum > 0){
                mThreadPool = Executors.newFixedThreadPool(mCameraNum);
                for (int i = 0; i < mCameraNum; i++) {
                    initTextureView(i);
                }
            } else {
                mTitleUtile.setResultVisibility(View.VISIBLE);
                mTitleUtile.setResult(R.string.camera_test_result_fail);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initTextureView(final int cameraId) {
        mTextureView.add(cameraId, new TextureView(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mTextureViewWidth, mTextureViewHeight);
        lp.setMargins(80, 10, 10, 10);
        mTextureView.get(cameraId).setLayoutParams(lp);
        ((LinearLayout) findViewById(R.id.camera_test_llayout)).addView(mTextureView.get(cameraId));
        final int w = mTextureView.get(cameraId).getWidth();
        final int h = mTextureView.get(cameraId).getHeight();
        final int num = cameraId;

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (mTextureView.get(cameraId).isAvailable()) {
                    openCamera(w, h, num);
                } else {
                    mTextureView.get(cameraId).setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                            //当SurefaceTexture可用的时候，设置相机参数并打开相机
                            openCamera(i, i1, num);
                        }

                        @Override
                        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
                        }

                        @Override
                        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                            return false;
                        }

                        @Override
                        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                        }
                    });
                }
            }
        });
    }

    private void openCamera(int w, int h, int num) {
        //获取摄像头的管理者CameraManager
        LogUtils.LogD(TAG, "openCamera enter");
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            if (manager != null) {
                LogUtils.LogD(TAG, "openCamera cameraId: " + mCameraId.get(num));
                if (mCameraId.get(num) == null) {
                    LogUtils.LogE(TAG, "cameraId is null");
                    return;
                }
                characteristics = manager.getCameraCharacteristics(mCameraId.get(num));
                int sensor_orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize.add(num, CameraUtils.getOptimalSize(map.getOutputSizes(SurfaceTexture.class), w, h));

                mCaptureSize.add(num, Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                }));

                //旋转预览的方向
                configureTransform(mPreviewSize.get(num).getWidth(), mPreviewSize.get(num).getHeight(), num, sensor_orientation);

                //判断有无权限打开摄像头
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //打开相机，第一个参数指示打开哪个摄像头，
                // 第二个参数stateCallback为相机的状态回调接口，
                // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
                manager.openCamera(mCameraId.get(num), new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice cameraDevice) {
                        //开启预览
                        for (int i = 0; i < mCameraNum; i++) {
                            if(cameraDevice.getId().equals(mCameraId.get(i))){
                                LogUtils.LogD(TAG, "onOpened mCameraId : " + mCameraId.get(i) + " num: " + i);
                                startPreview(cameraDevice, i);
                            }
                        }
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    }
                    @Override
                    public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    }
                }, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight, int num, int orientation){
        if(mTextureView.get(num) == null){
            LogUtils.LogE(TAG, "configureTransform mTextureView is null, num : " + num);
            return;
        }
        LogUtils.LogD(TAG, "configureTransform rotation : " + orientation);
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mCaptureSize.get(num).getHeight(), mCaptureSize.get(num).getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (90 == orientation || 270 == orientation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mCaptureSize.get(num).getHeight(),
                    (float) viewWidth / mCaptureSize.get(num).getWidth());
            LogUtils.LogD(TAG, "configureTransform centerX : " + centerX + " centerY : " + centerY + " scale: " + scale);
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(orientation - 180, centerX, centerY);
        } else if (180 == orientation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.get(num).setTransform(matrix);
    }

    private void startPreview(CameraDevice cameraDevice, final int num){
        SurfaceTexture surfaceTexture = mTextureView.get(num).getSurfaceTexture();
        //设置TextureView的缓冲区大小
        surfaceTexture.setDefaultBufferSize(mPreviewSize.get(num).getWidth(), mPreviewSize.get(num).getHeight());
        //获取Surface显示预览数据
        Surface surface = new Surface(surfaceTexture);
        LogUtils.LogD(TAG, "startPreview w: " + mPreviewSize.get(num).getWidth() + " h: " +  mPreviewSize.get(num).getHeight());

        try {
            //创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            mCaptureRequestBuilder.add(num, cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW));
            //设置Surface作为预览数据的显示界面
            mCaptureRequestBuilder.get(num).addTarget(surface);
            //Camera2中并没有Camera1中的PreviewCallback接口，那怎么实现获取预览帧数据呢？
            // 答案就是使用ImageReader间接实现
            setupImageReader(num);
            //获取ImageReader的Surface
            Surface imageReaderSurface = mImageReader.get(num).getSurface();
            //CaptureRequest添加imageReaderSurface，不加的话就会导致ImageReader的onImageAvailable()方法不会回调
            //本项目只需要预览，不用抓图功能，所以暂不回调onImageAvailable()
            //mCaptureRequestBackBuilder.addTarget(imageReaderSurface);

            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，
            // 第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，
            // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            //创建CaptureSession时加上imageReaderSurface，如下，这样预览数据就会同时输出到previewSurface和imageReaderSurface了
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReaderSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    LogUtils.LogD(TAG,"  onConfigured");
                    //创建捕获请求
                    mCaptureRequest.add(num, mCaptureRequestBuilder.get(num).build());
                    mCameraCaptureSession.add(num, cameraCaptureSession);
                    //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                    try {
                        mCameraCaptureSession.get(num).setRepeatingRequest(mCaptureRequest.get(num),
                                null, null/*mCameraHandler.get(num)*/);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    LogUtils.LogD(TAG,"  onConfigureFailed");
                }
            }, null/*mCameraHandler.get(num)*/);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupImageReader(final int num) {
        Log.d(TAG, "mCaptureSize w: " + mCaptureSize.get(num).getWidth() + " h: " +  mCaptureSize.get(num).getHeight());
        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据，本例的2代表ImageReader中最多可以获取1帧图像流
        mImageReader.add(num, ImageReader.newInstance(mCaptureSize.get(num).getWidth(),
                mCaptureSize.get(num).getHeight(), ImageFormat.JPEG, 1));
        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.get(num).setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                String string = Environment.getExternalStorageDirectory() + "/Pictures/MyCamera/Back_";
                mCameraHandler.get(num).post(new CameraUtils.imageSaver(image, CameraActivity.this, string));
            }
        }, null);
    }
}
