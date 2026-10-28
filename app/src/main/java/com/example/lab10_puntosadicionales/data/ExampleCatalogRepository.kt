package com.example.lab10_puntosadicionales.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URI
import java.text.Normalizer
import java.util.Locale

// Alternativa pública documentada: https://developers.google.com/books/docs/dynamic-links
// Los títulos y autores son ejemplos locales; Google Books devuelve enlaces y portadas por ISBN.
interface PublicCoversApi {
 @GET("books") suspend fun covers(@Query("bibkeys") isbns:String,@Query("jscmd") command:String="viewapi",@Query("callback") callback:String="bookshelf"):ResponseBody
}
data class PublicCover(@SerializedName("info_url") val infoUrl:String?=null,@SerializedName("thumbnail_url") val thumbnailUrl:String?=null)
data class CatalogTitle(val isbn:String,val title:String,val author:String,val topic:String)
val exampleTitles=listOf(
 CatalogTitle("9781617293290","Kotlin in Action","Dmitry Jemerov y Svetlana Isakova","kotlin programación"),
 CatalogTitle("9780132350884","Clean Code","Robert C. Martin","programación código"),
 CatalogTitle("9780134685991","Effective Java","Joshua Bloch","java programación"),
 CatalogTitle("9780135957059","The Pragmatic Programmer","David Thomas y Andrew Hunt","programación"),
 CatalogTitle("9780735211292","Atomic Habits","James Clear","hábitos desarrollo personal"),
 CatalogTitle("9780141439518","Pride and Prejudice","Jane Austen","novela literatura"),
)
fun parsePublicCovers(response:String):Map<String,PublicCover> {
 val body=response.trim().removeSuffix(";").trim()
 val json=if(body.startsWith("bookshelf(")) body.removePrefix("bookshelf(").removeSuffix(")") else body
 val type=object:TypeToken<Map<String,PublicCover>>(){}.type
 return Gson().fromJson<Map<String,PublicCover>>(json,type) ?: emptyMap()
}
class ExampleCatalogRepository(private val api:PublicCoversApi):BooksRepository {
 override suspend fun search(query:String):List<Book> {
  fun normalize(s:String)=Normalizer.normalize(s,Normalizer.Form.NFD).replace("\\p{M}+".toRegex(),"").lowercase(Locale.ROOT)
  val q=normalize(query.trim())
  val selected=exampleTitles.filter {normalize(it.title+" "+it.author+" "+it.topic).contains(q)}
  if(selected.isEmpty()) return emptyList()
  val covers=parsePublicCovers(api.covers(selected.joinToString(",") {"ISBN:${it.isbn}"}).string())
  return selected.map { t ->
   val link=covers["ISBN:${t.isbn}"]
   val id=link?.infoUrl?.let {url -> runCatching {URI(url).rawQuery?.split("&")?.firstOrNull {it.startsWith("id=")}?.substringAfter("id=")}.getOrNull()} ?: "ISBN:${t.isbn}"
   Book(id=id,title=t.title,authors=t.author,coverUrl=httpsCover(link?.thumbnailUrl),
    description="Este título forma parte del catálogo de ejemplo incluido en la aplicación. Su portada y enlace se consultan por ISBN en Google Books. La búsqueda completa de la API REST requiere que Google permita nuevas consultas o una clave con cuota disponible.",
    publishedDate="",publisher="ISBN ${t.isbn}",pages=null)
  }
 }
}
object ExampleCatalogContainer {
 val repository:BooksRepository by lazy {
  val api=Retrofit.Builder().baseUrl("https://books.google.com/").build().create(PublicCoversApi::class.java)
  ExampleCatalogRepository(api)
 }
}
