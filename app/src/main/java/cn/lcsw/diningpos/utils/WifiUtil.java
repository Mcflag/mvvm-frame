package cn.lcsw.diningpos.utils;

import android.content.Context;
import android.util.Log;

public class WifiUtil {

    public static void openWifi(){
        WifiAdmin wifiAdmin = new WifiAdmin();
        if (!wifiAdmin.mWifiManager.isWifiEnabled()) {
            wifiAdmin.openWifi();
        }

    }

    public static void joinToWifi(String strResult) {
        WifiAdmin wifiAdmin = new WifiAdmin();
        if (strResult.contains("P:") && strResult.contains("T:")) {// 自动连接wifi
            Log.e("扫描返回的结果----->", strResult);// 还是要判断
            String passwordTemp = strResult.substring(strResult.indexOf("P:"));
            String password = passwordTemp.substring(2, passwordTemp.indexOf(";"));
            String netWorkTypeTemp = strResult.substring(strResult.indexOf("T:"));
            String netWorkType = netWorkTypeTemp.substring(2, netWorkTypeTemp.indexOf(";"));
            String netWorkNameTemp = strResult.substring(strResult.indexOf("S:"));
            String netWorkName = netWorkNameTemp.substring(2, netWorkNameTemp.indexOf(";"));

            int net_type = 0x13;
            if (netWorkType.compareToIgnoreCase("wpa") == 0) {
                net_type = WifiAdmin.TYPE_WPA;// wpa
            } else if (netWorkType.compareToIgnoreCase("wep") == 0) {
                net_type = WifiAdmin.TYPE_WEP;// wep
            } else {
                net_type = WifiAdmin.TYPE_NO_PASSWD;// 无加密
            }
            wifiAdmin.addNetwork(netWorkName, password, net_type);
            Log.e("解析的数据----->", "networkname: "
                    + netWorkName + " "
                    + "password: "
                    + password
                    + " netWorkType: "
                    + net_type);

        }
    }
}
