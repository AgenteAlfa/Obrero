package com.distribuido.obrero

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

abstract class AbsComunicacion (val mSocket : Socket){

    private var OOS : ObjectOutputStream ?= null
    private var OIS : ObjectInputStream ?= null

    init {
        println("Declarando...")
        OOS = ObjectOutputStream(mSocket.getOutputStream())
        OIS= ObjectInputStream(mSocket.getInputStream())
        println("Ya declare!!!...")
    }

    fun Ordenar(orden: Int, cadena: String) {
        try {
            OOS!!.writeObject(orden)
            OOS!!.writeObject(cadena)
        } catch (e: IOException) {
            //e.printStackTrace();
            println("Error desconocido")
        }

    }

    fun Responder(res: String) {
        try {
            OOS!!.writeObject(res)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun LeerOrden(): Int {
        try {
            if (mSocket.isConnected)
                return OIS!!.readObject() as Int
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return -1
    }

    fun Escuchar(): String {
        try {
            if (mSocket.isConnected)
                return OIS!!.readObject() as String
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return "0"
    }



}