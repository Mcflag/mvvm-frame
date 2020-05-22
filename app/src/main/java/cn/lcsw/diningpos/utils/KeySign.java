package cn.lcsw.diningpos.utils;

import java.util.UUID;

/**
 * 签名处理
 * @author linzhe
 *
 */
public class KeySign {

	public static String prefix_key = "&access_token=";



	public static String getRandomUUID(){
		UUID uuid = UUID.randomUUID();
		String replace = uuid.toString().replace("-", "");
		return replace;
	}
	/**
	 * MD5无密签名
	 * @param text
	 * @return
	 */
	public static String signMD5(String text) {

		return MD5.MD5Encode(text);
	}

	/**
	 * MD5加密签名
	 * @param text
	 * @param key
	 * @return
	 */
	public static String signMD5(String text, String key) {

		text = text + prefix_key + key;
//		LogUtils.i("md5",text);
		return MD5.MD5Encode(text);
	}

	/**
	 * MD5无密校验
	 * @param text
	 * @param sign
	 * @return
	 */
	public static boolean verifyMD5(String text, String sign) {
		
		String mysign = MD5.MD5Encode(text);
				
		if(mysign.equals(sign)) {
    		return true;
    	}
    	else {
    		return false;
    	}
		
	}

	/**
	 * MD5加密校验
	 * @param text
	 * @param sign
	 * @param key
	 * @return
	 */
	public static boolean verifyMD5(String text, String sign, String key) {
		
		text = text + prefix_key + key;

		String mysign =  MD5.MD5Encode(text);
		
		if(mysign.equals(sign)) {
    		return true;
    	}
    	else {
    		return false;
    	}
	}
	
	
}
