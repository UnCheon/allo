package com.allo;

import android.util.Base64;

/**
 * Created by uncheon on 2015. 8. 13..
 */

public class AlloCrypto {

    public static byte[] encryptBytes(String seed, byte[] cleartext) throws Exception {
        byte[] result_base64 = Base64.encode(cleartext, 0);
        return result_base64;
    }

    public static byte[] decryptBytes(String seed, byte[] encrypted) throws Exception {
        byte[] result_base64 = Base64.decode(encrypted, 0);
        return result_base64;
    }

}
