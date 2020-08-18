package br.com.studiosol.processoseletivo.qualonumero.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import br.com.studiosol.processoseletivo.qualonumero.R
import br.com.studiosol.processoseletivo.qualonumero.model.MainViewModel
import com.pes.androidmaterialcolorpickerdialog.ColorPicker
import kotlinx.android.synthetic.main.activity_main.*

/**
 * A lógica do programa se baseia que ao inicializar a aplicação é realizado o request na API, se não houver erro no request o número gerado que foi retornado em um objeto JSON, é armazenado em uma váriavel global,
 * a aplicação aguarda o usúario submeter um palpite, após submeter o palpite é realizada a conversão do inteiro para exibição no display da forma que se percorre a string e se um caracter for '2' por exemplo, altera os backgrounds
 * dos segmentos (a, b, d, e) para a cor correspondente ao "led" "aceso" e os segmentos restantes o background é alterado para a cor correspondente ao "led" "apagado", após isso é feito
 * a normalização do display para não ser exibido zero(s) a esquerda dependendo do palpite realizado e por fim é feito a comparação do palpite com o número gerado da API.
 * Se o palpite for maior será exibido no Text View "É menor", se o palpite for menor será exibido no Text View "É maior", se o palpite for igual será exibido no Text View "Acertou!".
 * Quando o usúario acertar o palpite o botão "Nova Partida" é setado como habilitado e se o usúario pressionar é feito um novo request para gerar um novo número aleatório.
 * Caso haja erro no request o status code que foi retornado em um objeto JSON, é armazenado em uma várivel global, é feito a conversão do erro para o display seguindo a mesma lógica do palpite (exceto a parte de percorrer a string)
 * e por fim o botão "Nova Partida" é habilitado, e caso pressionado realiza a mesma ação descrita anteriormente.
 * Para a paleta de cores foi utilizada a API (disponível em: https://github.com/Pes8/android-material-color-picker-dialog) para seleção da cor, e sempre quando o usúario confirmar a seleção de uma cor,o valor em hex desta cor é armazenado
 * em uma váriavel global e é feita a atualização dos segmentos para esta nova cor e posteriormente todos os novos palpites também serão exibidos no display com esta nova cor.
 */

class MainActivity : AppCompatActivity()
{

    lateinit var viewModel : MainViewModel
    var erro : Int = 0 // armazena o status code
    var numeroGerado : Int = 0 // armazena o numero gerado da API
    var ocorreuErro : Boolean = false // define se ocorreu erro ou não
    var corSelecionada : String = "#102A3C" // armazena a cor que o usúario selecionar na paleta de cores (Cor padrao: #102A3C)
    var numeroRecente : String = "0" // armazena o número mais recente que o usúario enviou

