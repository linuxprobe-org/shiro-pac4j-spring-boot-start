package org.linuxprobe.shiro.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ASEUtils {
    private static final String KEY_AES = "AES";

    /**
     * 构建加解密工具类
     */
    private static Cipher buildCipher(String key, boolean encrypt) {
        if (key == null || key.length() != 16) {
            throw new IllegalArgumentException("key不满足条件");
        }
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, ASEUtils.KEY_AES);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ASEUtils.KEY_AES);
            if (encrypt) {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cipher;
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  密钥
     */
    public static String encrypt(String data, String key) {
        Cipher cipher = ASEUtils.buildCipher(key, true);
        byte[] encrypted;
        try {
            encrypted = cipher.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Base64.encodeBase64String(encrypted);
    }

    /**
     * 解密
     *
     * @param data 已加密数据
     * @param key  密钥
     */
    public static String decrypt(String data, String key) {
        Cipher cipher = ASEUtils.buildCipher(key, false);
        byte[] encrypted1 = Base64.decodeBase64(data);
        byte[] original;
        try {
            original = cipher.doFinal(encrypted1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new String(original);
    }
}
