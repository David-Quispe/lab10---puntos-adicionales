package com.example.lab10_puntosadicionales

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lab10_puntosadicionales.data.Book
import com.example.lab10_puntosadicionales.ui.theme.BooksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksApp(vm:BooksViewModel=viewModel()) {
 val s by vm.state.collectAsStateWithLifecycle()
 val context=LocalContext.current
 val prefs=remember {context.getSharedPreferences("appearance",Context.MODE_PRIVATE)}
 var dark by rememberSaveable {mutableStateOf(prefs.getBoolean("dark",false))}
 SideEffect {(context as? Activity)?.window?.let {WindowCompat.getInsetsController(it,it.decorView).apply {isAppearanceLightStatusBars=!dark;isAppearanceLightNavigationBars=!dark}}}
 BackHandler(s.selected!=null) {vm.back()}
 BooksTheme(dark) {
  Scaffold(topBar={TopAppBar(title={Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   if(s.selected==null) Icon(painterResource(R.drawable.ic_books),null,tint=MaterialTheme.colorScheme.primary)
   Text("Bookshelf",fontFamily=FontFamily.Serif,fontWeight=FontWeight.Bold)
  }},navigationIcon={if(s.selected!=null) IconButton(onClick=vm::back) {Icon(Icons.AutoMirrored.Filled.ArrowBack,"Volver a la biblioteca")}},
   actions={IconButton(onClick={dark=!dark;prefs.edit().putBoolean("dark",dark).apply()}) {Icon(painterResource(if(dark) R.drawable.ic_theme_sun else R.drawable.ic_theme_moon),if(dark) "Activar tema claro" else "Activar tema oscuro")}},
   colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background))}) {padding ->
    Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.TopCenter) {
     Box(Modifier.widthIn(max=1000.dp).fillMaxSize()) {
      s.selected?.let {BookDetail(it,s.exampleMode)} ?: Library(s,vm::draft,{vm.search()},{vm.search(it)},vm::select,vm::openExamples,vm::returnToApi)
     }
    }
   }
 }
}
@Composable private fun Library(s:BooksState,change:(String)->Unit,search:()->Unit,topic:(String)->Unit,select:(Book)->Unit,examples:()->Unit,returnToApi:()->Unit) {
 val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
 val submit={focus.clearFocus();keyboard?.hide();search()}
 val grid=rememberLazyGridState()
 LaunchedEffect(s.query) {grid.scrollToItem(0)}
 LazyVerticalGrid(columns=GridCells.Adaptive(150.dp),state=grid,modifier=Modifier.fillMaxSize().testTag("booksGrid"),contentPadding=PaddingValues(24.dp),horizontalArrangement=Arrangement.spacedBy(18.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
  item(span={GridItemSpan(maxLineSpan)}) {
   Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
    if(s.exampleMode) Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(14.dp)) {
     Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
      Text("Catálogo de ejemplo",fontWeight=FontWeight.Bold)
      Text("Títulos locales y portadas consultadas por ISBN en Google Books. La API REST tiene la cuota agotada.",style=MaterialTheme.typography.bodyMedium)
      TextButton(onClick=returnToApi,enabled=!s.loading) {Text("Volver a la búsqueda de la API")}
     }
    }
    Text("TU PRÓXIMA LECTURA",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelMedium,letterSpacing=2.sp)
    Text("Un libro puede\ncambiar tu día.",style=MaterialTheme.typography.headlineLarge)
    Text("Explora títulos, autores y nuevas ideas.",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyLarge)
    OutlinedTextField(value=s.draft,onValueChange=change,singleLine=true,label={Text(if(s.exampleMode) "Buscar en el catálogo de ejemplo" else "Título, autor o tema")},leadingIcon={Icon(Icons.Default.Search,null)},
     trailingIcon={if(s.draft.isNotEmpty()) IconButton(onClick={change("")}) {Icon(Icons.Default.Close,"Limpiar búsqueda")}},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),keyboardOptions=KeyboardOptions(imeAction=ImeAction.Search),keyboardActions=KeyboardActions(onSearch={submit()}))
    Button(onClick=submit,enabled=!s.loading,modifier=Modifier.fillMaxWidth().heightIn(min=50.dp),shape=RoundedCornerShape(14.dp)) {Text(if(s.loading) "Buscando libros…" else "Buscar libros")}
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
     listOf("Kotlin","Historia","Novela").forEach {name -> SuggestionChip(onClick={focus.clearFocus();keyboard?.hide();topic(name.lowercase())},label={Text(name)},modifier=Modifier.heightIn(min=48.dp),enabled=!s.loading)}
    }
   }
  }
  if(s.loading) item(span={GridItemSpan(maxLineSpan)}) {
   Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {LinearProgressIndicator(Modifier.fillMaxWidth());Text("Consultando Google Books y preparando las portadas…",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  }
  s.error?.let {message -> item(span={GridItemSpan(maxLineSpan)}) {
   Surface(color=MaterialTheme.colorScheme.errorContainer,contentColor=MaterialTheme.colorScheme.onErrorContainer,shape=RoundedCornerShape(18.dp)) {
    Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
     Icon(Icons.Default.Info,null);Text("No pudimos cargar los libros",style=MaterialTheme.typography.titleLarge);Text(message,style=MaterialTheme.typography.bodyMedium)
     OutlinedButton(onClick=search,enabled=!s.loading) {Icon(Icons.Default.Refresh,null);Spacer(Modifier.width(8.dp));Text("Reintentar")}
     if(!s.exampleMode) TextButton(onClick=examples,enabled=!s.loading) {Text("Abrir catálogo de ejemplo")}
    }
   }
  }}
  if(s.query.isNotEmpty() || s.exampleMode) item(span={GridItemSpan(maxLineSpan)}) {
   Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
    Text(if(s.query.isEmpty()) "Selección de ejemplo" else "Resultados para «${s.query}»",style=MaterialTheme.typography.titleLarge)
    Text("${s.books.size} ${if(s.books.size==1) "libro" else "libros"} · ${if(s.exampleMode) "Catálogo de ejemplo" else "Google Books"}",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium)
   }
  }
  if(!s.loading && s.error==null && (s.query.isNotEmpty() || s.exampleMode) && s.books.isEmpty()) item(span={GridItemSpan(maxLineSpan)}) {Text("No encontramos libros con esa búsqueda. Prueba otro título, autor o tema.",style=MaterialTheme.typography.bodyLarge)}
  items(s.books,key={it.id}) {book -> BookCard(book) {select(book)} }
  item(span={GridItemSpan(maxLineSpan)}) {Text(if(s.exampleMode) "Datos de ejemplo. Portadas y enlaces de Google Books." else "Información y portadas de Google Books.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}
@Composable private fun BookCard(book:Book,onClick:()->Unit) {
 Card(onClick=onClick,modifier=Modifier.fillMaxWidth().testTag("book-${book.id}"),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainer)) {
  Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Cover(book,Modifier.fillMaxWidth().aspectRatio(.68f))
   Text(book.title,style=MaterialTheme.typography.titleMedium,maxLines=3,overflow=TextOverflow.Ellipsis)
   Text(book.authors,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)
  }
 }
}
@Composable private fun Cover(book:Book,modifier:Modifier) {
 var loading by remember(book.coverUrl) {mutableStateOf(book.coverUrl!=null)}
 var failed by remember(book.coverUrl) {mutableStateOf(book.coverUrl==null)}
 Box(modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),contentAlignment=Alignment.Center) {
  if(book.coverUrl!=null) AsyncImage(model=book.coverUrl,contentDescription="Portada de ${book.title}",modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Fit,onSuccess={loading=false;failed=false},onError={loading=false;failed=true})
  if(loading) CircularProgressIndicator(Modifier.size(28.dp),strokeWidth=2.dp)
  if(failed) Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Icon(painterResource(R.drawable.ic_books),null,Modifier.size(36.dp),tint=MaterialTheme.colorScheme.onPrimaryContainer)
   Text("Portada no disponible",color=MaterialTheme.colorScheme.onPrimaryContainer,style=MaterialTheme.typography.bodyMedium)
  }
 }
}
@Composable private fun BookDetail(book:Book,exampleMode:Boolean) {
 val uri=LocalUriHandler.current
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  if(exampleMode) Text("Catálogo de ejemplo · Portada de Google Books",color=MaterialTheme.colorScheme.secondary,style=MaterialTheme.typography.labelLarge)
  Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {Cover(book,Modifier.widthIn(max=240.dp).fillMaxWidth(.65f).aspectRatio(.68f))}
  Text(book.title,style=MaterialTheme.typography.headlineLarge)
  Text(book.authors,style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.primary)
  val details=listOfNotNull(book.publishedDate.takeIf {it.isNotBlank()},book.pages?.let {"$it páginas"},book.publisher.takeIf {it.isNotBlank()})
  if(details.isNotEmpty()) Text(details.joinToString(" · "),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
  Text("Sobre este libro",style=MaterialTheme.typography.titleLarge)
  Text(remember(book.description) {HtmlCompat.fromHtml(book.description,HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()},style=MaterialTheme.typography.bodyLarge)
  OutlinedButton(onClick={uri.openUri(if(book.id.startsWith("ISBN:")) "https://books.google.com/books?vid=${android.net.Uri.encode(book.id)}" else "https://books.google.com/books?id=${android.net.Uri.encode(book.id)}")},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) {Text("Ver en Google Books")}
 }
}
