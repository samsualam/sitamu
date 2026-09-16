package com.example.sitamu.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sitamu.R
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.local.AuthPreferences
import com.example.sitamu.data.repository.AuthRepository
import com.example.sitamu.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

private val SitamuRed = Color(0xFFC62828)
private val LoginBackground = Color(0xFFF8F8F8)
private val LoginTextPrimary = Color(0xFF1C1B1F)
private val LoginTextSecondary = Color(0xFF6F6A70)
private val LoginFieldBorder = Color(0xFFCAC4D0)
private val LoginErrorBackground = Color(0xFFFFE9E7)

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val authPreferences = AuthPreferences(context)
    val repository = AuthRepository(database.adminDao(), authPreferences)
    val viewModel = viewModel { LoginViewModel(repository) }

    LaunchedEffect(viewModel) {
        viewModel.loginSuccess.collectLatest { success ->
            if (success) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }

    LoginContent(
        email = viewModel.email,
        password = viewModel.password,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onEmailChange = { viewModel.email = it },
        onPasswordChange = { viewModel.password = it },
        onLogin = viewModel::onLoginClick
    )
}

@Composable
private fun LoginContent(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    // Keadaan ini sengaja hanya presentasional sampai perilaku "ingat saya" tersedia.
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBackground)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginHero()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Masuk",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LoginTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Masukkan akun admin untuk melanjutkan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LoginTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                LoginTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null)
                    },
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                LoginTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Password",
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLoading
                        ) {
                            PasswordVisibilityIcon(
                                visible = passwordVisible,
                                modifier = Modifier.semantics {
                                    contentDescription = if (passwordVisible) {
                                        "Sembunyikan password"
                                    } else {
                                        "Tampilkan password"
                                    }
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled = !isLoading
                    )
                    Text(
                        text = "Ingat saya",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LoginTextSecondary
                    )
                }

                if (errorMessage != null) {
                    LoginErrorCard(message = errorMessage)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SitamuRed,
                        contentColor = Color.White,
                        disabledContainerColor = SitamuRed.copy(alpha = 0.55f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Masuk",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "KPU Kota Gorontalo",
                    style = MaterialTheme.typography.labelSmall,
                    color = LoginTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Versi 1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = LoginTextSecondary
                )
            }
        }
    }
}

@Composable
private fun LoginHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB3150B0B))
        )
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_kpu),
                contentDescription = "Logo Komisi Pemilihan Umum",
                modifier = Modifier.size(88.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SITAMU",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sistem Informasi Tamu\nKPU Kota Gorontalo",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            // Extra vertical space keeps the input glyphs clear at larger font scales.
            .heightIn(min = 64.dp),
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        singleLine = true,
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = SitamuRed,
            focusedLabelColor = SitamuRed,
            cursorColor = SitamuRed,
            unfocusedBorderColor = LoginFieldBorder,
            focusedLeadingIconColor = SitamuRed,
            focusedTrailingIconColor = SitamuRed
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun LoginErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LoginErrorBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "!",
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SitamuRed)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Login belum berhasil",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SitamuRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = LoginTextPrimary
                )
            }
        }
    }
}

@Composable
private fun PasswordVisibilityIcon(visible: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = 2.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val eyeWidth = size.width * 0.8f
        val eyeHeight = size.height * 0.45f
        val iconColor = LoginTextSecondary

        drawOval(
            color = iconColor,
            topLeft = Offset(center.x - eyeWidth / 2f, center.y - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        drawCircle(
            color = iconColor,
            radius = 3.dp.toPx(),
            center = center
        )
        if (visible) {
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.2f, size.height * 0.2f),
                end = Offset(size.width * 0.8f, size.height * 0.8f),
                strokeWidth = stroke
            )
        }
    }
}
