package com.horses.camera.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Brian Salvattore
 */
public class MD5Util {

    public static String getMD5(String val) {
        MessageDigest md5;
        try {

            md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes());

            byte[] m = md5.digest();

            return getString(m);
        }
        catch (NoSuchAlgorithmException e) {

            return val;
        }

    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static String getString(byte[] b) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }

        return sb.toString();
    }

}
