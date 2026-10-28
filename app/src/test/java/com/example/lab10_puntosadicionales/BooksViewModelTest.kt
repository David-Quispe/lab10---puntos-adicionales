package com.example.lab10_puntosadicionales

import com.example.lab10_puntosadicionales.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BooksViewModelTest {
 private val dispatcher=StandardTestDispatcher()
 @Before fun setup() {Dispatchers.setMain(dispatcher)}
 @After fun clean() {Dispatchers.resetMain()}
 private fun book(id:String)=Book(id,id,"Autora",null,"Texto","","",null)
 @Test fun cargaExitosaExponeLibrosYPermiteVolverDelDetalle()=runTest(dispatcher) {
  val vm=BooksViewModel(object:BooksRepository {override suspend fun search(query:String)=listOf(book(query))})
  advanceUntilIdle();assertEquals("kotlin",vm.state.value.books.single().id)
  vm.select(vm.state.value.books.single());assertNotNull(vm.state.value.selected);vm.back();assertNull(vm.state.value.selected)
 }
 @Test fun errorDeConexionPermiteReintentar()=runTest(dispatcher) {
  var fail=true
  val vm=BooksViewModel(object:BooksRepository {override suspend fun search(query:String):List<Book> {if(fail) throw IOException();return listOf(book("ok"))}})
  advanceUntilIdle();assertNotNull(vm.state.value.error);assertFalse(vm.state.value.loading)
  fail=false;vm.search();advanceUntilIdle();assertNull(vm.state.value.error);assertEquals("ok",vm.state.value.books.single().id)
 }
 @Test fun ultimaBusquedaGanaAunqueLaAnteriorSeaMasLenta()=runTest(dispatcher) {
  val vm=BooksViewModel(object:BooksRepository {override suspend fun search(query:String):List<Book> {delay(if(query=="lenta") 2000 else 10);return listOf(book(query))}})
  advanceUntilIdle();vm.search("lenta");runCurrent();vm.search("rapida");advanceUntilIdle()
  assertEquals("rapida",vm.state.value.query);assertEquals("rapida",vm.state.value.books.single().id)
 }
}
