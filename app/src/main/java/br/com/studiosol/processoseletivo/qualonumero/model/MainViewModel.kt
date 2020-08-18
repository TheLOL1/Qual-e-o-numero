package br.com.studiosol.processoseletivo.qualonumero.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.studiosol.processoseletivo.qualonumero.model.request.ConfigHttp
import org.json.JSONObject
import java.util.concurrent.ExecutionException

/**
 * Coordena a operação do request para a resposta ser passada para MainActivity
 */

class MainViewModel : ViewModel()
{
    var resposta = MutableLiveData<JSONObject>() // armazena o objeto JSON

    /**
     * Realiza o request e armazena em resposta o retorno em formato objeto JSON que poderá conter ou não o número aleátorio gerado (pode haver erro no request), que será passada para MainActivity.
     */

    fun getNumeroAleatorio()
    {
        try
        {
            resposta.value = ConfigHttp().execute().get() // realiza o request e armazena em resposta
        }
        catch (e: InterruptedException) // houve interrupção na thread que realiza o request, retorna a exceção
        {
            e.printStackTrace()
        }
        catch (e: ExecutionException) // houve interrupção no request, retorna a exceção
        {
            e.printStackTrace()
        }
    }
}