package com.yan.xpay.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;

public class SecureUtils {
    public static String decodePrivateKey(String keystore, String encrypt){
        byte[] key = SecureUtil.decode(encrypt);
        AES aes = SecureUtil.aes(key);
        String privateKey = aes.decryptStr(keystore);
        return privateKey;
    }
}
