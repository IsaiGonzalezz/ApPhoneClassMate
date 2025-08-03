package com.example.classmate

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.classmate.data.repositories.HorarioRepository


sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Main : Screen("main", "Inicio", Icons.Filled.Home)
    object AddTask : Screen("add_task", "Agregar Tarea", Icons.Filled.Add)
    object AddNote : Screen("add_note","Agregar Nota",Icons.Filled.NoteAdd)
    object AddExamen : Screen("add_examen","Agregar Examen", Icons.Filled.Book)
    object Horario : Screen("Horario","Horario", Icons.Filled.Schedule)
    object RegistrarHorario : Screen("Agregar Horario","Agregar Horario", Icons.Filled.TableView)
}

@Composable
fun AppNavigation(context: Context = LocalContext.current) {

    val navController = rememberNavController()
    val bottomItems = listOf(Screen.Main, Screen.Horario)
    val horarioRepository = HorarioRepository() // o usar Hilt si estás inyectando

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController,
                items = bottomItems
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route
            ) {
                composable(Screen.Main.route) {
                    MainScreen(navController = navController)
                }
                composable(Screen.AddTask.route) {
                    AddTaskScreen(navController = navController)
                }
                composable(Screen.AddNote.route) {
                    AddNoteScreen(navController = navController)
                }
                composable(Screen.AddExamen.route) {
                    AddExamenScreen(navController = navController)
                }
                composable(Screen.Horario.route) {
                    Horario(
                        navController = navController,
                        horarioRepository = horarioRepository,
                        context = context)
                }
                composable (Screen.RegistrarHorario.route) {
                    HorarioScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    navController: androidx.navigation.NavHostController,
    items: List<Screen>
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
