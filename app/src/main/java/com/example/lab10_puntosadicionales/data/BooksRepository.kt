package com.example.lab10_puntosadicionales.data

import com.example.lab10_puntosadicionales.BuildConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface BooksRepository {
    suspend fun search(query: String): List<Book>
}
class NetworkBooksRepository(private val service: BooksApi, private val apiKey: String? = null): BooksRepository {
    override suspend fun search(query: String): List<Book> = coroutineScope {
        val term=query.trim()
        require(term.isNotBlank()) {"Escribe un título, autor o tema."}
        val ids=service.search(term,key=apiKey).items.orEmpty().map {it.id}.filter {it.isNotBlank()}.distinct()
        val semaphore=Semaphore(3)
        ids.map { id -> async {semaphore.withPermit {service.detail(id,key=apiKey).toBook()}} }.awaitAll()
    }
}
object BooksContainer {
    val repository: BooksRepository by lazy {
        val service=Retrofit.Builder().baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(BooksApi::class.java)
        NetworkBooksRepository(service,BuildConfig.GOOGLE_BOOKS_API_KEY.takeIf {it.isNotBlank()})
    }
}
