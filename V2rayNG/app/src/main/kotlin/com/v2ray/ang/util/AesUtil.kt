package com.v2ray.ang.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.internal.toHexString
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.security.Key
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesUtil {
    const val TAG = "AesUtil";

    @RequiresApi(Build.VERSION_CODES.O)
    public fun AESEncode(content: String, keyByte: ByteArray): String? {
        if (keyByte.size != 32) {
            return null;
        }

        val salt = ByteArray(keyByte.size / 2);
        val encryptKey = ByteArray(keyByte.size / 2);

        System.arraycopy(keyByte, 0, salt, 0, keyByte.size / 2);
        System.arraycopy(keyByte, keyByte.size / 2, encryptKey, 0, keyByte.size / 2);

        val key: Key = SecretKeySpec(encryptKey, "AES");

        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(salt));
            val encryptedText: ByteArray = cipher.doFinal(content.toByteArray());
            return Base64.getUrlEncoder().encodeToString(encryptedText);
        }catch (e: Exception) {

        }

        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun AESDecode(content: String, keyByte: ByteArray): String? {
        if (keyByte.size != 32) {
            return null;
        }

        val salt = ByteArray(keyByte.size / 2);
        val encryptKey = ByteArray(keyByte.size / 2);

        System.arraycopy(keyByte, 0, salt, 0, keyByte.size / 2);
        System.arraycopy(keyByte, keyByte.size / 2, encryptKey, 0, keyByte.size / 2);

        try {
            val skeySpec = SecretKeySpec(encryptKey, "AES");
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            val ivParameter = IvParameterSpec(salt);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameter);
            try {
                val encrypted1 = Base64.getUrlDecoder().decode(content);
                val original = cipher.doFinal(encrypted1);
                return original.decodeToString();
            } catch (e: Exception) {
                return null;
            }
        } catch (e: Exception) {
            return null;
        }
    }

    public fun sha256(str: String): ByteArray? {
        var messageDigest: MessageDigest;
        var encodeStr = "";

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            val hash = messageDigest.digest(str.toByteArray());
            return hash;
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace();
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace();
        }

        return null;
    }

    public fun time33(str: String): Int {
        var hash = 5381;
        for (s in str) {
            hash = hash * 33 + s.code
        }

        Log.d(TAG, "time33: hash is $hash bin: ${hash.toHexString()}")

        val a = hash
        Log.d(TAG, "time33: $a")

        return hash;
    }
}