package cn.lcsw.diningpos.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;

/**
 * 工具类
 *
 * @author Haolp create on 2016-11-29
 */
public class SignBeanUtil {

    /**
     * 传JavaBean 和签名用的key ,有值参数进行字典排序
     * 过滤null 和空字符串  "",(内部使用)
     *
     * @param object
     * @param key
     * @return
     */
    public static String generateSign(Object object, String key, String Filter) {
        Tool.Sign signContent = Tool.SignTool();
        String sign = null;
        try {
//			Field[] fields = object.getClass().getDeclaredFields();
            Field[] fields = null;

            fields = getBeanFields(object.getClass(), fields);

            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                String name = f.getName();
                // 私有变量必须先设置Accessible为true
                f.setAccessible(true);
                Object valueObject = f.get(object);
                if (valueObject == null) {
                    continue;
                }

                if (Filter != null && !Filter.equals("") && Filter.equals(name)) {
                    continue;
                }
                String value = valueObject.toString();

                if (value != null && !value.equals("")) {
                    signContent.putParam(name, value);
                }
            }
            signContent.putLastParam("key", key);
            String parm = signContent.getSignStr();
            sign = MD5.sign(parm, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sign;
    }

    /**
     * 传JavaBean 和签名用的key ,有值参数进行字典排序
     * 只过滤null （仅限于外部使用）
     *
     * @param object
     * @param key
     * @return
     */
    public static String FilterNullSign(Object object, String key, String Filter) {
        Tool.Sign signContent = Tool.SignTool();
        String sign = null;
        try {
            Field[] fields = null;

            fields = getBeanFields(object.getClass(), fields);

            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                String name = f.getName();
                // 私有变量必须先设置Accessible为true
                f.setAccessible(true);
                Object valueObject = f.get(object);
                if (valueObject == null) {
                    continue;
                }

                if (Filter != null && !Filter.equals("") && Filter.equals(name)) {
                    continue;
                }
                String value = valueObject.toString();

                if (value != null) {
                    signContent.putParam(name, value);
                }

            }
            signContent.putLastParam("key", key);
            String parm = signContent.getSignStr();
            sign = MD5.sign(parm, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sign;
    }

    /**
     * 传JavaBean 和签名用的key ,有值参数进行字典排序
     * 只过滤null （仅限于外部使用。支付接口）
     * 比FilterNullSign(Object object, String key, String Filter) 兼容性更广
     *
     * @param object
     * @param keyname
     * @param keyvalue
     * @param Filter
     * @param charset
     * @return
     */
    public static String FilterNullSign(Object object, String keyname, String keyvalue, String Filter, String charset) {
        Tool.Sign signContent = Tool.SignTool();
        String sign = null;
        try {
            Field[] fields = null;

            fields = getBeanFields(object.getClass(), fields);

            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                String name = f.getName();
                // 私有变量必须先设置Accessible为true
                f.setAccessible(true);
                Object valueObject = f.get(object);
                if (valueObject == null) {
                    continue;
                }

                if (Filter != null && !Filter.equals("") && Filter.equals(name)) {
                    continue;
                }
                if (name.equals("serialVersionUID")
                        || name.equals("shadow$_klass_")
                        || name.equals("shadow$_monitor_")) {
                    continue;
                }
                String value = valueObject.toString();

                if (value != null) {
                    signContent.putParam(name, value);
                }

            }
            signContent.putLastParam(keyname, keyvalue);
            String parm = signContent.getSignStr();
            try {
//                LogUtils.i(parm);
                sign = MD5.MD5Encode(parm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sign;
    }

    /**
     * 获取类对象，包含父类
     *
     * @param cls
     * @param fs
     * @return
     */
    public static Field[] getBeanFields(Class cls, Field[] fs) {
        fs = (Field[]) ArrayUtils.addAll(fs, cls.getDeclaredFields());
        if (cls.getSuperclass() != null) {
            Class clsSup = cls.getSuperclass();
            fs = getBeanFields(clsSup, fs);
        }
        return fs;
    }

    public static void main(String[] args) {
        // AddBean addBean = new AddBean();
        // addBean.setAccount_name("1");
        // addBean.setMerchant_id_type(2);
        // generateSign(addBean,"a");

    }

    public static String sginAccessToken(Object object, String accessToken) {
        String sign = FilterNullSign(object, "access_token", accessToken, "key_sign", "utf-8");
        return sign;
    }

    public static String signKey(Object object, String key) {
        String sign = FilterNullSign(object, "key", key, "key_sign", "utf-8");
        return sign;
    }
}
