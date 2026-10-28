package com.example.lab10_puntosadicionales

import com.example.lab10_puntosadicionales.data.parsePublicCovers
import org.junit.Assert.*
import org.junit.Test

class ExampleCatalogTest {
 @Test fun documentedJsonpResponseIsParsedWithoutExecutingJavaScript() {
  val response="""bookshelf({"ISBN:9781617293290":{"info_url":"https://books.google.com/books?id=book1","thumbnail_url":"https://books.google.com/cover.png"}});"""
  val result=parsePublicCovers(response)
  assertEquals("https://books.google.com/cover.png",result["ISBN:9781617293290"]?.thumbnailUrl)
 }
 @Test fun unavailableIsbnReturnsEmptyMap() {assertTrue(parsePublicCovers("bookshelf({});").isEmpty())}
}
