package com.v2ray.anamin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.kaopiz.kprogresshud.KProgressHUD
import com.v2ray.anamin.R
import com.v2ray.anamin.data.RetrofitInstance
import com.v2ray.anamin.data.api.ApiService
import com.v2ray.anamin.data.model.Login
import com.v2ray.anamin.data.utils.storeStringPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Objects


class LoginActivity : AppCompatActivity() {
    private val TAG = "hossein75rn"
    private var etUserName: AppCompatEditText? = null
    private var etPassword: AppCompatEditText? = null
    private var buttonLogin: AppCompatButton? = null
    private var userName: String? = null
    private var password: String? = null

    private var api: ApiService? = null

    private var loading: KProgressHUD? = null
    private var storeUUid: storeStringPreference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
        buttonLogin!!.setOnClickListener { view: View -> this.cvClick(view) }
    }

    private fun cvClick(view: View) {
        Log.w(TAG, "onCreate: clicked")
        loading!!.show()
        readEditTexts()
        view.isClickable = false
        api?.login(userName, password)?.enqueue(object : Callback<Login?> {
            override fun onResponse(call: Call<Login?>, response: Response<Login?>) {
                Log.w(TAG, "onResponse: response received")
                loading!!.dismiss()
                if (response.body() != null) {
                    val responseS: Login? = response.body()
                    if (responseS != null) {
                        if (responseS.getStatus().equals(responseS.statusFailed, ignoreCase = true)) {
                            toastResult(responseS.getMessage())
                            view.isClickable = true
                        } else if (responseS.getStatus()
                                .equals(responseS.statusSuccess, ignoreCase = true)
                        ) {
                            storeUUid?.save("uuid", responseS.getUuid())
                            startConfigActivity()
                        } else {
                            toastResult("خطای ناشناخته ای رخ داد")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Login?>, t: Throwable) {
                Log.w(TAG, "onFailure: ", t)
                toastResult("خطای اینترنت")
            }
        })
    }

    private fun toastResult(responseS: String) {
        Toast.makeText(this@LoginActivity, responseS, Toast.LENGTH_SHORT).show()
    }


    private fun readEditTexts() {
        userName = Objects.requireNonNull(etUserName!!.text).toString().trim { it <= ' ' }
        password = Objects.requireNonNull(etPassword!!.text).toString().trim { it <= ' ' }
    }


    private fun startConfigActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        this@LoginActivity.finish()
    }

    private fun init() {
        storeUUid = storeStringPreference(this)
        checkIfSharedPreferenceIsSet()
        etUserName = findViewById(R.id.etUserName)
        etPassword = findViewById(R.id.etPassword)
        buttonLogin = findViewById(R.id.cvLogin)
        api = RetrofitInstance.Instance().create<ApiService>(ApiService::class.java)
        loading = KProgressHUD.create(this@LoginActivity)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("لطفا صبر کنید")
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
    }
    private fun checkIfSharedPreferenceIsSet() {
        val sh = storeUUid?.read("uuid");
        if (sh != null && sh.length > 2) {
            startConfigActivity()
        }
    }
}
