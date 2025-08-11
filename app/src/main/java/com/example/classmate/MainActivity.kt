package com.example.classmate

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.classmate.data.repositories.TaskRepository
import com.example.classmate.ui.theme.ClassMateTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    private val app by lazy { applicationContext as ClassmateApp }
    private var systems by mutableStateOf(listOf<ClassMate>())
    override fun onCreate(
        savedInstanceState: Bundle?

    ) {
        super.onCreate(savedInstanceState)
        // Cargar datos iniciales
        lifecycleScope.launch {
            systems = withContext(Dispatchers.IO) {
                app.room.classmateDao().getAll()
            }
        }
        fun generarNumeroDe10Digitos(): Long {
            val min = 1_000_000_000L  // Mínimo número de 10 dígitos
            val max = 9_999_999_999L  // Máximo número de 10 dígitos
            return Random.nextLong(min, max + 1)
        }

        //crearCanalDeNotificacion(this)  // Paso 1
        //mostrarNotificacionesDeHoy(this, taskRepository)  // Paso 3
        enableEdgeToEdge()
        setContent {
            ClassMateTheme {
                if (systems.isEmpty()){
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(16.dp)
                        ) {
                            Text(
                                "Sistemas guardados",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            systems.forEach {
                                Text("• Base activa: ${it.base_activa}, ID FR: ${it.id_Fr}")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val idUser = generarNumeroDe10Digitos()
                                    lifecycleScope.launch {
                                        withContext(Dispatchers.IO) {
                                            app.room.classmateDao().insertAll(
                                                listOf(
                                                    ClassMate(
                                                        id = 0,
                                                        base_activa = true,
                                                        id_Fr = "$idUser"
                                                    )
                                                )
                                            )
                                            systems = app.room.classmateDao().getAll()
                                        }
                                    }
                                }
                            ) {
                                Text("Empezar")
                            }
                        }
                    }
                }else{
                    AppNavigation()
                    Log.d("ClassMateDebug", "Datos existentes:")
                    systems.forEach {
                        Log.d("ClassMateDebug", "ID FR: ${it.id_Fr}, Base activa: ${it.base_activa}")
                    }

                }

            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
//                    1001 // Puedes elegir otro requestCode si ya lo usaste
//                )
//            }
//        }
    }
}



// Componentes de UI reutilizables
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando datos iniciales...")
        }
    }
}

@Composable
private fun ErrorScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error al cargar datos", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button (onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyStateScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No se encontraron datos", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ClassMateTheme {
        Greeting("Android")
    }
}
