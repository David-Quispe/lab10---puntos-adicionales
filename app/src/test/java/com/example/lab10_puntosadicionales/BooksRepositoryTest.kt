package com.example.lab10_puntosadicionales

import com.example.lab10_puntosadicionales.data.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BooksRepositoryTest {
 private class FakeService:BooksApi {
  var query="";val details=mutableListOf<String>();var active=0;var peak=0
  override suspend fun search(query:String,limit:Int,key:String?):SearchResponse {this.query=query;return SearchResponse(listOf(VolumeReference("a"),VolumeReference("b"),VolumeReference("a"),VolumeReference("")))}
  override suspend fun detail(id:String,key:String?):Volume {
   active++;peak=maxOf(peak,active);delay(5);details+=id;active--
   return Volume(id,VolumeInfo(title="Libro $id",authors=listOf("Autora"),imageLinks=ImageLinks("http://books.google.com/cover/$id")))
  }
 }
 @Test fun consultaIdsYDetallesConServicioFalsoSinRed()=runBlocking {
  val service=FakeService();val books=NetworkBooksRepository(service).search(" kotlin ")
  assertEquals("kotlin",service.query);assertEquals(listOf("a","b"),books.map {it.id})
  assertEquals(setOf("a","b"),service.details.toSet());assertTrue(service.peak<=3)
  assertEquals("https://books.google.com/cover/a",books.first().coverUrl)
 }
 @Test fun unaConsultaVaciaNoLlamaAlServicio()=runBlocking {
  val service=FakeService();val result=runCatching {NetworkBooksRepository(service).search(" ")}
  assertTrue(result.exceptionOrNull() is IllegalArgumentException);assertEquals("",service.query)
 }
 @Test fun respuestaSinItemsProduceListaVacia()=runBlocking {
  val service=object:BooksApi {
   override suspend fun search(query:String,limit:Int,key:String?)=SearchResponse()
   override suspend fun detail(id:String,key:String?):Volume=error("No debe consultarse")
  }
  assertTrue(NetworkBooksRepository(service).search("no existe").isEmpty())
 }
 @Test fun camposOpcionalesNoRompenElModelo() {
  val b=Volume("a").toBook()
  assertEquals("Sin título",b.title);assertEquals("Autor no disponible",b.authors);assertNull(b.coverUrl);assertNull(b.pages)
  assertNull(httpsCover("file:///private/file"));assertNull(httpsCover(""));assertEquals("https://a.test/a",httpsCover("http://a.test/a"))
 }
 @Test fun retrofitUsaRutasParametrosYGsonCorrectamente()=runBlocking {
  val server=MockWebServer();server.start()
  try {
   val service=Retrofit.Builder().baseUrl(server.url("/books/v1/")).addConverterFactory(GsonConverterFactory.create()).build().create(BooksApi::class.java)
   server.enqueue(MockResponse().setBody("""{"items":[{"id":"libro-1"}]}"""))
   server.enqueue(MockResponse().setBody("""{"id":"libro-1","volumeInfo":{"title":"Prueba","authors":["Ana"],"imageLinks":{"thumbnail":"http://books.google.com/test.jpg"},"pageCount":120}}"""))
   val result=NetworkBooksRepository(service,"solo-clave-de-prueba").search("jazz history")
   val first=server.takeRequest();assertEquals("/books/v1/volumes",first.requestUrl!!.encodedPath);assertEquals("jazz history",first.requestUrl!!.queryParameter("q"));assertEquals("solo-clave-de-prueba",first.requestUrl!!.queryParameter("key"))
   assertEquals("/books/v1/volumes/libro-1",server.takeRequest().requestUrl!!.encodedPath)
   assertEquals("Prueba",result.single().title);assertEquals(120,result.single().pages)
  } finally {server.shutdown()}
 }
 @Test fun cuotaAgotadaSeExplicaSinSimularResultados()=runBlocking {
  val server=MockWebServer();server.start()
  try {
   val service=Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(BooksApi::class.java)
   server.enqueue(MockResponse().setResponseCode(429).setBody("{}"))
   val e=runCatching {NetworkBooksRepository(service).search("kotlin")}.exceptionOrNull() as Exception
   assertTrue(networkMessage(e).contains("límite de consultas"))
  } finally {server.shutdown()}
 }
}
