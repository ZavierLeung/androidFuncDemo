/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.zavier.androidfunctiontestv2.SerialPortTest;

import java.io.File;
import java.io.IOException;


public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private boolean mOpenFileFlag = false;
	private String mFilePath;

	public SerialPort(File device) throws SecurityException, IOException {

		/* Check access permission */
//		if (!device.canRead() || !device.canWrite()) {
//			try {
//				/* Missing read/write permission, trying to chmod the file */
//				Process su;
//				su = Runtime.getRuntime().exec("/system/bin/su");
//				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
//						+ "exit\n";
//				su.getOutputStream().write(cmd.getBytes());
//				if ((su.waitFor() != 0) || !device.canRead()
//						|| !device.canWrite()) {
//					throw new SecurityException();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new SecurityException();
//			}
//		}

		mOpenFileFlag = open(device.getAbsolutePath());
		if (!mOpenFileFlag) {
			mFilePath = null;
			//Log.e(TAG, "native open returns null");
			//throw new IOException();
		} else{
			mFilePath = device.getAbsolutePath();
		}
	}

	public boolean isEmpty(){
		return !mOpenFileFlag;
	}

	public void close(){
		if(!mFilePath.isEmpty() && mOpenFileFlag){
			close(mFilePath);
		}
	}

	public byte[] read(){
		if(!mFilePath.isEmpty() && mOpenFileFlag){
			return read(mFilePath);
		}
		return null;
	}

	public void write(byte[] buf, int size){
		if(!mFilePath.isEmpty() && mOpenFileFlag){
			write(mFilePath, buf, size);
		}
	}

	// JNI
	private native static boolean open(String path);
	private native void close(String path);
	private native byte[] read(String path);
	private native void write(String path, byte[] buf, int size);
	static {
		System.loadLibrary("serial_port");
	}
}
