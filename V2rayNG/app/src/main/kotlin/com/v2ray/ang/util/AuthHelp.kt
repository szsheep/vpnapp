package com.v2ray.ang.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSONObject
import com.v2ray.ang.AppConfig
import com.v2ray.ang.ui.LoginActivity
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import rx.Completable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception

object AuthHelp {
    private const val TAG = "AuthHelp";

    public fun register(userName: String, password: String): Completable {
        return Completable.create {
            val httpClient = OkHttpClient();
            val JSON = "application/json;charset=utf-8".toMediaTypeOrNull();
            val jsonObject = JSONObject();
            try {
                jsonObject["userName"] = userName;
                jsonObject["password"] = password;
            } catch (e: Exception) {
                Log.d(TAG, "register: Exception:${e.message}");
                it.onError(e);
                e.printStackTrace();
                return@create;
            }

            val request = Request.Builder()
                .url(AppConfig.registerPath)
                .addHeader("User-Agent", AppConfig.USER_AGENT)
                .addHeader("Content-Type", AppConfig.CONTENT_TYPE)
                .addHeader("Accept", AppConfig.ACCEPT)
                .post(jsonObject.toJSONString().toRequestBody(JSON))
                .build();

            val response = httpClient.newCall(request).execute();
            if (response.isSuccessful) {
                Log.d(TAG, "register: 注册成功")
                it.onCompleted();
            }else {
                it.onError(Exception("响应失败: ${response.code}"));
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun login(userName: String, password: String): Completable {
        return Completable.create {
            val JSON: MediaType? = "application/json;charset=utf-8".toMediaTypeOrNull();
            try {
                val okHttpClient = OkHttpClient();
                val challengeJsonObj = JSONObject();
                challengeJsonObj["userName"] = userName;
                val getChallengeReq = Request.Builder()
                    .url(AppConfig.getChallengePath)
                    .addHeader("User-Agent", AppConfig.USER_AGENT)
                    .addHeader("Content-Type", AppConfig.CONTENT_TYPE)
                    .addHeader("Accept", AppConfig.ACCEPT)
                    .post(challengeJsonObj.toJSONString().toRequestBody(JSON))
                    .build();

                val getChallengeRsp = okHttpClient.newCall(getChallengeReq).execute();
                var encodeData: String? = null;

                if (getChallengeRsp.isSuccessful) {
                    val challengeBody = getChallengeRsp.body?.string();
                    Log.d(LoginActivity.TAG, "onCreate: challengeBody $challengeBody");
                    val challengeBodyJson = JSONObject.parseObject(challengeBody);
                    if (challengeBodyJson.getIntValue("code") != 0) {
                        it.onError(Exception("获取挑战响应失败: ${challengeBodyJson.getIntValue("code")}"));
                        return@create;
                    }

                    encodeData = challengeBodyJson.getString("data");
                    Log.d(LoginActivity.TAG, "onCreate: encodeData:${encodeData}");
                }else {
                    it.onError(Exception("获取挑战请求响应失败:${getChallengeRsp.code}"));
                    return@create;
                }

                val loginJsonObj: JSONObject = JSONObject();
                loginJsonObj["userName"] = userName;
                loginJsonObj["code"] = "3";
                Log.d(LoginActivity.TAG, "onCreate: after JSONObject")

                val key: ByteArray? = AesUtil.sha256(password);
                var decData: String? = key?.let { it1 -> AesUtil.AESDecode(encodeData, it1) };

                val decJsonObj = JSONObject.parseObject(decData);
                val challenge = decJsonObj.getString("challenge");

                val encodeChallenge = key?.let { it1 -> AesUtil.AESEncode(challenge, it1) };
                loginJsonObj["password"] = encodeChallenge;

                val loginReq = Request.Builder()
                    .url(AppConfig.loginPath)
                    .addHeader("User-Agent", AppConfig.USER_AGENT)
                    .addHeader("Content-Type", AppConfig.CONTENT_TYPE)
                    .addHeader("Accept", AppConfig.ACCEPT)
                    .post(loginJsonObj.toJSONString().toRequestBody(JSON))
                    .build();

                val loginRsp = okHttpClient.newCall(loginReq).execute();
                if (loginRsp.isSuccessful) {
                    val loginBodyString = loginRsp.body?.string();
                    Log.d(LoginActivity.TAG, "onCreate: loginBodyString $loginBodyString");
                    val loginBodyJson = JSONObject.parseObject(loginBodyString);
                    if (loginBodyJson.getIntValue("code") == 0) {
                        it.onCompleted();
                    }else {
                        Log.d(LoginActivity.TAG, "onCreate: 登录服务器响应失败: ${loginRsp.code}");
                        it.onError(Exception("登录服务器响应失败:${loginBodyJson.getIntValue("code")}"));
                    }
                }else {
                    Log.d(LoginActivity.TAG, "onCreate: 登录服务器响应异常: ${loginRsp.code}");
                    it.onError(Exception("登录服务器响应异常:${loginRsp.code}"));
                }
            }catch (e: Exception) {
                Log.d(LoginActivity.TAG, "onCreate: ${e.message}");
                it.onError(e);
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}