package com.zavier.androidfunctiontestv2.usbCommunication.hid;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OpenUsbHidPort {

    private static final String TAG = "OpenUsbHidPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private boolean mHidPortFlag =false;

    public OpenUsbHidPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        /* Check access permission */
//        if (!device.canRead() || !device.canWrite()) {
//            try {
//                /* Missing read/write permission, trying to chmod the file */
//                Process su;
//                su = Runtime.getRuntime().exec("/system/bin/su");
//                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
//                        + "exit\n";
//                su.getOutputStream().write(cmd.getBytes());
//                if ((su.waitFor() != 0) || !device.canRead()
//                        || !device.canWrite()) {
//                    throw new SecurityException();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new SecurityException();
//            }
//        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            throw new IOException();
        }

        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        mHidPortFlag = true;
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    public boolean getHidPortFlag() {
        return mHidPortFlag;
    }

    public void closeHidPort(){
        close();
    }
    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);
    private native void close();
    static {
        System.loadLibrary("usb_hid_port");
    }
}
