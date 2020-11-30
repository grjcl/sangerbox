package com.video.server.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    /**
     * aes加密
     *
     * @param content
     * @return
     * @throws Exception
     */
    public static String aesEncrypt(String content) {
        try {
            var uuid = Util.getUUID();
            return uuid + aesEncryptToBytes(content, uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * AES加密
     *
     * @throws Exception
     */
    public static String aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
        return new Base64().encodeToString(cipher.doFinal(content.getBytes("utf-8")));
    }


}
