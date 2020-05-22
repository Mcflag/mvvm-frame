package cn.lcsw.diningpos.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

public class StringUtil {
  // 判断一个字符是否是中文
  public static boolean isChinese(char c) {
    return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
  }

  // 判断一个字符串是否含有中文
  public static boolean isChinese(String str) {
    if (str == null) return false;
    for (char c : str.toCharArray()) {
      if (isChinese( c )) return true;// 有一个中文字符就返回
    }
    return false;
  }

  public static boolean isNull(String str) {

    if (str == null || "".equals( str )) {
      return true;
    }
    return false;
  }

  public static boolean isNotNull(String str) {

    if (isNull( str )) {
      return false;
    }
    return true;
  }

  public static String binaryString2hexString(String bString) {
    if (bString == null || bString.equals( "" ) || bString.length() % 8 != 0) {
      return null;
    }
    StringBuffer tmp = new StringBuffer();
    int iTmp = 0;
    for (int i = 0; i < bString.length(); i += 4) {
      iTmp = 0;
      for (int j = 0; j < 4; j++) {
        iTmp += Integer.parseInt( bString.substring( i + j, i + j + 1 ) ) << (4 - j - 1);
      }
      tmp.append( Integer.toHexString( iTmp ) );
    }
    return tmp.toString();
  }

  private static char[] HEXCHAR = {'0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  public static String toHexString(byte[] b) {
    StringBuilder sb = new StringBuilder( b.length * 2 );
    for (int i = 0; i < b.length; i++) {
      sb.append( HEXCHAR[(b[i] & 0xf0) >>> 4] );
      sb.append( HEXCHAR[b[i] & 0x0f] );
    }
    return sb.toString();
  }

