package com.distribuido.obrero

import java.io.IOException
import java.io.StringReader
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList


class Cadena(S: String) {

    enum class STATUS {
        NS, NUMERO, X, CE, ES, EXPONENTE, SIG
    }

    private var Simbolos: ArrayList<Simbolo>
    private var mStatus = STATUS.NS

    private var PunAct = 0
    private var PunAnt = 0
    private var Contador = 0




    init {
        Datos = this
        Simbolos = ArrayList()
        Simbolos.add(Simbolo())
        try {
            Leer(S)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun Leer(S: String) {
        val R = StringReader(S)

        var letra = -1
        var Ultimo: Simbolo? = null
        var leer = true
        letra = if (leer) R.read() else letra
        while ( letra != -1 || mStatus != STATUS.SIG) {

            if (letra == ' '.toInt()) continue else if (letra == -1) mStatus = STATUS.SIG
            leer = true

            Ultimo = Simbolos[Simbolos.size - 1]
            //Imprimir();
            //System.out.println("Cadena -> STATUS : " + mStatus.toString());
            //System.out.println((char)letra);


            var t : Char = letra.toChar();

            when (mStatus) {

                STATUS.NS, STATUS.ES -> {
                    when (t) {
                        '+', '-' -> Ultimo.setPositivo(letra == '+'.toInt())
                        else -> {
                            Ultimo.setPositivo(true)
                            leer = false
                        }
                    }
                    mStatus = STATUS.values()[mStatus.ordinal + 1]
                }
                STATUS.EXPONENTE, STATUS.NUMERO -> when (t) {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> Ultimo.addVal(letra)
                    else -> {
                        leer = false
                        mStatus = STATUS.values()[mStatus.ordinal + 1]
                    }
                }

                STATUS.X -> when (t) {
                    '+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                        leer = false
                        Ultimo.equis = false
                        mStatus = STATUS.SIG
                    }
                    'x', 'X' -> {
                        Ultimo.equis = true
                        mStatus = STATUS.values()[mStatus.ordinal + 1]
                    }
                }
                STATUS.CE -> when (t) {
                    '+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                        leer = false
                        mStatus = STATUS.SIG
                    }
                    '^' -> mStatus = STATUS.values()[mStatus.ordinal + 1]
                }
                STATUS.SIG -> {
                    leer = false
                    if (letra != -1) {
                        Simbolos.add(Simbolo())
                        mStatus = STATUS.NS
                    }
                }
            }
            Ultimo.Corregir()
            letra = if (leer) R.read() else letra
        }
    }

    fun Codificar(): String {
        val builder = StringBuilder()
        for (S in Simbolos) {
            builder.append(S)
        }
        return builder.toString()
    }

    fun Imprimir() {

        for (S in Simbolos) {
            print(S.Codificar() + "\t")
        }
        println()
    }

