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

    /**
     * 此方案有一个问题,当网络不稳定或者抖动的时候,会频繁的执行任务,此处需要做过滤重复任务的操作
     * 可以借助集合或者是数据库来排除重复任务
     */
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
