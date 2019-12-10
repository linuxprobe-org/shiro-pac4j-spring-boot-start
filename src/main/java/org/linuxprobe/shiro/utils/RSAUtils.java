package org.linuxprobe.shiro.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {
    /**
     * 随机生成密钥对
     *
     * @param keySize 密钥长度
     */
    public static StringKeyPair genStringKeyPair(int keySize) {
        try {
            // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(keySize, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 得到私钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            // 得到公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
            // 得到私钥字符串
            String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
            return new StringKeyPair(privateKeyString, publicKeyString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据字符串密钥对生成对象密钥对
     *
     * @param publicKey  公钥
     * @param privateKey 私钥
     */
    public static KeyPair generateKeyPair(String publicKey, String privateKey) {
        try {
            PublicKey rsaPublicKey = null;
            if (privateKey != null) {
                byte[] publicKeyDecoded = Base64.decodeBase64(publicKey);
                rsaPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyDecoded));
            }
            PrivateKey rsaPrivateKey = null;
            if (privateKey != null) {
                byte[] privateKeyDecoded = Base64.decodeBase64(privateKey);
                rsaPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyDecoded));
            }
            return new KeyPair(rsaPublicKey, rsaPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     *
     * @param str         需要加密字的符串
     * @param key         密钥
     * @param isPublicKey 是否是公钥
     */
    private static String encrypt(String str, String key, boolean isPublicKey) {
        try {
            //base64编码的公钥
            byte[] decoded = Base64.decodeBase64(key);
            Key encryptKey = null;
            if (isPublicKey) {
                encryptKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            } else {
                encryptKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
            }
            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, encryptKey);
            byte[] bin = str.getBytes(StandardCharsets.UTF_8);
            int blockSize = cipher.getBlockSize();
            if (blockSize > 0) {
                int outputSize = cipher.getOutputSize(bin.length);
                int leavedSize = bin.length % blockSize;
                int blocksSize = leavedSize != 0 ? bin.length / blockSize + 1
                        : bin.length / blockSize;
                byte[] raw = new byte[outputSize * blocksSize];
                int i = 0, remainSize = 0;
                while ((remainSize = bin.length - i * blockSize) > 0) {
                    int inputLen = remainSize > blockSize ? blockSize : remainSize;
                    cipher.doFinal(bin, i * blockSize, inputLen, raw, i * outputSize);
                    i++;
                }
                return Base64.encodeBase64String(raw);
            }
            return Base64.encodeBase64String(cipher.doFinal(bin));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥加密
     *
     * @param str       需要加密字的符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encryptByPublicKey(String str, String publicKey) {
        return RSAUtils.encrypt(str, publicKey, true);
    }

    /**
     * 私钥加密
     *
     * @param str        需要加密字的符串
     * @param privateKey 私钥
     */
    public static String encryptByPrivateKey(String str, String privateKey) {
        return RSAUtils.encrypt(str, privateKey, false);
    }


    /**
     * 私钥解密
     *
     * @param str        已被加密字的符串
     * @param privateKey 私钥
     */
    public static String decryptByPrivateKey(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        //return new String(cipher.doFinal(inputByte));
        int blockSize = cipher.getBlockSize();
        if (blockSize > 0) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
            int j = 0;
            while (inputByte.length - j * blockSize > 0) {
                bout.write(cipher.doFinal(inputByte, j * blockSize, blockSize));
                j++;
            }
            return bout.toString();
        }
        return new String(cipher.doFinal(inputByte));
    }

    /**
     * 公钥解密
     *
     * @param str       已被加密字的符串
     * @param publicKey 公钥
     */
    public static String decryptByPublicKey(String str, String publicKey) {
        try {
            //64位解码加密后的字符串
            byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8));
            //base64编码的公钥
            byte[] decoded = Base64.decodeBase64(publicKey);
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            //RSA解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            return new String(cipher.doFinal(inputByte));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class StringKeyPair {
        private String privateKey;
        private String publicKey;
    }
}
