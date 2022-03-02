/*
 * Copyright 2009-2011 Cedric Priscal
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

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>

#include "SerialPort.h"

#include "android/log.h"
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jboolean JNICALL Java_com_zavier_androidfunctiontestv2_SerialPortTest_SerialPort_open
  (JNIEnv *env, jclass thiz, jstring path)
{
	int fd;
	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
		LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDONLY);
		fd = open(path_utf, O_RDONLY);

		LOGD("open() fd = %d", fd);
		(*env)->ReleaseStringUTFChars(env, path, path_utf);
		if (fd == -1)
		{
			/* Throw an exception */
			LOGE("Cannot open port");
			/* TODO: throw an exception */
			return JNI_FALSE;
		}
	}
	close(fd);
	return JNI_TRUE;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_zavier_androidfunctiontestv2_SerialPortTest_SerialPort_close
  (JNIEnv *env, jobject thiz, jstring path)
{
	LOGD("close");
}

JNIEXPORT jbyteArray JNICALL Java_com_zavier_androidfunctiontestv2_SerialPortTest_SerialPort_read
		(JNIEnv *env, jobject thiz, jstring path)
{
    int fd;
    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR);
        fd = open(path_utf, O_RDONLY);
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1)
        {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return  NULL;
        }
    }

	jint size = 64;
	unsigned char charbuf[size];
	jint i = read(fd, charbuf, (size_t)size);
	LOGD("read(fd = %d),i:%d,buf: %s ", fd, i, charbuf);

	jbyteArray buf = (*env)->NewByteArray(env, i);
	(*env)->SetByteArrayRegion(env, buf, 0, i, charbuf);
    close(fd);
	return buf;
}

JNIEXPORT void JNICALL Java_com_zavier_androidfunctiontestv2_SerialPortTest_SerialPort_write
		(JNIEnv *env, jobject thiz, jstring path, jbyteArray buf, jint size)
{
    int fd;
    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR);
        fd = open(path_utf, O_RDWR | O_TRUNC);
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1)
        {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return ;
        }
    }

	unsigned char* charbuf = (*env)->GetByteArrayElements(env,buf,NULL);
	if(charbuf == NULL){
		LOGD("write(fd = %d),charbuf is null", fd);
		return;
	}
	pwrite(fd, charbuf, (size_t)size, 0);
	LOGD("write(fd = %d),size:%d ,buf: %s", fd,size,charbuf);

	(*env)->ReleaseByteArrayElements(env,buf,charbuf,0);
    close(fd);
}

