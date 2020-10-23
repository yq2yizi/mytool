package com.test.mytool.net;

import android.util.Log;

/**
 * Author:wb-yangqiang@ubtrobot.com
 * Time:2020/10/23
 * Description: This is OffLineHandle
 */
public class OffLineHandle {

    String TAG = "OffLineHandle";

    boolean isWifiReady;

    public void setIsWifiReady(boolean b) {
        isWifiReady = b;
    }

    public boolean isWifiReady() {
        return isWifiReady;
    }

    public void handleOffLineTask() {
        Log.d(TAG, "handleOffLineTask: ...");
    }

    private static class Holder {
        private static OffLineHandle offLineLogHandle = new OffLineHandle();
    }

    public static OffLineHandle getIntance() {
        return Holder.offLineLogHandle;
    }

}
