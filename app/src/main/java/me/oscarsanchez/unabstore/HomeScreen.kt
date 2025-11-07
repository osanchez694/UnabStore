package me.oscarsanchez.unabstore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}) {
    val auth = Firebase.auth
    val user = auth.currentUser
    val productViewModel: ProductViewModel = viewModel()
    val scope = rememberCoroutineScope()

    // Estado para los productos
    var productos by remember { mutableStateOf(listOf<Producto>()) }

    // Estados del diálogo
    var showDialog by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    // Cargar productos de Firestore al iniciar
    LaunchedEffect(Unit) {
        productViewModel.obtenerProductos { productos = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Unab Shop",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF9800))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFFFF9800)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🧑 Usuario logueado
            Spacer(modifier = Modifier.height(12.dp))
            Text(user?.email ?: "No hay usuario", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    auth.signOut()
                    onClickLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Cerrar sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Productos disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 📦 Lista de productos
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos) { producto ->
                    ProductCard(
                        producto = producto,
                        onDelete = {
                            scope.launch {
                                productViewModel.eliminarProducto(producto.id ?: "") { ok ->
                                    if (ok) {
                                        productViewModel.obtenerProductos { productos = it }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // ➕ Diálogo para agregar producto
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Agregar producto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") }
                        )
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") }
                        )
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it },
                            label = { Text("Precio") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nuevoProducto = Producto(
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precio.toDoubleOrNull() ?: 0.0
                            )
                            productViewModel.agregarProducto(nuevoProducto) { ok, _ ->
                                if (ok) {
                                    productViewModel.obtenerProductos { productos = it }
                                }
                            }
                            showDialog = false
                            nombre = ""
                            descripcion = ""
                            precio = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductCard(producto: Producto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Text(producto.descripcion, color = Color.Gray)
                Text("Precio: $${producto.precio}", color = Color(0xFFFF9800))
            }
            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}