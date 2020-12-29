package com.distribuido.obrero

import java.net.Socket
import android.R.attr.start



class Obrero(mSocket: Socket) : AbsComunicacion(mSocket) , Runnable {
    val Hilo: Thread? = Thread(this)
    var mCadena: Cadena? = null
    fun Iniciar() {
        println("Me inicio...")
        Hilo?.start()
    }

    override fun run() {

        var orden: Int
        var mensaje: String
        try {
            while (true) {
                println("Conexion -> estoy en un bucle wiiiii")
                //Lee un codigo de orden
                orden = LeerOrden()
                mensaje = Escuchar()
                println(Hilo?.getName() + " Conexion -> ORDEN : " + orden)
                println(Hilo?.getName() + " Conexion -> MENSAJE : " + mensaje)
                if (orden == 2)
                    break
                else if (orden == 1)
                {
                   Responder(mCadena?.Integrar(mensaje).toString())

                }
                else if(orden == 0)
                {
                    mCadena = Cadena(mensaje)
                }

                //Responder("0");
            }

            System.out.println("Obrero -> Termine de trabajar...");

        } catch (E: Exception) {
            //System.out.println(E.getMessage());
        }

    }

}