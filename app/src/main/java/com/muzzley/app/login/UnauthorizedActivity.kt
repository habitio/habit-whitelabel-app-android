package com.muzzley.app.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.muzzley.R
import com.muzzley.app.LauncherActivity
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.startActivity

class UnauthorizedActivity : AppCompatActivity(){

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unauthorized)

        RxDialogs.confirm(this,getString(R.string.mobile_logout),getString(R.string.mobile_force_logout))
        .subscribe {
            startActivity<LauncherActivity>{
                putExtra("logout",true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }

}