    fun IntegrarParalelo(intervalos: String): String {

        var R = BigDecimal("0")
        //Ejecutar cadena cad forma : ini fin inter
        val cadena = intervalos.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val A = BigDecimal(cadena[0])
        val B = BigDecimal(cadena[1])
        var I: BigDecimal

        I = BigDecimal(DEFAULT_INTERVALO)
        ////System.out.println("Usare intervalo : " + I.toString());

        //Dado que queremos usar N hilos , aplicaremos la integral de A a un Pm con intervalo I

        val delta = B.subtract(A).divide(BigDecimal(HILOS), Configuracion.ESCALA, RoundingMode.DOWN)
        if (A.subtract(B).abs().compareTo(delta) < 0)
            I = A.subtract(B).abs()


        val HI = arrayOfNulls<HiloIntegrar>(HILOS)
        val Pmed = arrayOfNulls<BigDecimal>(HILOS + 1)//Puntos medios A ... Pm1 ... Pm2 ...... B
        Pmed[0] = A
        Pmed[HILOS] = B

        var tiempo = Date().getTime()
        ////System.out.println("Cadena -> dIntervalo : " + delta.toString());
        for (i in 1 until Pmed.size) {
            Pmed[i] = Pmed[i - 1]!!.add(delta)
            ////System.out.println("Cadena -> INTEGRO DE : " + Pmed[i - 1].toString() + " a " + Pmed[i].toString()) ;
            HI[i - 1] = HiloIntegrar(Pmed[i - 1]!!, Pmed[i]!!, I)
            HI[i - 1]?.start()
        }
        ////System.out.println("Cadena -> EO EMPECE A TRABAJAR");

        for (hi in HI) {
            while ((hi?.resuelto) == false) {
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }

        for (i in 1 until HILOS) {
            R = R.add(HI[i]?.getmRes())
        }
        ////System.out.println("Cadena -> EO ESPERE");
        ////System.out.println("Cadena -> EO TENGO : " + R.toString());
        tiempo = Date().getTime() - tiempo

        //Tiempos[Contador++] = tiempo;
        PunAct += (tiempo / Vueltas).toInt()
        if (Contador.toLong() == Vueltas) {
            if (PunAnt != 0)
                PunAnt = PunAct

            if (PunAct < PunAnt)
            //Antes era mas lento : Vamos mejorando, mejoremos más
                HILOS++
            if (PunAct > PunAnt && HILOS > 1)
            // Ahora es mas lento : Mejoramos suficiente
                HILOS--
            Contador = 0
        }


        //System.out.println("Cadena -> Esta ronda se demoro " + tiempo + "ms para responder " + R.toString());
        //System.out.println("Cadena -> Para la siguiente usaremos " + HILOS + " hilos");

        return R.toString()
    }

    fun Integrar(msj: String): BigDecimal {

        val spliteado = msj.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val A = BigDecimal(spliteado[0])
        val B = BigDecimal(spliteado[1])
        return Integrar(A, B, BigDecimal(DEFAULT_INTERVALO))
    }

    fun Integrar(A: BigDecimal, B: BigDecimal, I: BigDecimal): BigDecimal {
        var A = A
        var I = I

        var R = BigDecimal("0")
        if (I.compareTo(A.subtract(B).abs()) > 0)
            I = A.subtract(B).abs()
        if (A.compareTo(B) > 0) {
            I = I.negate()
        }
        //System.out.println(" Integro en " + A.toString() + " a " + B.toString() + " de " + I.toString());
        while (A.compareTo(B) <= 0) {
            //System.out.println("Agregando...");
            var delta = EvaluarTodo(A)
            delta = delta.multiply(I)
            R = R.add(delta)
            A = A.add(I)

        }


        return R
    }

    private fun EvaluarTodo(X: BigDecimal): BigDecimal {
        var R = BigDecimal("0")
        for (mS in Simbolos) {

            R = R.add(mS.Evaluar(X))
        }//System.out.println("un evaluado...");
        return R
    }

    private inner class HiloIntegrar(
        private val mA: BigDecimal,
        private val mB: BigDecimal,
        private val mI: BigDecimal
    ) : Thread() {
        var resuelto = false
        private var mRes: BigDecimal? = null

        fun getmRes(): BigDecimal? {
            return mRes
        }

        override fun run() {
            resuelto = false
            ////System.out.println("Cadena -> HAGO RUN RUN con " + mA.toString() + " : " + mB.toString()  + " : " + mI.toString() );
            ////System.out.println("Cadena -> SALIDA :  " +
            //Integrar(mA,mB,mI).toString());;
            mRes = Integrar(mA, mB, mI)
            resuelto = true
        }
    }


    private inner class Simbolo() {

        var ValCoc: BigDecimal? = null
        var ValExp: BigDecimal? = null
        public var equis = false
        private var positivo = true

        fun Corregir() {
            if (mStatus == STATUS.X) {
                if (ValCoc == null)
                    ValCoc = if (positivo) BigDecimal("1") else BigDecimal("-1")
            } else if (mStatus == STATUS.SIG) {
                if (ValExp == null)
                    ValExp = if (equis) BigDecimal("1") else BigDecimal("0")
            }
        }

        fun setPositivo(positivo: Boolean) {
            this.positivo = positivo
            ////System.out.println("Cadena -> Seteado : " + positivo);
        }

        fun addVal(c: Int) {
            ////System.out.println("Cadena -> Agregando..." + (char) c);
            if (mStatus == STATUS.NUMERO) {
                val anterior = if (ValCoc != null) ValCoc!!.toString() else ""
                ValCoc = BigDecimal(
                    anterior + c.toChar()
                )
                if (!positivo && ValCoc!!.signum() === 1) {
                    ////System.out.println("Cadena -> Antiguo : " + ValCoc.toString());
                    ValCoc = ValCoc!!.negate()
                    ////System.out.println("Cadena -> Nuevo : " + ValCoc.toString());
                }

            } else {
                val anterior = if (ValExp != null) ValExp!!.toString() else ""
                ValExp = BigDecimal(
                    anterior + c.toChar()
                )
                if (!positivo && ValExp!!.signum() === 1) ValExp = ValExp!!.negate()
            }

        }

        fun Evaluar(X: BigDecimal): BigDecimal {
            ////System.out.println("intalue " + X.toString() + " pow  " + ValExp.toBigInteger().intValue());
            ////System.out.println("sale : " + D.toString());
            return X.pow(ValExp!!.toInt(), MathContext(Configuracion.ESCALA))
                .multiply(
                    ValCoc
                )
        }


        fun Codificar(): String {

            var temp : String = if (ValCoc == null) "?" else ValCoc.toString()
            temp += "X^" + if (ValExp == null) '?' else ValExp!!.toString()
            return  temp
        }

    }

    companion object {
        var Datos : Cadena? = null
        val DEFAULT_INTERVALO = "0.00001"
        private val Vueltas: Long = 10
        private var HILOS = 1
    }
}