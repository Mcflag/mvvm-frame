package cn.lcsw.diningpos.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class UnitUtils {

  private static final DecimalFormat df = new DecimalFormat("0.00");

  public static String convertRate(int rate) {
    return Arith.div(rate, 10, 1) + "‰";
  }

  public static String fen2Yuan(long fen) {
    double div = Arith.div(fen, 100, 2); // 仅仅设置scale为2会出现0.10为0.1的情况
    return df.format(div);
  }

  public static String fen2YuanIgnoreEndZero(long fen) {
    String yuan = fen2Yuan(fen);
    if (yuan.indexOf(".") > 0) {
      //正则表达
      yuan = yuan.replaceAll("0+?$", "");//去掉后面无用的零
      yuan = yuan.replaceAll("[.]$", "");//如小数点后面全是零则去掉小数点
    }
    return yuan;
  }

  public static String yuan2FenString(String yuan) {
    if(!isNumber(yuan)){
      return "";
    }
    return String.valueOf(Double.valueOf(Arith.mul(Double.parseDouble(yuan), 100)).intValue());
  }

  public static int yuan2FenInt(String yuan) {
    if(TextUtils.isEmpty(yuan)){
      throw new IllegalArgumentException("empty Yuan");
    }
    return Integer.parseInt(yuan2FenString(yuan));
  }

  private static boolean isNumber(String str) {
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后2位的数字的正则表达式
    java.util.regex.Matcher match = pattern.matcher(str);
    return match.matches();
  }

  public static int getFenByYuan(String yuan) {
    return new BigDecimal(yuan).multiply(new BigDecimal(100)).intValue();
  }
}