    /**
     * Chama o método da ViewModel para realizar o request ao inicializar o aplicativo.
     */

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getNumeroAleatorio()
        viewModel.resposta.observe(this, Observer // fica "observando" caso haja um request que altera "resposta"
        { JSON ->
            try
            {
                numeroGerado = JSON.getInt("value")
                btnNovaPartida.visibility = View.GONE
                btnEnviar.isEnabled = true
                val btnAux = Button(this)
                btnEnviar.background = btnAux.background
                btnEnviar.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))
                ocorreuErro = false
            }
            catch (e: org.json.JSONException) // houve erro ao realizar o request
            {
                erro = JSON.getInt("StatusCode")
                erroToDisplay(erro)
                txtViewResultadosPossiveis.text = "Erro"
                btnNovaPartida.visibility = View.VISIBLE
                btnEnviar.isEnabled = false
                btnEnviar.background = getDrawable(R.color.backgroundEnviarDesabilitado)
                btnEnviar.setTextColor(Color.BLACK)
                ocorreuErro = true
            }
        })
    }

    /**
     * Infla o menu na Action Bar
     * @param menu - o action bar na qual vai ser inflado o item.
     */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menupaletadecores,menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Se o usúario pressionar o botão da paleta de cores do menu, abrirá um dialog para selecionar a cor.
     * @param item - item do menu
     */

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId)
    {
        R.id.paletaCor ->
        {
            val corSelecionadaAux : Int = Color.parseColor(corSelecionada)
            val paletaDeCor = ColorPicker(this,Color.red(corSelecionadaAux),Color.green(corSelecionadaAux),Color.blue(corSelecionadaAux))
            paletaDeCor.show()
            paletaDeCor.enableAutoClose()
            paletaDeCor.setCallback {
                corSelecionada = String.format("#%06X", (0xFFFFFF and it)) // formata a string para hexadecimal
                alterarCorSegmentos() // chama este método após o usúario confirmar a cor
            }
            true
        }
        else ->
        {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Ao retornar do segundo plano, colorir os segmentos novamente devido ao problema de estar "descolorindo".
     */

    override fun onResume() {
        alterarCorSegmentos()
        super.onResume()
    }

    /**
     * Altera a cor dos segmentos que formam o número mais recente que o usúario enviou.
     */

    fun alterarCorSegmentos()
    {
        if (numeroRecente.equals("0"))
        {
            zeroToDisplay(1)
        }
        else
        {
            for (posicao in numeroRecente.indices) // percorre a string
            {
                if (numeroRecente.get(posicao) == '0')
                {
                    zeroToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '1')
                {
                    umToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '2')
                {
                    doisToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '3')
                {
                    tresToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '4')
                {
                    quatroToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '5')
                {
                    cincoToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '6')
                {
                    seisToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '7')
                {
                    seteToDisplay(posicao)
                }
                else if (numeroRecente.get(posicao) == '8')
                {
                    oitoToDisplay(posicao)
                }
                else
                {
                    noveToDisplay(posicao)
                }
            }
        }
    }

    /**
     * Converte o status code de erro para ser exibido no display.
     * @param erroAux - status code recebido
     */

    fun erroToDisplay(erroAux : Int)
    {
        if (erroAux == 404) // Status Code "Not Found"
        {
            quatroToDisplay(0)
            zeroToDisplay(1)
            quatroToDisplay(2)
            numeroRecente = "404"
        }
        else if (erroAux == 502) // Status Code "Bad Gateway"
        {
            cincoToDisplay(0)
            zeroToDisplay(1)
            doisToDisplay(2)
            numeroRecente = "502"
        }
        else // Status Code "Gateway Timeout"
        {
            cincoToDisplay(0)
            zeroToDisplay(1)
            quatroToDisplay(2)
            numeroRecente = "504"
        }
    }

    /**
     * Se houver erro ao realizar o request ou o usúario acertou o palpite com isso irá realizar um novo request se o usúario pressionar o botão de "NOVA PARTIDA" e retornar a aplicação para seu estado inicial.
     * @param view - botão nova partida definido pelo ID "btnNovaPartida" no activity_main.xml
     */

    fun novaPartida (view: View)
    {
        viewModel.getNumeroAleatorio()
        if (!ocorreuErro)
        {
            val hexagonoAux = getDrawable(R.drawable.hexagono)
            val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
            hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
            hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
            txtViewResultadosPossiveis.text = ""
            editTXTPalpite.setText("")
            numeroRecente = "0"
            PrimeiroViewA.background = getDrawable(android.R.color.white)
            PrimeiroViewB.background = getDrawable(android.R.color.white)
            PrimeiroViewC.background = getDrawable(android.R.color.white)
            PrimeiroViewD.background = getDrawable(android.R.color.white)
            PrimeiroViewE.background = getDrawable(android.R.color.white)
            PrimeiroViewF.background = getDrawable(android.R.color.white)
            PrimeiroViewG.background = getDrawable(android.R.color.white)
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = hexagono
            SegundoViewF.background = hexagono
            SegundoViewG.background = getDrawable(R.color.ledApagado)
            TerceiroViewA.background = getDrawable(android.R.color.white)
            TerceiroViewB.background = getDrawable(android.R.color.white)
            TerceiroViewC.background = getDrawable(android.R.color.white)
            TerceiroViewD.background = getDrawable(android.R.color.white)
            TerceiroViewE.background = getDrawable(android.R.color.white)
            TerceiroViewF.background = getDrawable(android.R.color.white)
            TerceiroViewG.background = getDrawable(android.R.color.white)
        }
    }

    /**
     * Se o usúario pressionar o botão "ENVIAR", realiza a conversão do inteiro para ser exibido no display e verifica se o palpite é maior ou menor ou igual o número gerado.
     * @param view - botão enviar definido pelo ID "btnEnviar" no activity_main.xml
     */

    fun enviar (view: View)
    {
        if (!editTXTPalpite.equals("")) // verifica se o usúario submeteu um palpite
        {
            var palpiteAux : String = editTXTPalpite.text.toString().trimStart('0') // remove o(s) zero(s) a esquerda
            editTXTPalpite.setText("")
            if (palpiteAux.isEmpty()) // permitir palpites em casos se for "0" ou "00" ou "000"
            {
                palpiteAux = "0"
            }
            numeroRecente = palpiteAux
            for (posicao in palpiteAux.indices) // percorre a string
            {
                if (palpiteAux.get(posicao) == '0')
                {
                    zeroToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '1')
                {
                    umToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '2')
                {
                    doisToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '3')
                {
                    tresToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '4')
                {
                    quatroToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '5')
                {
                    cincoToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '6')
                {
                    seisToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '7')
                {
                    seteToDisplay(posicao)
                }
                else if (palpiteAux.get(posicao) == '8')
                {
                    oitoToDisplay(posicao)
                }
                else
                {
                    noveToDisplay(posicao)
                }
            }
            verificarQuantidadeDigitos(palpiteAux.length)
            val palpite = palpiteAux.toInt()
            if (palpite > numeroGerado) // se o palpite for maior que o número gerado, exibe no text view "É menor"
            {
                txtViewResultadosPossiveis.text = "É menor"
            }
            else if (palpite < numeroGerado) // se o palpite for menor que o número gerado, exibe no text view "É maior"
            {
                txtViewResultadosPossiveis.text = "É maior"
            }
            else // se o palpite for igual, exibe no text view "Acertou!", desabilita o botão "ENVIAR" e habilita o botão "Nova Partida"
            {
                txtViewResultadosPossiveis.text = "Acertou!"
                btnNovaPartida.visibility = View.VISIBLE
                btnEnviar.isEnabled = false
                btnEnviar.background = getDrawable(R.color.backgroundEnviarDesabilitado)
                btnEnviar.setTextColor(Color.BLACK)
            }
        }
    }

    /**
     * Verifica a quantidade de digitos do palpite para normalizar o display.
     */

    fun verificarQuantidadeDigitos(tamanho: Int)
    {
        if (tamanho == 2) // se for um palpite de 2 digitos, altera o background do terceiro digito do LED para branco
        {
            TerceiroViewA.background = getDrawable(android.R.color.white)
            TerceiroViewB.background = getDrawable(android.R.color.white)
            TerceiroViewC.background = getDrawable(android.R.color.white)
            TerceiroViewD.background = getDrawable(android.R.color.white)
            TerceiroViewE.background = getDrawable(android.R.color.white)
            TerceiroViewF.background = getDrawable(android.R.color.white)
            TerceiroViewG.background = getDrawable(android.R.color.white)
        }
        else if (tamanho == 1) // se for um palpite de 1 digito, altera o background do segundo e terceiro digito do LED para branco
        {
            TerceiroViewA.background = getDrawable(android.R.color.white)
            TerceiroViewB.background = getDrawable(android.R.color.white)
            TerceiroViewC.background = getDrawable(android.R.color.white)
            TerceiroViewD.background = getDrawable(android.R.color.white)
            TerceiroViewE.background = getDrawable(android.R.color.white)
            TerceiroViewF.background = getDrawable(android.R.color.white)
            TerceiroViewG.background = getDrawable(android.R.color.white)
            SegundoViewA.background = getDrawable(android.R.color.white)
            SegundoViewB.background = getDrawable(android.R.color.white)
            SegundoViewC.background = getDrawable(android.R.color.white)
            SegundoViewD.background = getDrawable(android.R.color.white)
            SegundoViewE.background = getDrawable(android.R.color.white)
            SegundoViewF.background = getDrawable(android.R.color.white)
            SegundoViewG.background = getDrawable(android.R.color.white)
        }
    }

    /**
     * Realiza a conversão de 0 para exibição em determinada posição do display.
     */

    fun zeroToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // // se o caracter for igual a 0 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = hexagono
            PrimeiroViewF.background = hexagono
            PrimeiroViewG.background = getDrawable(R.color.ledApagado)
        }
        else if (posicao == 1) // se o caracter for igual a 0 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = hexagono
            SegundoViewF.background = hexagono
            SegundoViewG.background = getDrawable(R.color.ledApagado)
        }
        else if (posicao == 2) // se o caracter for igual a 0 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = hexagono
            TerceiroViewF.background = hexagono
            TerceiroViewG.background = getDrawable(R.color.ledApagado)
        }
    }

    /**
     * Realiza a conversão de 1 para exibição em determinada posição do display.
     */

    fun umToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 1 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = getDrawable(R.color.ledApagado)
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = getDrawable(R.color.ledApagado)
            PrimeiroViewE.background = getDrawable(R.color.ledApagado)
            PrimeiroViewF.background = getDrawable(R.color.ledApagado)
            PrimeiroViewG.background = getDrawable(R.color.ledApagado)
        }
        else if (posicao == 1) // se o caracter for igual a 1 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = getDrawable(R.color.ledApagado)
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = getDrawable(R.color.ledApagado)
            SegundoViewE.background = getDrawable(R.color.ledApagado)
            SegundoViewF.background = getDrawable(R.color.ledApagado)
            SegundoViewG.background = getDrawable(R.color.ledApagado)
        }
        else // se o caracter for igual a 1 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = getDrawable(R.color.ledApagado)
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = getDrawable(R.color.ledApagado)
            TerceiroViewE.background = getDrawable(R.color.ledApagado)
            TerceiroViewF.background = getDrawable(R.color.ledApagado)
            TerceiroViewG.background = getDrawable(R.color.ledApagado)
        }
    }

    /**
     * Realiza a conversão de 2 para exibição em determinada posição do display.
     */

    fun doisToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 2 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = getDrawable(R.color.ledApagado)
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = hexagono
            PrimeiroViewF.background = getDrawable(R.color.ledApagado)
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 2 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = getDrawable(R.color.ledApagado)
            SegundoViewD.background = hexagono
            SegundoViewE.background = hexagono
            SegundoViewF.background = getDrawable(R.color.ledApagado)
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 2 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = getDrawable(R.color.ledApagado)
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = hexagono
            TerceiroViewF.background = getDrawable(R.color.ledApagado)
            TerceiroViewG.background = hexagono
        }
    }

    /**
     * Realiza a conversão de 3 para exibição em determinada posição do display.
     */

    fun tresToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 3 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = getDrawable(R.color.ledApagado)
            PrimeiroViewF.background = getDrawable(R.color.ledApagado)
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 3 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = getDrawable(R.color.ledApagado)
            SegundoViewF.background = getDrawable(R.color.ledApagado)
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 3 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = getDrawable(R.color.ledApagado)
            TerceiroViewF.background = getDrawable(R.color.ledApagado)
            TerceiroViewG.background = hexagono
        }
    }

    /**
     * Realiza a conversão de 4 para exibição em determinada posição do display.
     */

    fun quatroToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 4 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = getDrawable(R.color.ledApagado)
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = getDrawable(R.color.ledApagado)
            PrimeiroViewE.background = getDrawable(R.color.ledApagado)
            PrimeiroViewF.background = hexagono
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 4 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = getDrawable(R.color.ledApagado)
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = getDrawable(R.color.ledApagado)
            SegundoViewE.background = getDrawable(R.color.ledApagado)
            SegundoViewF.background = hexagono
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 4 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = getDrawable(R.color.ledApagado)
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = getDrawable(R.color.ledApagado)
            TerceiroViewE.background = getDrawable(R.color.ledApagado)
            TerceiroViewF.background = hexagono
            TerceiroViewG.background = hexagono
        }
    }

    /**
     * Realiza a conversão de 5 para exibição em determinada posição do display.
     */

    fun cincoToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 5 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = getDrawable(R.color.ledApagado)
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = getDrawable(R.color.ledApagado)
            PrimeiroViewF.background = hexagono
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 5 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = getDrawable(R.color.ledApagado)
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = getDrawable(R.color.ledApagado)
            SegundoViewF.background = hexagono
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 5 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = getDrawable(R.color.ledApagado)
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = getDrawable(R.color.ledApagado)
            TerceiroViewF.background = hexagono
            TerceiroViewG.background = hexagono
        }
    }

    /**
     * Realiza a conversão de 6 para exibição em determinada posição do display.
     */

    fun seisToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 6 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = getDrawable(R.color.ledApagado)
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = hexagono
            PrimeiroViewF.background = hexagono
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 6 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = getDrawable(R.color.ledApagado)
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = hexagono
            SegundoViewF.background = hexagono
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 6 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = getDrawable(R.color.ledApagado)
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = hexagono
            TerceiroViewF.background = hexagono
            TerceiroViewG.background = hexagono
        }
    }

    /**
     * Realiza a conversão de 7 para exibição em determinada posição do display.
     */

    fun seteToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 7 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = getDrawable(R.color.ledApagado)
            PrimeiroViewE.background = getDrawable(R.color.ledApagado)
            PrimeiroViewF.background = getDrawable(R.color.ledApagado)
            PrimeiroViewG.background = getDrawable(R.color.ledApagado)
        }
        else if (posicao == 1) // se o caracter for igual a 7 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = getDrawable(R.color.ledApagado)
            SegundoViewE.background = getDrawable(R.color.ledApagado)
            SegundoViewF.background = getDrawable(R.color.ledApagado)
            SegundoViewG.background = getDrawable(R.color.ledApagado)
        }
        else // se o caracter for igual a 7 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = getDrawable(R.color.ledApagado)
            TerceiroViewE.background = getDrawable(R.color.ledApagado)
            TerceiroViewF.background = getDrawable(R.color.ledApagado)
            TerceiroViewG.background = getDrawable(R.color.ledApagado)
        }
    }

    /**
     * Realiza a conversão de 8 para exibição em determinada posição do display.
     */

    fun oitoToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 8 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = hexagono
            PrimeiroViewF.background = hexagono
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 8 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = hexagono
            SegundoViewF.background = hexagono
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 8 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = hexagono
            TerceiroViewF.background = hexagono
            TerceiroViewG.background = hexagono
        }
    }

    /**
     * Realiza a conversão de 9 para exibição em determinada posição do display.
     */

    fun noveToDisplay(posicao : Int)
    {
        val hexagonoAux = getDrawable(R.drawable.hexagono)
        val hexagono = DrawableCompat.wrap(hexagonoAux!!.mutate())
        hexagono.setTintMode(PorterDuff.Mode.SRC_IN)
        hexagono.setTint(Color.parseColor(corSelecionada)) // altera a cor do hexagono para a selecionada atual
        if (posicao == 0) // se o caracter for igual a 9 e for o primeiro digito, "acende" as leds correspondentes ao primeiro digito
        {
            PrimeiroViewA.background = hexagono
            PrimeiroViewB.background = hexagono
            PrimeiroViewC.background = hexagono
            PrimeiroViewD.background = hexagono
            PrimeiroViewE.background = getDrawable(R.color.ledApagado)
            PrimeiroViewF.background = hexagono
            PrimeiroViewG.background = hexagono
        }
        else if (posicao == 1) // se o caracter for igual a 9 e for o segundo digito, "acende" as leds correspondentes ao segundo digito
        {
            SegundoViewA.background = hexagono
            SegundoViewB.background = hexagono
            SegundoViewC.background = hexagono
            SegundoViewD.background = hexagono
            SegundoViewE.background = getDrawable(R.color.ledApagado)
            SegundoViewF.background = hexagono
            SegundoViewG.background = hexagono
        }
        else // se o caracter for igual a 9 e for o terceiro digito, "acende" as leds correspondentes ao terceiro digito
        {
            TerceiroViewA.background = hexagono
            TerceiroViewB.background = hexagono
            TerceiroViewC.background = hexagono
            TerceiroViewD.background = hexagono
            TerceiroViewE.background = getDrawable(R.color.ledApagado)
            TerceiroViewF.background = hexagono
            TerceiroViewG.background = hexagono
        }
    }
}
