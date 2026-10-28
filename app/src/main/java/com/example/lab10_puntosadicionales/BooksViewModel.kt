package com.example.lab10_puntosadicionales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab10_puntosadicionales.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class BooksState(
    val draft:String="kotlin",
    val query:String="",
    val loading:Boolean=false,
    val books:List<Book> = emptyList(),
    val error:String?=null,
    val selected:Book?=null,
    val exampleMode:Boolean=false,
)
class BooksViewModel(private val repository:BooksRepository=BooksContainer.repository,private val examples:BooksRepository=ExampleCatalogContainer.repository):ViewModel() {
    private val mutable=MutableStateFlow(BooksState())
    val state=mutable.asStateFlow()
    private var searchJob:Job?=null
    init {search()}
    fun draft(value:String) {mutable.update {it.copy(draft=value)}}
    fun openExamples() {mutable.update {it.copy(exampleMode=true,books=emptyList(),query="",draft="",selected=null)};search("")}
    fun returnToApi() {mutable.update {it.copy(exampleMode=false,books=emptyList(),query="",draft="kotlin",selected=null)};search("kotlin")}
    fun search(term:String=mutable.value.draft) {
        if(term.isBlank() && !mutable.value.exampleMode) {mutable.update {it.copy(error="Escribe un título, autor o tema para buscar.")};return}
        searchJob?.cancel()
        mutable.update {it.copy(loading=true,error=null,draft=term,selected=null)}
        val selectedRepository=if(mutable.value.exampleMode) examples else repository
        searchJob=viewModelScope.launch {
            try {
                val books=selectedRepository.search(term)
                mutable.update {it.copy(loading=false,books=books,query=term,error=null)}
            } catch(e:CancellationException) {throw e}
            catch(e:Exception) {mutable.update {it.copy(loading=false,error=networkMessage(e))}}
        }
    }
    fun select(book:Book) {mutable.update {it.copy(selected=book)}}
    fun back() {mutable.update {it.copy(selected=null)}}
}
fun networkMessage(e:Exception):String = when {
    e is HttpException && e.code()==429 -> "Google Books alcanzó su límite de consultas. Inténtalo más tarde; puede requerir una clave de API con cuota disponible."
    e is HttpException && e.code()==403 -> "Google Books no autorizó la consulta. Revisa la configuración de la API y su cuota."
    e is IOException -> "No pudimos conectar con Google Books. Revisa tu conexión a Internet y vuelve a intentarlo."
    else -> "No se pudo cargar la biblioteca. Vuelve a intentarlo."
}
