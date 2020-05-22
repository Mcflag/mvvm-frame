package cn.lcsw.diningpos.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class WxUtil {
	
	public static final String KEY_ALGORITHM = "RSA";
	 /**
     * SHA256WithRSA加密方法
     * @param data
     * @param key
     * @return
     */
    public static String signSHA256WithRSA(String data, String key) {
        try {
            byte[] dataByte = data.getBytes("UTF-8");
            // X509EncodedKeySpec
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key.getBytes("UTF-8")));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateKey);
            signature.update(dataByte);
            return new String(Base64.encodeBase64(signature.sign()));
        } catch (Exception e) {
            throw new RuntimeException("sign fail!", e);
        }
    }
}
