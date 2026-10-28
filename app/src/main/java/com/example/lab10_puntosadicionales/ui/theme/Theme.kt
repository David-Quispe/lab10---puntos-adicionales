package com.example.lab10_puntosadicionales.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Light=lightColorScheme(
 primary=Color(0xFF764B2C),onPrimary=Color.White,primaryContainer=Color(0xFFF1E2CF),onPrimaryContainer=Color(0xFF4A2E19),
 secondary=Color(0xFF47634E),onSecondary=Color.White,secondaryContainer=Color(0xFFDFEADA),onSecondaryContainer=Color(0xFF263F2B),
 background=Color(0xFFFAF7EF),onBackground=Color(0xFF28251F),surface=Color(0xFFFAF7EF),onSurface=Color(0xFF28251F),
 surfaceContainer=Color(0xFFFFFDFA),surfaceContainerHigh=Color(0xFFEDE8DE),onSurfaceVariant=Color(0xFF656057),
 outline=Color(0xFF81786C),outlineVariant=Color(0xFFD8D0C4),error=Color(0xFFAA3027),onError=Color.White,
)
private val Dark=darkColorScheme(
 primary=Color(0xFFE8B88B),onPrimary=Color(0xFF432A16),primaryContainer=Color(0xFF59412D),onPrimaryContainer=Color(0xFFFFDDC0),
 secondary=Color(0xFFB9D0B2),onSecondary=Color(0xFF213821),secondaryContainer=Color(0xFF344B33),onSecondaryContainer=Color(0xFFD7E8D0),
 background=Color(0xFF1D1C18),onBackground=Color(0xFFEDE8DD),surface=Color(0xFF1D1C18),onSurface=Color(0xFFEDE8DD),
 surfaceContainer=Color(0xFF2A2822),surfaceContainerHigh=Color(0xFF36342C),onSurfaceVariant=Color(0xFFC7BFB2),
 outline=Color(0xFFA39988),outlineVariant=Color(0xFF514C42),
)
private val BooksTypography=Typography(
 headlineLarge=TextStyle(fontFamily=FontFamily.Serif,fontSize=34.sp,lineHeight=39.sp,fontWeight=FontWeight.Bold),
 titleLarge=TextStyle(fontFamily=FontFamily.Serif,fontSize=24.sp,lineHeight=29.sp,fontWeight=FontWeight.Bold),
 titleMedium=TextStyle(fontSize=16.sp,lineHeight=22.sp,fontWeight=FontWeight.SemiBold),
 bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),
 labelLarge=TextStyle(fontSize=14.sp,lineHeight=20.sp,fontWeight=FontWeight.SemiBold),
)
@Composable fun BooksTheme(dark:Boolean=isSystemInDarkTheme(),content:@Composable ()->Unit) {
 MaterialTheme(colorScheme=if(dark) Dark else Light,typography=BooksTypography,content=content)
}
