package com.v2ray.ang.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSONObject
import com.tencent.mmkv.MMKV
import com.v2ray.ang.AppConfig
import com.v2ray.ang.databinding.ActivityLoginBinding
import com.v2ray.ang.util.AesUtil
import com.v2ray.ang.util.AuthHelp
import com.v2ray.ang.util.MmkvManager
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import rx.Completable
import rx.CompletableSubscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception
import kotlin.math.log

class LoginActivity : AppCompatActivity() {
    companion object {
        val TAG: String = "LoginActivity";
    }
    private val mainStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private lateinit var binding: ActivityLoginBinding;
    private val registerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater);
        setContentView(binding.root)

        val autoLogin = binding.autoLoginChk;
        autoLogin.setOnCheckedChangeListener { buttonView, isChecked ->
            mainStorage.encode(AppConfig.AUTO_LOGIN_ENABLED, isChecked);
        };

        val registerBtn = binding.registerBtn;
        registerBtn.setOnClickListener() {
            val intent: Intent = Intent(this, RegisterActivity::class.java);
            registerResult.launch(intent);
        }

        val loginBtn = binding.loginBtn;
        loginBtn.setOnClickListener() {
            val userName = binding.usernameEdt.text.toString();
            val password = binding.passwordEdt.text.toString();

            if (userName.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "名字或密码不能为空", Toast.LENGTH_SHORT).show()
            }else {
                    AuthHelp.login(userName, password)
                    .subscribe(object : CompletableSubscriber {
                        override fun onCompleted() {
                            val intent = Intent();
                            intent.putExtra(AppConfig.LOGIN_USER_NAME, userName);
                            intent.putExtra(AppConfig.LOGIN_PASSWORD, password);
                            setResult(0, intent);
                            finish();
                        }

                        override fun onError(e: Throwable?) {
                            Log.d(TAG, "onError: ${e?.message}");
                            Toast.makeText(applicationContext, "登录失败,原因:${e?.message}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onSubscribe(d: Subscription?) {
                            Log.d(TAG, "onSubscribe: ")
                        }
                    })
            }
        }

    }

    override fun onBackPressed() {

    }
}