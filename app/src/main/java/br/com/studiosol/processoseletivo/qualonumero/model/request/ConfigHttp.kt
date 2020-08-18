package br.com.studiosol.processoseletivo.qualonumero.model.request

import android.os.AsyncTask
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*

/**
 * Realiza a configuração do request na API.
 */

class ConfigHttp : AsyncTask<Void, Void, JSONObject>()
{

    /**
     * Configura o request para ser realizado no background.
     */

    override fun doInBackground(vararg params: Void?): JSONObject
    {
        val jsonAux = StringBuilder()
        var json = JSONObject()
        try
        {
            val url = URL("https://us-central1-ss-devops.cloudfunctions.net/rand?min=1&max=300")
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "GET"
            conexao.setRequestProperty("Accept", "application/json")
            conexao.connectTimeout = 5000
            conexao.connect()
            val lerJson = Scanner(url.openStream())
            while (lerJson.hasNext()) // enquanto houver retorno do json, armazenar no StringBuilder jsonAux
            {
                jsonAux.append(lerJson.next())
            }
            json = JSONObject(jsonAux.toString()) // realiza conversao do StringBuilder para um objeto json para facilitar o mapeamento posteriormente
        }
        catch (e: MalformedURLException) // url está incorreta retorna a exceção, e armazena no objeto JSON em StatusCode
        {
            json.put("StatusCode",404)
            e.printStackTrace()
        }
        catch (e: IOException) // houve erro no request retorna a exceção, e armazena no objeto JSON em StatusCode
        {
            json.put("StatusCode",502)
            e.printStackTrace()
        }
        catch (e: SocketTimeoutException) // tempo de timeout expirou retorna a exceção, e armazena no objeto JSON em StatusCode
        {
            json.put("StatusCode",504)
            e.printStackTrace()
        }
        return (json)
    }
}