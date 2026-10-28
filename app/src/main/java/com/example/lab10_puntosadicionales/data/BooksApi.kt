package com.example.lab10_puntosadicionales.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// La búsqueda entrega IDs; cada volumen se consulta después para obtener sus datos.
data class SearchResponse(val items: List<VolumeReference>? = null)
data class VolumeReference(val id: String = "")
data class Volume(val id: String = "", val volumeInfo: VolumeInfo? = null)
data class VolumeInfo(
    val title: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val publishedDate: String? = null,
    val publisher: String? = null,
    val pageCount: Int? = null,
    val imageLinks: ImageLinks? = null,
)
data class ImageLinks(val thumbnail: String? = null, val smallThumbnail: String? = null)
interface BooksApi {
    @GET("volumes") suspend fun search(@Query("q") query: String, @Query("maxResults") limit: Int = 12, @Query("key") key: String? = null): SearchResponse
    @GET("volumes/{id}") suspend fun detail(@Path("id") id: String, @Query("key") key: String? = null): Volume
}
data class Book(
    val id: String,
    val title: String,
    val authors: String,
    val coverUrl: String?,
    val description: String,
    val publishedDate: String,
    val publisher: String,
    val pages: Int?,
)
fun httpsCover(url: String?): String? = url?.trim()?.takeIf { it.isNotBlank() }?.let {
    val secure = if(it.startsWith("http://")) "https://" + it.removePrefix("http://") else it
    secure.takeIf { value -> value.startsWith("https://") }
}
fun Volume.toBook(): Book {
    val info = volumeInfo
    return Book(id=id,title=info?.title?.takeIf {it.isNotBlank()} ?: "Sin título",
        authors=info?.authors?.filter {it.isNotBlank()}?.joinToString(", ")?.takeIf {it.isNotBlank()} ?: "Autor no disponible",
        coverUrl=httpsCover(info?.imageLinks?.thumbnail ?: info?.imageLinks?.smallThumbnail),
        description=info?.description?.takeIf {it.isNotBlank()} ?: "No hay una descripción disponible para este libro.",
        publishedDate=info?.publishedDate.orEmpty(),publisher=info?.publisher.orEmpty(),pages=info?.pageCount?.takeIf {it>0})
}
