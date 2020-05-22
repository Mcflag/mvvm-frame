package cn.lcsw.diningpos.utils;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class DeviceUtil {
  /**
   * 获取机器号
   */
  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  public static String getMacid() {
    String id = "";
    if (StringUtil.isNull(id)) {
      String SerialNumber = android.os.Build.SERIAL;
      id = SerialNumber;
    }

    return id;
  }

  //获取网络Mac地址
  public static String getMac() {
    String macSerial = null;
    String str = "";
    try {
      Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address");
      InputStreamReader ir = new InputStreamReader(pp.getInputStream());
      LineNumberReader input = new LineNumberReader(ir);

      for (; null != str; ) {
        str = input.readLine();
        if (str != null) {
          macSerial = str.trim();// 去空格
          break;
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return macSerial;
  }
}