  public static String toHexString(String src) {
    byte[] b = src.getBytes();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      sb.append( Integer.toHexString( 0xFF & b[i] ) );
    }
    return sb.toString();
  }

  private static byte toByte(char c) {
    byte b = (byte) "0123456789ABCDEF".indexOf( c );
    return b;
  }

  public static byte[] hexStringToByte(String hex) {
    if (hex.length() == 1) {
      hex = "0" + hex;
    }
    hex = hex.toUpperCase();

    int len = (hex.length() / 2);
    byte[] result = new byte[len];
    char[] achar = hex.toCharArray();
    for (int i = 0; i < len; i++) {
      int pos = i * 2;
      result[i] = (byte) (toByte( achar[pos] ) << 4 | toByte( achar[pos + 1] ));
    }
    return result;
  }

  /**
   * @功能: 10进制串转为BCD码 左补0
   * @参数: 10进制串
   * @结果: BCD码
   */
  public static byte[] str2Bcdleft(String asc) {
    int len = asc.length();
    int mod = len % 2;
    if (mod != 0) {
      asc = "0" + asc;
      len = asc.length();
    }
    byte abt[] = new byte[len];
    if (len >= 2) {
      len = len / 2;
    }
    byte bbt[] = new byte[len];
    abt = asc.getBytes();
    int j, k;
    for (int p = 0; p < asc.length() / 2; p++) {
      if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
        j = abt[2 * p] - '0';
      } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
        j = abt[2 * p] - 'a' + 0x0a;
      } else {
        j = abt[2 * p] - 'A' + 0x0a;
      }
      if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
        k = abt[2 * p + 1] - '0';
      } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
        k = abt[2 * p + 1] - 'a' + 0x0a;
      } else {
        k = abt[2 * p + 1] - 'A' + 0x0a;
      }
      int a = (j << 4) + k;
      byte b = (byte) a;
      bbt[p] = b;
    }
    return bbt;
  }

  /**
   * byteתString
   */
  public static String bytes2HexString(byte[] b) {
    String ret = "";
    for (int i = 0; i < b.length; i++) {
      String hex = Integer.toHexString( b[i] & 0xFF );
      if (hex.length() == 1) {
        hex = '0' + hex;
      }
      ret += hex.toUpperCase();
    }
    return ret;
  }

  // String转10进制
  public static short parseHex4(String num) {
    if (num.length() != 4) {
      throw new NumberFormatException( "Wrong length: " + num.length()
          + ", must be 4." );
    }
    int ret = Integer.parseInt( num, 16 );
    ret = ((ret & 0x8000) > 0) ? (ret - 0x10000) : (ret);
    return (short) ret;
  }

  // mpos
  // 字符串转二进制

  public static String StringTo2(byte[] bitMap) {

    StringBuffer sb = new StringBuffer();

    for (byte b : bitMap) {

      // System.out.println(b);

      int x = b;

      if (x < 0) {
        x = b + 256;// 如果是负数，求补
      }

      String bit = Integer.toBinaryString( x );// 整型转2进制

      if (bit.length() < 8) {

        int highBitBu0 = 8 - bit.length();

        for (int i = 0; i < highBitBu0; i++) { //

          sb.append( "0" );
        }

        sb.append( bit );
      } else {

        sb.append( bit );
      }

      sb.append( " " );// 每8位（字节）的分隔符
    }

    return sb.toString();
  }

  public static String[] getAlias(String bitMap) {

    if (bitMap.length() > 64) {
      System.err.print( "wrong bitMap" );
    }

    String tmp = bitMap;

    tmp = tmp.replace( "0", "" );

    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < 64; i++) {

      char c = bitMap.charAt( i );

      if (c == '1') {

        if ((i + 1) < 10) {
          sb.append( "00" );
        } else if ((i + 1) < 100) {
          sb.append( "0" );
        }

        sb.append( i + 1 ).append( "," );
      }
    }

    String fields = sb.toString();

    fields = fields.substring( 0, fields.length() - 1 );// ȥ�����һ��,

    // System.out.println("���ĵ������="+fields);

    String[] fieldsArray = fields.split( "," );// ת�����ַ�����

    if (tmp.length() != fieldsArray.length) {
      System.err.println( "fields area is error,please check by hand" );
    }

    return fieldsArray;
  }

  /**
   * asciiת16����
   */
  public static String convertHexToString(String hex) {

    StringBuilder sb = new StringBuilder();
    StringBuilder temp = new StringBuilder();
    // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
    for (int i = 0; i < hex.length() - 1; i += 2) {

      // grab the hex in pairs
      String output = hex.substring( i, (i + 2) );
      // convert hex to decimal
      int decimal = Integer.parseInt( output, 16 );
      // convert the decimal to character
      sb.append( (char) decimal );

      temp.append( decimal );
    }

    return sb.toString();
  }

  /**
   * 生成随机字符串
   */
  public static String getRandomString(int length) { //length表示生成字符串的长度
    String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    Random random = new Random();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      int number = random.nextInt( base.length() );
      sb.append( base.charAt( number ) );
    }
    return sb.toString();
  }

  /**
   * 字符串转换成16进制字符串
   *
   * @param str 待转换的ASCII字符串
   * @return 转换后的16进制字符串，Byte之间用空格分开，如：[61 6C 6B]，每16个Byte换行
   */
  public static String str2Hex(String str) {
    char[] hexChars = "0123456789ABCDEF".toCharArray();
    StringBuilder sb = new StringBuilder();
    byte[] bs = str.getBytes();
    int bit;
    for (int i = 0; i < bs.length; i++) {
      bit = (bs[i] & 0xf0) >> 4;
      sb.append( hexChars[bit] );
      bit = bs[i] & 0x0f;
      sb.append( hexChars[bit] );
      sb.append( " " );
				/*if (i != 0 && i % 16 == 0) {
					sb.append("\n");
				}*/
    }
    return sb.toString().trim();
  }

  /**
   * @功能: 10进制串转为BCD码 右补0
   * @参数: 10进制串
   * @结果: BCD码
   */
  public static byte[] str2Bcdright(String asc) {
    int len = asc.length();
    int mod = len % 2;
    if (mod != 0) {
      asc = asc + "0";
      len = asc.length();
    }
    byte abt[] = new byte[len];
    if (len >= 2) {
      len = len / 2;
    }
    byte bbt[] = new byte[len];
    abt = asc.getBytes();
    int j, k;
    for (int p = 0; p < asc.length() / 2; p++) {
      if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
        j = abt[2 * p] - '0';
      } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
        j = abt[2 * p] - 'a' + 0x0a;
      } else {
        j = abt[2 * p] - 'A' + 0x0a;
      }
      if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
        k = abt[2 * p + 1] - '0';
      } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
        k = abt[2 * p + 1] - 'a' + 0x0a;
      } else {
        k = abt[2 * p + 1] - 'A' + 0x0a;
      }
      int a = (j << 4) + k;
      byte b = (byte) a;
      bbt[p] = b;
    }
    return bbt;
  }

  // two_Track_Dec
  static public String getTwoTrackDec(String two_tarck) {
    if (two_tarck.length() % 2 != 0) {
      two_tarck = two_tarck + "F";
      String TDB_2 = two_tarck.substring( two_tarck.length() - 18,
          two_tarck.length() - 2 );
      String TDB_2_ENC = StringUtil
          .toHexString( StringUtil.decryptMode(
              StringUtil
                  .hexStringToByte( "22222222222222222222222222222222" ),
              StringUtil.hexStringToByte( TDB_2 ) ) );
      String TDB_1 = two_tarck.substring( 0, two_tarck.length() - 18 );
      two_tarck = TDB_1
          + TDB_2_ENC
          + two_tarck.substring( two_tarck.length() - 2,
          two_tarck.length() - 1 );
      System.out.println( "aaaaaaaaaaaaaaaaaa=" + two_tarck );
    } else {
      String TDB_2 = two_tarck.substring( two_tarck.length() - 18,
          two_tarck.length() - 2 );
      String TDB_2_ENC = StringUtil
          .toHexString( StringUtil.decryptMode(
              StringUtil
                  .hexStringToByte( "22222222222222222222222222222222" ),
              StringUtil.hexStringToByte( TDB_2 ) ) );
      String TDB_1 = two_tarck.substring( 0, two_tarck.length() - 18 );
      // int length=f35ascii.length()-TDB_2_ENC.length()-2;
      two_tarck = TDB_1
          + TDB_2_ENC
          + two_tarck.substring( two_tarck.length() - 2,
          two_tarck.length() );
    }
    return two_tarck;
  }

  /**
   * 3DES解密
   */
  public static byte[] decryptMode(byte[] keybyte, byte[] src) {
    try {
      // 生成密钥
      // SecretKey secretKey = skf.generateSecret(dks);
      SecretKey deskey = new SecretKeySpec( keybyte,
          "DESede/ECB/NoPadding" ); // 解密
      Cipher c1 = Cipher.getInstance( "DESede/ECB/NoPadding" );
      c1.init( Cipher.DECRYPT_MODE, deskey );
      return c1.doFinal( src );
    } catch (java.security.NoSuchAlgorithmException e1) {
      e1.printStackTrace();
    } catch (javax.crypto.NoSuchPaddingException e2) {
      e2.printStackTrace();
    } catch (Exception e3) {
      e3.printStackTrace();
    }
    return null;
  }

  /**
   * 3des加解密
   *
   * @param mode Cipher.ENCRYPT_MODE | Cipher.DECRYPT_MODE
   * @param input byte[]
   * @return byte[]
   * @throws Exception NoSuchAlgorithm,InvalidKey,NoSuchPadding,BadPadding, IllegalBlockSize
   */
  private static byte[] des3Init(byte[] key, int mode, byte[] input)
      throws Exception {
    // 根据【给定的字节数组key】和【 指定算法DESede(3des)】构造一个密钥
    SecretKey secretKey = new SecretKeySpec( key, "DESede/ECB/NoPadding" );
    // 加解密
    Cipher cipher = Cipher.getInstance( "DESede/ECB/NoPadding" );
    cipher.init( mode, secretKey );
    return cipher.doFinal( input );
  }

  public static boolean isBlank(CharSequence string) {
    return (string == null || string.toString().trim().length() == 0);
  }

  public static String valueOrDefault(String string, String defaultString) {
    return isBlank( string ) ? defaultString : string;
  }

  public static String truncateAt(String string, int length) {
    return string.length() > length ? string.substring( 0, length ) : string;
  }
}
