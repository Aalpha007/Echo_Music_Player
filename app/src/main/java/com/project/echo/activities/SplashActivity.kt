package com.project.echo.activities

import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.project.echo.R

class SplashActivity : AppCompatActivity() {

    var permissionString = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(!hasPermission(this@SplashActivity, *permissionString)){
            //we have to ask for permissons
            ActivityCompat.requestPermissions(this@SplashActivity, permissionString, 131)
        }
        else{
            starting()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            131 ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED){
                    starting()
                }else {
                    Toast.makeText(this@SplashActivity, "Please grant all these permissions", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                return

            }
            else ->{
                Toast.makeText(this@SplashActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                this.finish()
                return}
        }
    }


    fun hasPermission(context: Context, vararg permissions: String):Boolean{
        var hasAllPermission = true

        for(permission in permissions){
            var res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED){
                hasAllPermission = false
            }
        }
        return  hasAllPermission
    }

    fun starting(){
        Handler().postDelayed({
            var startAct = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(startAct)
            this.finish()
        }, 1000)
    }

}
