package com.example.sitamu

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sitamu.data.remote.SupabaseRepository
import com.example.sitamu.navigation.NavGraph
import com.example.sitamu.ui.theme.SitamuTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SitamuTheme {

                // =========================
                // TEST KONEKSI SUPABASE
                // =========================
                LaunchedEffect(Unit) {

                    Log.e("SupabaseTest", "TEST STARTED")

                    val repository = SupabaseRepository()

                    runCatching {
                        kotlinx.coroutines.withTimeout(10_000) {
                            repository.getDestinations()
                        }
                    }
                        .onSuccess { destinations ->

                            Log.e(
                                "SupabaseTest",
                                "CONNECTION SUCCESS"
                            )

                            Log.e(
                                "SupabaseTest",
                                "Jumlah destinations = ${destinations.size}"
                            )

                            destinations.forEach { destination ->
                                Log.e(
                                    "SupabaseTest",
                                    "ID=${destination.id}, " +
                                            "Name=${destination.name}, " +
                                            "Division=${destination.division}, " +
                                            "Active=${destination.active}"
                                )
                            }
                        }
                        .onFailure { error ->

                            Log.e(
                                "SupabaseTest",
                                "CONNECTION FAILED: ${error.message}",
                                error
                            )
                        }
                }

                // =========================
                // UI SITAMU
                // =========================

                val navController = rememberNavController()

                NavGraph(
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    SitamuTheme {
        Greeting("Android")
    }
}
