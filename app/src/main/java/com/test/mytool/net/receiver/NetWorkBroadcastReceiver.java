package com.test.mytool.net.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.test.mytool.net.NetWorkUtils;
import com.test.mytool.net.OffLineHandle;

/**
 * @author yangqiang
 */
public class NetWorkBroadcastReceiver extends BroadcastReceiver {

    String TAG = "RemoteDebug:BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: "+intent.getAction());

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                if (NetWorkUtils.isNetworkAvailable(context)) {
                    OffLineHandle.getIntance().setIsWifiReady(true);
                }
            }else if (wifiState == WifiManager.WIFI_STATE_DISABLED){
                OffLineHandle.getIntance().setIsWifiReady(false);
            }
        }

        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
            if(NetWorkUtils.isNetworkAvailable(context)){
                if (OffLineHandle.getIntance().isWifiReady()){

                    Log.d(TAG, "ready handleOffLineLog...");

                    OffLineHandle.getIntance().handleOffLineTask();
                }
            }
        }
    }
}
