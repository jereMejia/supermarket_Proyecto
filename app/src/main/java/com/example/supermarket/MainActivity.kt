package com.example.supermarket

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import androidx.navigation.compose.*
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextDecoration
import org.json.JSONObject
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap



// Define la familia de fuentes Montserrat
val montserratFamily = FontFamily(
    Font(R.font.montserrat_regular),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_italic, FontWeight.Normal)
)

class MainActivity : ComponentActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.insetsController?.setSystemBarsAppearance(
            0, // Indica el comportamiento deseado para la barra de estado
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS // Para íconos claros en la barra de estado
        )

        // Crear un interceptor para loguear las solicitudes y respuestas HTTP
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        // Crear el cliente OkHttp con el interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Configurar Retrofit con el cliente OkHttp personalizado
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.218.4:3000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        setContent {
            val navController = rememberNavController()
            val (accessToken, _) = getSavedTokens()

            // Lógica para determinar el startDestination según el accessToken y accountStatus
            val startDestination = when {
                accessToken == null -> "login" // Si no existe el accessToken, ir al login
                else -> {
                    val accountStatus = decodeJWT(accessToken)?.getInt("accountStatus") ?: -1

                    // Si accountStatus es 4, navegar a "sendcode", si es 3, a "verify"
                    when (accountStatus) {
                        4 -> "sendcode"  // Redirigir a la pantalla de SendCode
                        3 -> "verify"    // Redirigir a la pantalla de Verify
                        else -> "homeadmin"   // Si no es ni 4 ni 3, ir al home
                    }
                }
            }

            // Configuración del NavHost con la lógica de startDestination
            NavHost(navController = navController, startDestination = startDestination) {
                composable("login") {
                    LoginScreen(navController)
                }
                composable("homeadmin") {
                    HomeAdminScreen(navController)
                }
                composable("homedelivery") {
                    HomeDeliveryScreen(navController)
                }
                composable("homeuser") {
                    HomeUserScreen(navController)
                }
                composable("register") {
                    RegisterScreen(navController) // Pantalla de registro
                }
                composable("verify") {
                    VerifyScreen(navController) // Pantalla de verificación
                }
                composable("sendcode") {
                    SendCodeScreen(navController) // Pantalla para enviar código
                }
                composable("updatepassword") {
                    UpdatePasswordScreen(navController) // Pantalla de actualización de contraseña
                }
                composable("products") {
                    ProductAdminScreen() // Pantalla de actualización de contraseña
                }
            }
        }


    }

    @Composable
    fun LoginScreen(navController: NavController) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loginResponse by remember { mutableStateOf<LoginResponse?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }

        // Validar si los campos están vacíos
        val isFormValid = username.isNotBlank() && password.isNotBlank()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121212))
                        .border(2.dp, Color(0xFF3D74FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Inicio de Sesión",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = montserratFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF),
                                focusedIndicatorColor = Color(0xFF3D74FF),
                                unfocusedIndicatorColor = Color(0xFF3D74FF)
                            )
                        )

                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF),
                                focusedIndicatorColor = Color(0xFF3D74FF),
                                unfocusedIndicatorColor = Color(0xFF3D74FF)
                            )
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(2000)
                                    loginUser(username, password, { response ->
                                        loginResponse = response
                                        errorMessage = null
                                        isLoading = false
                                        showError = false
                                        saveTokens(response.accessToken, response.refreshToken)
                                        val (accessToken, _) = getSavedTokens()
                                        val userType = decodeJWT(accessToken.toString())?.getInt("userType") ?: -1
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(1500)

                                            if (userType == 1) {
                                                // Navegar a la pantalla de actualización de contraseña
                                                navController.navigate("homeadmin") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            if (userType == 2) {
                                                // Navegar a la pantalla de actualización de contraseña
                                                navController.navigate("homedelivery") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            if (userType == 3) {
                                                // Navegar a la pantalla de actualización de contraseña
                                                navController.navigate("homeuser") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        }
                                    }, { error ->
                                        loginResponse = null
                                        errorMessage = error
                                        isLoading = false
                                        showError = true
                                    })
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00bb00),
                                contentColor = Color.White
                            ),
                            enabled = isFormValid  // Deshabilitar el botón si los campos están vacíos
                        ) {
                            Text(
                                "Login",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(40.dp),
                                color = Color(0xFF3D74FF),
                                strokeWidth = 8.dp
                            )
                        }

                        loginResponse?.let {
                            Text(
                                text = "Inicio de sesión exitoso",
                                color = Color.Green,
                                style = TextStyle(
                                    fontFamily = montserratFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                fontSize = 14.sp
                            )
                            LaunchedEffect(Unit) {
                                delay(3000)
                                username = ""
                                password = ""
                                loginResponse = null
                            }
                        }

                        errorMessage?.let {
                            Text(
                                text = "Error: $it",
                                color = Color.Red,
                                style = TextStyle(fontFamily = montserratFamily),
                                modifier = Modifier.padding(16.dp)
                            )
                            LaunchedEffect(Unit) {
                                delay(3000)
                                errorMessage = null
                            }
                        }

                        // Mensaje de advertencia si los campos están vacíos
                        if (username.isBlank() || password.isBlank()) {
                            Text(
                                text = "Por favor, complete ambos campos.",
                                color = Color.Red,
                                style = TextStyle(
                                    fontFamily = montserratFamily,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Text(
                            text = "¿No tienes cuenta? Regístrate",
                            color = Color(0xFF3D74FF),
                            fontFamily = montserratFamily,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable {
                                    navController.navigate("register") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                        )

                        // Botón para olvidar contraseña
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = Color(0xFF3D74FF),
                            fontFamily = montserratFamily,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable {
                                    // Navegar a la pantalla de enviar código
                                    navController.navigate("sendcode") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun RegisterScreen(navController: NavController) {
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var registerResponse by remember { mutableStateOf<RegisterResponse?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        val isFormValid = username.isNotBlank() && email.isNotBlank() && password.isNotBlank()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121212))
                        .border(2.dp, Color(0xFF3D74FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Registro",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = montserratFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF)
                            )
                        )

                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF)
                            )
                        )

                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF)
                            )
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    registerUser(
                                        username, password, email,
                                        onSuccess = { response ->
                                            registerResponse = response
                                            errorMessage = null
                                            isLoading = false
                                            saveTokens(response.accessToken, response.refreshToken)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                delay(2000)
                                                navController.navigate("verify") {
                                                    popUpTo("register") { inclusive = true }
                                                }
                                            }
                                        },
                                        onError = { error ->
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00bb00),
                                contentColor = Color.White
                            ),
                            enabled = isFormValid
                        ) {
                            Text(
                                "Registrarse",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(40.dp),
                                color = Color(0xFF3D74FF),
                                strokeWidth = 8.dp
                            )
                        }

                        registerResponse?.let {
                            Text(
                                text = it.message,
                                color = Color.Green,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        errorMessage?.let {
                            Text(
                                text = "Error: $it",
                                color = Color.Red,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        if (!isFormValid) {
                            Text(
                                text = "Todos los campos son obligatorios.",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        // 🔗 Enlace para ir a login
                        Text(
                            text = "¿Ya tienes cuenta? Inicia sesión",
                            color = Color(0xFF3D74FF),
                            fontFamily = montserratFamily,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .clickable {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun VerifyScreen(navController: NavController) {
        var code by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // Usamos getSavedTokens sin necesidad de pasar sharedPreferences
        val (accessToken, refreshToken) = remember { getSavedTokens() }

        val isCodeValid = code.isNotBlank()

        // Decodificamos el JWT y extraemos el accountStatus
        val accountStatus = remember {
            accessToken?.let { token ->
                val payload = decodeJWT(token)
                payload?.getInt("accountStatus") ?: -1 // Retorna -1 si no encuentra accountStatus
            } ?: -1 // Retorna -1 si no hay token
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121212))
                        .border(2.dp, Color(0xFF3D74FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Verifica tu cuenta",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = montserratFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        TextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Código de verificación", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF)
                            )
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                verifyCode(
                                    accessToken = accessToken ?: "",
                                    refreshToken = refreshToken ?: "",
                                    code = code,
                                    onSuccess = { response ->
                                        successMessage = response.message
                                        errorMessage = null

                                        // Navegar a login después de un retraso
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(2000)
                                            if (accountStatus == 4) {
                                                // Navegar a la pantalla de actualización de contraseña
                                                navController.navigate("updatepassword") {
                                                    popUpTo("verify") { inclusive = true }
                                                }
                                            }else {
                                                // Limpiamos los tokens tras la verificación exitosa
                                                clearTokens()
                                                navController.navigate("login") {
                                                    popUpTo("verify") { inclusive = true }
                                                }
                                            }


                                        }
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                        successMessage = null
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00bb00),
                                contentColor = Color.White
                            ),
                            enabled = isCodeValid
                        ) {
                            Text(
                                "Verificar",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(40.dp),
                                color = Color(0xFF3D74FF),
                                strokeWidth = 8.dp
                            )
                        }

                        successMessage?.let {
                            Text(
                                text = it,
                                color = Color.Green,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        errorMessage?.let {
                            Text(
                                text = "Error: $it",
                                color = Color.Red,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        if (!isCodeValid) {
                            Text(
                                text = "Debes ingresar un código.",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SendCodeScreen(navController: NavController) {
        var email by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // Validación de correo electrónico
        val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121212))
                        .border(2.dp, Color(0xFF3D74FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Enviar código de verificación",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = montserratFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        // Campo para ingresar el correo electrónico
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo electrónico", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF)
                            )
                        )

                        // Botón para enviar el correo
                        Button(
                            onClick = {
                                isLoading = true
                                sendingCode(
                                    email = email,
                                    onSuccess = { response ->
                                        successMessage = "Código enviado exitosamente!"
                                        errorMessage = null

                                        // Guardamos los tokens obtenidos
                                        saveTokens(response.accessToken, response.refreshToken)

                                        // Pequeño retraso para mostrar el mensaje de éxito
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(1500)
                                            // Navegar a la pantalla de verificación
                                            navController.navigate("verify") {
                                                popUpTo("sendcode") { inclusive = true }
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                        successMessage = null
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00bb00),
                                contentColor = Color.White
                            ),
                            enabled = isEmailValid
                        ) {
                            Text(
                                "Enviar Código",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Indicador de carga
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(40.dp),
                                color = Color(0xFF3D74FF),
                                strokeWidth = 8.dp
                            )
                        }

                        // Mensaje de éxito
                        successMessage?.let {
                            Text(
                                text = it,
                                color = Color.Green,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        // Mensaje de error
                        errorMessage?.let {
                            Text(
                                text = "Error: $it",
                                color = Color.Red,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        // Validación de correo
                        if (!isEmailValid) {
                            Text(
                                text = "Ingresa un correo electrónico válido.",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun UpdatePasswordScreen(navController: NavController) {
        var newPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // Usamos getSavedTokens sin necesidad de pasar sharedPreferences
        val (accessToken, refreshToken) = remember { getSavedTokens() }

        val isPasswordValid = newPassword.length >= 6

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121212))
                        .border(2.dp, Color(0xFF3D74FF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Actualiza tu contraseña",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = montserratFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        TextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nueva contraseña", color = Color(0xFF3D74FF)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textStyle = TextStyle(
                                fontFamily = montserratFamily,
                                color = Color(0xFF3D74FF)
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFF1D1D1D),
                                cursorColor = Color(0xFF3D74FF)
                            ),
                            visualTransformation = PasswordVisualTransformation() // To hide the password
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                updatePassword(
                                    accessToken = accessToken ?: "",
                                    refreshToken = refreshToken ?: "",
                                    newPassword = newPassword,
                                    onSuccess = { response ->
                                        successMessage = response.message
                                        errorMessage = null

                                        // Limpiamos los tokens tras la actualización exitosa
                                        clearTokens()

                                        // Navegar a login después de un retraso
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(2000)
                                            navController.navigate("login") {
                                                popUpTo("updatepassword") { inclusive = true }
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                        successMessage = null
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00bb00),
                                contentColor = Color.White
                            ),
                            enabled = isPasswordValid
                        ) {
                            Text(
                                "Actualizar contraseña",
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(40.dp),
                                color = Color(0xFF3D74FF),
                                strokeWidth = 8.dp
                            )
                        }

                        successMessage?.let {
                            Text(
                                text = it,
                                color = Color.Green,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        errorMessage?.let {
                            Text(
                                text = "Error: $it",
                                color = Color.Red,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        if (!isPasswordValid) {
                            Text(
                                text = "La contraseña debe tener al menos 6 caracteres.",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = montserratFamily,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HomeAdminScreen(navController: NavController) {
        val (accessToken, _) = getSavedTokens()
        val username = remember {
            accessToken?.let { token ->
                val payload = decodeJWT(token)
                payload?.getString("username") ?: "Desconocido"
            } ?: "Desconocido"
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Texto de bienvenida con el nombre de usuario
                Text(
                    text = "¡Bienvenido, Admin $username 👋!",
                    style = TextStyle(
                        fontFamily = montserratFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Botón de "Productos"
                Button(
                    onClick = {
                        // Aquí iría la lógica para navegar a la pantalla de productos
                        navController.navigate("products")
                    },
                    modifier = Modifier.padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Verde, por ejemplo
                        contentColor = Color.White
                    )
                ) {
                    Text("Productos", fontFamily = montserratFamily, fontWeight = FontWeight.Bold)
                }

                // Botón para cerrar sesión
                Button(
                    onClick = {
                        clearTokens()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFB4B4B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cerrar sesión", fontFamily = montserratFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun ProductAdminScreen() {
        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Obtener los tokens guardados
        val (accessToken, refreshToken) = getSavedTokens()

        // Realizamos la petición a la API para obtener los productos
        LaunchedEffect(Unit) {
            if (accessToken != null && refreshToken != null) {
                fetchProducts(accessToken, refreshToken) { result, error ->
                    if (result != null) {
                        products = result // Aquí asignamos la lista de productos
                    }
                    if (error != null) {
                        errorMessage = error
                    }
                    isLoading = false
                }
            } else {
                errorMessage = "No se encontraron tokens"
                isLoading = false
            }
        }

        // Vista principal
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Si está cargando, mostramos un indicador de progreso
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFF3D74FF)
                    )
                } else {
                    // Si hay un error, mostramos un mensaje de error
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontFamily = montserratFamily,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Si los productos se cargaron correctamente, mostramos la lista
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(40.dp)) // Este es el espacio extra arriba
                        }

                        items(products) { product ->
                            ProductItem(product)
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun ProductItem(product: Product) {
        Log.d("Base64Image", product.photo ?: "Sin imagen")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF1D1D1D))
                .clip(RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            // Verifica si la foto no es nula
            product.photo?.let { base64 ->
                // Decodifica la cadena Base64 en un array de bytes
                val imageBytes = Base64.decode(base64.substringAfter(",") , Base64.DEFAULT)
                val imageBitmap = imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }

                imageBitmap?.let {
                    // Mostrar la imagen usando base64 decodificada
                    Image(
                        bitmap = it,
                        contentDescription = "Imagen del producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = product.name,
                color = Color.White,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description ?: "Sin descripción",
                color = Color.Gray,
                fontFamily = montserratFamily,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Stock: ${product.stock}",
                color = Color.White,
                fontFamily = montserratFamily,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Precio: Lps ${product.salePrice}",
                color = Color.White,
                fontFamily = montserratFamily,
                fontSize = 16.sp
            )
        }
    }


    @Composable
    fun HomeDeliveryScreen(navController: NavController) {
        val (accessToken, _) = getSavedTokens()
        val username = remember {
            accessToken?.let { token ->
                val payload = decodeJWT(token)
                payload?.getString("username") ?: "Desconocido"
            } ?: "Desconocido"
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Texto de bienvenida con el nombre de usuario
                Text(
                    text = "¡Bienvenido, $username 👋!",
                    style = TextStyle(
                        fontFamily = montserratFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Botón para cerrar sesión
                Button(
                    onClick = {
                        clearTokens()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFB4B4B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cerrar sesión", fontFamily = montserratFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun HomeUserScreen(navController: NavController) {
        val (accessToken, _) = getSavedTokens()
        val username = remember {
            accessToken?.let { token ->
                val payload = decodeJWT(token)
                payload?.getString("username") ?: "Desconocido"
            } ?: "Desconocido"
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Texto de bienvenida con el nombre de usuario
                Text(
                    text = "¡Hola, $username 👋!",
                    style = TextStyle(
                        fontFamily = montserratFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Botón para cerrar sesión
                Button(
                    onClick = {
                        clearTokens()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFB4B4B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cerrar sesión", fontFamily = montserratFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    private fun decodeJWT(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val json = String(decodedBytes, Charsets.UTF_8)

            JSONObject(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun clearTokens() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPreferences.edit { clear() }  // Elimina todos los valores guardados
    }

    // Función para recuperar los tokens de SharedPreferences
    private fun getSavedTokens(): Pair<String?, String?> {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        return Pair(accessToken, refreshToken)
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPreferences.edit {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
        }
    }

    private fun loginUser(
        username: String,
        password: String,
        onSuccess: (LoginResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.login(Login(username, password))
                }

                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: run {
                        onError("Empty response")
                    }
                } else {
                    if (response.code() == 400) {
                        onError("Usuario o contraseña incorrecta")
                    } else {
                        onError("Error: ${response.message()}")
                    }
                }
            } catch (e: IOException) {
                onError("Error de conexión")
                e.printStackTrace()
            } catch (e: Exception) {
                onError("Excepción: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun registerUser(
        username: String,
        password: String,
        email: String,
        onSuccess: (RegisterResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.register(Register(username, password, email))
                }

                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("Respuesta vacía del servidor.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "").getString("error")
                    } catch (_: Exception) {
                        "Error desconocido del servidor"
                    }
                    onError(errorMessage)
                }
            } catch (e: IOException) {
                onError("Error de conexión.")
                e.printStackTrace()
            } catch (e: Exception) {
                onError("Excepción: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun verifyCode(
        accessToken: String,
        refreshToken: String,
        code: String,
        onSuccess: (VerifyResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.verify(Verify(accessToken, refreshToken, code))
                }

                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("Respuesta vacía del servidor.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "").getString("error")
                    } catch (_: Exception) {
                        "Error desconocido del servidor"
                    }
                    onError(errorMessage)
                }
            } catch (e: IOException) {
                onError("Error de conexión.")
                e.printStackTrace()
            } catch (e: Exception) {
                onError("Excepción: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun sendingCode(
        email: String,
        onSuccess: (SendingCodeResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.sendingCode(SendingCode(email))
                }

                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("Respuesta vacía del servidor.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "").getString("error")
                    } catch (_: Exception) {
                        "Error desconocido del servidor"
                    }
                    onError(errorMessage)
                }
            } catch (e: IOException) {
                onError("Error de conexión.")
                e.printStackTrace()
            } catch (e: Exception) {
                onError("Excepción: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updatePassword(
        accessToken: String,
        refreshToken: String,
        newPassword: String,
        onSuccess: (UpdatePasswordResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.updatePassword(UpdatePassword(accessToken, refreshToken, newPassword))
                }

                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("Respuesta vacía del servidor.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "").getString("error")
                    } catch (_: Exception) {
                        "Error desconocido del servidor"
                    }
                    onError(errorMessage)
                }
            } catch (e: IOException) {
                onError("Error de conexión.")
                e.printStackTrace()
            } catch (e: Exception) {
                onError("Excepción: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun fetchProducts(
        accessToken: String,
        refreshToken: String,
        onResult: (List<Product>?, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Realizamos la solicitud a la API para obtener los productos
                val response = withContext(Dispatchers.IO) {
                    apiService.products(Products(accessToken, refreshToken))
                }

                if (response.isSuccessful) {
                    // Si la respuesta es exitosa, obtenemos la lista de productos
                    response.body()?.let {
                        onResult(it.products, null) // Le pasamos los productos
                    } ?: onResult(null, "No se encontraron productos")
                } else {
                    // En caso de error, manejamos el mensaje de error
                    onResult(null, "Error al cargar los productos: ${response.message()}")
                }
            } catch (e: Exception) {
                // En caso de una excepción (conexión, error inesperado, etc.)
                onResult(null, "Error de conexión: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

}