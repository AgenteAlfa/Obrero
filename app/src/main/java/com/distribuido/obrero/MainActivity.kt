package com.distribuido.obrero

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.net.Socket
import android.os.StrictMode
import java.net.InetSocketAddress
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        var mButton : Button  = findViewById(R.id.btn_conectar)
        val ip : EditText = findViewById(R.id.edt_ip)
        val puerto : EditText = findViewById(R.id.edt_puerto)

        mButton.setOnClickListener {

                println("conecandose a " + ip.text.toString() + " por "  + puerto.text.toString().toInt() +  "....")

                var sockAdr = InetSocketAddress(ip.text.toString(), puerto.text.toString().toInt())
                //var sockAdr = InetSocketAddress("192.168.0.14",46214)
                var socket = Socket()
                var timeout = 5000
                socket.connect(sockAdr, timeout)
                val mObrero = Obrero(socket)
                println("conectado!!!!")
                mObrero.Iniciar()

        }
    }
}
