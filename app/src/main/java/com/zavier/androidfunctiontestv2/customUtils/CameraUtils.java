package com.zavier.androidfunctiontestv2.customUtils;

import android.content.Context;
import android.media.Image;
import android.util.Size;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CameraUtils {

    private static final String TAG = "CameraUtils";

    public static Size getOptimalSize(Size[] sizes, int width, int height){
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = height;
        int targetWidth = width;
        LogUtils.LogD(TAG, " getOptimalSize w: " + width + " h: " + height);

        if(width < height){
            targetRatio = (double) height / width;
            for (android.util.Size size : sizes) {
                double ratio = (double) size.getHeight() / size.getWidth();
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.getWidth() - targetWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getWidth() - targetWidth);
                    LogUtils.LogD(TAG, " getOptimalSize 111 w: " + optimalSize.getWidth() + " h: " + optimalSize.getHeight());
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (android.util.Size size : sizes) {
                    if (Math.abs(size.getWidth() - targetWidth) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.getWidth() - targetWidth);
                        LogUtils.LogD(TAG, " getOptimalSize 222w: " + optimalSize.getWidth() + " h: " + optimalSize.getHeight());
                    }
                }
            }
        }
        else
        {
            targetRatio = (double) width / height;
            for (android.util.Size size : sizes) {
                double ratio = (double) size.getWidth() / size.getHeight();
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                    LogUtils.LogD(TAG, " getOptimalSize 333w: " + optimalSize.getWidth() + " h: " + optimalSize.getHeight());
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (android.util.Size size : sizes) {
                    if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.getHeight() - targetHeight);
                        LogUtils.LogD(TAG,
                                " getOptimalSize4444 w: " + optimalSize.getWidth() + " h: " + optimalSize.getHeight());
                    }
                }
            }
        }
        return optimalSize;
    }

    public static class imageSaver implements Runnable {
        private Image mImage;
        private Context mContext;
        private String mFileString;
        public imageSaver(Image image, Context context, String string) {
            mImage = image;
            mContext = context;
            mFileString = string;
        }
        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            long ms = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.ENGLISH);
            String string = sdf.format(ms);

            File mBackImageFile = new File(mFileString + string + ".jpg");

            FileOutputStream fos = null;
            try {
                LogUtils.LogD(TAG,"imageSaver111 "+ mBackImageFile.toString());
                fos = new FileOutputStream(mBackImageFile);
                fos.write(data, 0 ,data.length);
                Toast.makeText(mContext,"图片保存在：" + mBackImageFile.toString(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                mImage = null;
                mBackImageFile = null;
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
