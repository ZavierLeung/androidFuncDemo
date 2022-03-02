package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

public interface OnUsbStatusChanged {
    /** USB插入 */
    void onUsbAttached();

    /** USB拔出 */
    void onUsbDetached();
}
