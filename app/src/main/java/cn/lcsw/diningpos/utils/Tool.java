package cn.lcsw.diningpos.utils;

import java.text.SimpleDateFormat;
import java.util.*;


public class Tool {

	/**
	 * 判断整形是否为null或0
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(Integer value) {
		return (value == null || value == 0);
	}

	/**
	 * 判断字符串是否为空
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		return (value == null || "".equals(value.trim()) || "null".equals(value.trim()));
	}

	/**
	 * 判断数组是否为空
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(Object[] value) {
		if (value == null || value.length == 0)
			return true;
		else
			return false;
	}

	/**
	 * 判断List是否为空
	 * @param list
	 * @return
	 */
	public static boolean isEmpty(List list) {
		if (list == null || list.size() == 0)
			return true;
		else
			return false;
	}

	/**
	 * 判断Map是否为空
	 * @param map
	 * @return
	 */
	public static boolean isEmpty(Map map) {
		if (map == null || map.size() == 0)
			return true;
		else
			return false;
	}

	/**
	 * 判断Set是否为空
	 * @param set
	 * @return
	 */
	public static boolean isEmpty(Set set) {
		if (set == null || set.size() == 0)
			return true;
		else
			return false;
	}

	/**
	 * 判断Object是否为空
	 * @param Value
	 * @return
	 */
	public static boolean isEmpty(Object value) {
		if (value == null || value.equals("null") || value.equals(""))
			return true;
		else
			return false;
	}

	/**
	 * 返回系统当前时间(精确到秒)
	 * @return
	 */
	public static String getTime(String formatStr){
		return new SimpleDateFormat(formatStr).format(new Date()).toString();
	}

	public static String getTime(String formatStr, Date date){
		return new SimpleDateFormat(formatStr).format(date).toString();
	}

	/**
	 * 获取32位随机字符串
	 * @return
	 */
	public static String uuid(){
		return UUID.randomUUID().toString().replace("-","").toUpperCase();
	}

	/**
	 * 拼接字符串参数
	 * @param sPara 要签名的数组
	 * @return 签名结果字符串
	 */
	public static String getStr(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (i == keys.size() - 1) {
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		return prestr;
	}

	/**
	 * 解析字符串参数
	 * @param paramsStr
	 * @return
	 */
	public static Map<String, String> getMap(String paramsStr){
		Map<String, String> params = new HashMap<String, String>();
		try {
			String param[]=paramsStr.split("&");
			for (int i = 0; i < param.length; i++) {
				if(param[i].split("=").length == 2){
					String key=param[i].split("=")[0];
					String value=param[i].split("=")[1];
					params.put(key, value);
				}
			}
		} catch (Exception e) {
			params = new HashMap<String, String>();
			e.printStackTrace();
		}
		return params;
	}

	/**
	 * 将对象转换成map集合
	 * @param object
	 * @return
	 */
	//public static Map<String,String> getMap(Object object){
	//	Map<String,String> params = new HashMap<String,String>();
	//	JSONObject json = (JSONObject)JSON.toJSON(object);
	//	for (String key : json.keySet()) {
	//		if ( json.getString(key) != null ){
	//			params.put(key, json.getString(key));
	//		}
	//	}
	//	return params;
	//}

	public static Sign SignTool(){
		return new Sign();
	}

	public static class Sign{
		private Map<String, String> map;
		private String lastStr = "";

		private Sign() {
			map=new HashMap<>();
		}

		public Sign putParam(String key, String value){
			map.put(key,value);
			return this;
		}

		public Sign putParamFilter(String key, String value){
			if(!StringUtil.isNull(value)){
				map.put(key,value);
			}
			return this;
		}
		//只过滤null
		public Sign putParamFilterNull(String key, String value){
			if(value!=null){
				map.put(key,value);
			}
			return this;
		}

		public Sign putLastParam(String key, String value){
			lastStr="&"+key+"="+value;
			return this;
		}

		public String getParam(String key){
			return map.get(key);
		}

		/**
		 * 获取签名字符串
		 * @return
		 */
		public String getSignStr(){
			List<String> keys = new ArrayList<String>(map.keySet());
			Collections.sort(keys);
			String prestr = "";
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				String value = map.get(key);
				if (i == keys.size() - 1) {
					prestr = prestr + key + "=" + value;
				} else {
					prestr = prestr + key + "=" + value + "&";
				}
			}
			return prestr+lastStr;
		}

		/**
		 * 获取签名
		 * @return
		 */
		public String getSign(){
			return MD5.sign(getSignStr(), "utf-8");
		}
	}


	public static void main(String[] args) {
		System.out.println(Tool.SignTool().
				putParam("merchant_no","123123")
				.putParam("search_type","1")
				.putParam("terminal_id","56234234")
				.putParam("terminal_time","123123123123")
				.putParam("operator_id","")
				.putLastParam("access_token","asdfasdf").getSignStr());
	}
}
