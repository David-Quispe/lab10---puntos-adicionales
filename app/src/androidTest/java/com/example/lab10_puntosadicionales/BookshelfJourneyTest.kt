package com.example.lab10_puntosadicionales

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Recorrido de la aplicación real. No inyecta libros ni sustituye las respuestas de Google. */
class BookshelfJourneyTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private fun capture(name:String) {
  compose.waitForIdle();SystemClock.sleep(1200)
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  val folder=File(instrumentation.targetContext.getExternalFilesDir(null),"evidencias").apply {mkdirs()}
  instrumentation.uiAutomation.takeScreenshot().let {bitmap -> File(folder,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
 }
 @Test fun realNetworkCatalogueAndDetail() {
  compose.waitUntil(45000) {compose.onAllNodesWithText("Abrir catálogo de ejemplo").fetchSemanticsNodes().isNotEmpty() || compose.onAllNodesWithText("libros · Google Books",substring=true).fetchSemanticsNodes().isNotEmpty()}
  capture("01-api-real")
  if(compose.onAllNodesWithText("Abrir catálogo de ejemplo").fetchSemanticsNodes().isNotEmpty()) {
   compose.onNodeWithText("Abrir catálogo de ejemplo").performScrollTo().performClick()
   SystemClock.sleep(12000)
   capture("02-catalogo-identificado")
   compose.onNodeWithTag("booksGrid").performScrollToNode(hasText("Kotlin in Action"))
   compose.onNodeWithTag("booksGrid").performScrollToIndex(1)
   SystemClock.sleep(7000)
   capture("03-portadas")
   compose.onNodeWithText("Kotlin in Action").performClick()
   capture("04-detalle")
   compose.onNodeWithText("Sobre este libro").performScrollTo()
   capture("05-detalle-isbn")
   compose.onNodeWithContentDescription("Volver a la biblioteca").performClick()
   compose.onNodeWithContentDescription("Activar tema oscuro").performClick()
   compose.onNodeWithTag("booksGrid").performScrollToIndex(1)
   SystemClock.sleep(2000)
   capture("06-tema-oscuro")
   compose.onNodeWithContentDescription("Activar tema claro").performClick()
   compose.onNodeWithTag("booksGrid").performScrollToIndex(0)
   compose.onNodeWithText("Buscar en el catálogo de ejemplo").performTextInput("novela")
   compose.onNodeWithText("Buscar libros").performClick()
   SystemClock.sleep(7000)
   compose.onNodeWithTag("booksGrid").performScrollToNode(hasText("Pride and Prejudice"))
   compose.onNodeWithTag("booksGrid").performScrollToIndex(1)
   SystemClock.sleep(3500);capture("07-busqueda")
  }
 }
}
