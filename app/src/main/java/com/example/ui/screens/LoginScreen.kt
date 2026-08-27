package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.ui.theme.*
import com.example.util.BiometricAuthHelper

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("lishnilai_auth", Context.MODE_PRIVATE)
    }

    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun verifyPassword() {
        val currentPassword = sharedPrefs.getString("app_password", "admin28") ?: "admin28"
        if (passwordInput.trim() == currentPassword) {
            errorMessage = null
            Toast.makeText(context, "Berhasil masuk ke LishNilai", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        } else {
            errorMessage = "Kata sandi salah. Silakan periksa kembali."
        }
    }

    fun attemptBiometric() {
        val activity = context as? FragmentActivity
        if (activity != null) {
            BiometricAuthHelper.showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    Toast.makeText(context, "Autentikasi Berhasil!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                },
                onError = { err ->
                    Toast.makeText(context, "Gunakan Kata Sandi: $err", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flat Logo Container
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    color = SurfaceBackground,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, OutlineLight)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_checklist_1787747912802),
                            contentDescription = "Logo LishNilai",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                // App Title & Tagline in High Contrast Black
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LishNilai",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceLight,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Aplikasi Penilaian & Rekap Nilai Siswa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceVariantLight,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    color = OutlineLight,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Masuk Akun Guru",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )

                // Password Input Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Kata Sandi", color = OnSurfaceVariantLight) },
                    placeholder = { Text("Masukkan kata sandi...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password", tint = TealPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Sembunyikan sandi" else "Tampilkan sandi",
                                tint = OnSurfaceVariantLight
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { verifyPassword() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceLight,
                        unfocusedTextColor = OnSurfaceLight,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = OutlineLight,
                        focusedLabelColor = TealPrimary,
                        unfocusedLabelColor = OnSurfaceVariantLight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input_field")
                )

                // Error Message Display
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Surface(
                            color = RedErrorContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedErrorBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = OnRedErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = { verifyPassword() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("password_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Alternative Biometric Option
                OutlinedButton(
                    onClick = { attemptBiometric() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("fingerprint_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OnSurfaceLight
                    )
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Masuk dengan Sidik Jari", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OnSurfaceLight)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Kurikulum Merdeka & Standar Penilaian SMK",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

