package com.v2ray.ang.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivityRegisterBinding
import com.v2ray.ang.util.AuthHelp
import rx.CompletableSubscriber
import rx.Subscription

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater);
        setContentView(binding.root);

        val userNameEdt = binding.rgUsernameEdt;
        val passwordEdt = binding.rgPasswordEdt;
        val password2Edt = binding.rgPassword2Edt;

        val registerBtn = binding.rgRegisterBtn;
        registerBtn.setOnClickListener() {
            if (userNameEdt.text.isEmpty()) {
                Toast.makeText(applicationContext, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (passwordEdt.text.isEmpty()) {
                Toast.makeText(applicationContext, "密码不能为空", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (password2Edt.text.isEmpty()) {
                Toast.makeText(applicationContext, "密码2不能为空", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (!passwordEdt.text.contentEquals(password2Edt.text.toString())) {
                Toast.makeText(applicationContext, "两次密码不一致", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            AuthHelp.register(userNameEdt.text.toString(), passwordEdt.text.toString())
                .subscribe(object : CompletableSubscriber {
                    override fun onCompleted() {
                        val intent = Intent();
                        intent.putExtra(AppConfig.LOGIN_USER_NAME, userNameEdt.text.toString());
                        setResult(0, intent);
                        finish();
                    }

                    override fun onError(e: Throwable?) {
                        Toast.makeText(applicationContext, "注册失败:${e?.message}", Toast.LENGTH_SHORT).show();
                    }

                    override fun onSubscribe(d: Subscription?) {

                    }
                })
        }
    }
}