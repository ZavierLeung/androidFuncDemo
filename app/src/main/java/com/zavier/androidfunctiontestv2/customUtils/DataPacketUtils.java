package com.zavier.androidfunctiontestv2.customUtils;

/**
 *  zavier
 *  自定义数据包格式：数据类型#文件名#发送状态#内容长度#内容#
 *  发送状态：1：首次发送，2：中间数据发送，3：最后数据发送（如果文件太大，需要拆开发送）
 *
 * */
public class DataPacketUtils {
    private int mPacketLength = 0;
    private int mStatus = 0;
    private String mFileName;
    private int mFileContentLength;
    private String mFileContent;
    private String mMessageType;

    public final static int SEND_STATUS_NULL       = 0;
    public final static int SEND_STATUS_START      = 1;
    public final static int SEND_STATUS_MIDDLE     = 2;
    public final static int SEND_STATUS_END        = 3;
    public final static String PACKET_HEADER_FLAGS = "zavierLeung#";

    private static final String TAG = "UsbDataPacket";

    public DataPacketUtils(){

    }

    public String getFileNameFromPath(String path){
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    public String getFileName(){
        return mFileName;
    }

    public String getMessageType(){
        return mMessageType;
    }

    public int getFileStatus(){
        return mStatus;
    }

    public int getPacketHeaderLength(){
        return mPacketLength;
    }

    public String getPacketHeader(String path, int status, String type){
        String string = PACKET_HEADER_FLAGS + type + "#" + getFileNameFromPath(path) + "#" +
                status + "#";
        return string;
    }

    public String getPacketHeader(int status, String type, String fileName){
        String string = PACKET_HEADER_FLAGS + type + "#" + fileName + "#" +
                status + "#";
        return string;
    }

    public static String getPacketHeader(String string){
        return PACKET_HEADER_FLAGS +  string;
    }

    public int getFileContentLength(){
        return mFileContentLength;
    }


    public boolean isPacketHeader(String packet){
        //自定义数据包格式：文件名#发送状态#内容
        if(packet.length() < PACKET_HEADER_FLAGS.length()){
            LogUtils.LogD(TAG,"flags: "+PACKET_HEADER_FLAGS+" packet: "+packet.substring(0,
                    PACKET_HEADER_FLAGS.length()));
            return false;
        }

        if(PACKET_HEADER_FLAGS.equals(packet.substring(0, PACKET_HEADER_FLAGS.length()))){
            String string,string1,string2;
            string = packet.substring(packet.indexOf("#")+1, packet.length());
            mMessageType = string.substring(0, string.indexOf("#"));
            string1 = string.substring(string.indexOf("#")+1, string.length());
            mFileName = string1.substring(0, string1.indexOf("#"));
            string2 = string1.substring(string1.indexOf("#")+1, string1.length());
            mStatus = Integer.parseInt(string2.substring(0, string2.indexOf("#")));
            return true;
        } else {
            return false;
        }
    }

    public static String setFilePacket(String type, String fileName, int status){
        return PACKET_HEADER_FLAGS +type + "#" + fileName + "#" + status;
    }
}
