package me.oscarsanchez.unabstore


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType // Importación estándar
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickLogout: () -> Unit = {},
    // Inyección del ViewModel: Soluciona el error 'Unresolved reference'
    productViewModel: ProductViewModel = viewModel()
) {
    val auth = Firebase.auth
    val user = auth.currentUser

    // ESTADOS DEL UI
    var showAddDialog by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf(emptyList<Producto>()) }
    var mensaje by remember { mutableStateOf("") }

    // Función para recargar la lista de productos
    val loadProducts: () -> Unit = {
        // Llama a la función del ViewModel para obtener los productos de Firestore
        productViewModel.obtenerProductos { newProducts ->
            productos = newProducts
        }
    }

    // Cargar productos al inicio (y cada vez que el componente se monta)
    LaunchedEffect(Unit) {
        loadProducts()
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Unab Shop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(Icons.Filled.Notifications, "Notificaciones")
                    }
                    IconButton(onClick = { /* Carrito */ }) {
                        Icon(Icons.Filled.ShoppingCart, "Carrito")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        onClickLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFFF9900),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF9900),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Agregar producto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Muestra el mensaje de éxito/error y lo oculta después de 3s
            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    color = if (mensaje.contains("Error")) Color.Red else Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
                LaunchedEffect(mensaje) {
                    if (mensaje.isNotBlank()) {
                        delay(3000L)
                        mensaje = ""
                    }
                }
            }

            Text(
                text = "Lista de Productos",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (productos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No hay productos disponibles. ¡Agrega uno!", color = Color.Gray)
                }
            } else {
                // LISTAR PRODUCTOS
                ProductList(
                    productos = productos,
                    onDelete = { id ->
                        productViewModel.eliminarProducto(id) { success ->
                            if (success) {
                                mensaje = "Producto eliminado correctamente."
                                loadProducts() // Recargar la lista tras la eliminación
                            } else {
                                mensaje = "Error al eliminar el producto."
                            }
                        }
                    }
                )
            }
        }
    }

    // AGREGAR PRODUCTO (Diálogo)
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { producto ->
                productViewModel.agregarProducto(producto) { success, msg ->
                    showAddDialog = false
                    mensaje = msg
                    if (success) {
                        loadProducts() // Recargar la lista tras la adición
                    }
                }
            }
        )
    }
}

/**
 * Composable para mostrar la lista de productos (LazyColumn).
 */
@Composable
fun ProductList(productos: List<Producto>, onDelete: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(productos, key = { it.id ?: UUID.randomUUID().toString() }) { producto ->
            ProductItem(producto = producto, onDelete = onDelete)
        }
    }
}

/**
 * Composable para cada item de producto en la lista.
 */
@Composable
fun ProductItem(producto: Producto, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = producto.descripcion,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$ ${String.format("%.2f", producto.precio)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0077B6) // Azul corporativo
                )
            }
            // Botón/ícono para eliminar producto
            IconButton(onClick = { producto.id?.let { onDelete(it) } }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Diálogo Composable para agregar un nuevo producto.
 */
@Composable
fun AddProductDialog(onDismiss: () -> Unit, onAdd: (Producto) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = Color(0xFFFF9900))
                Spacer(Modifier.width(8.dp))
                Text("Agregar Nuevo Producto", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = precioText,
                    onValueChange = {
                        // Permite solo números y un punto decimal
                        precioText = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Precio") },
                    // CAMBIO SOLICITADO: Usar KeyboardType.Number
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = Color.Red, style = LocalTextStyle.current.copy(fontSize = 12.sp), modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precio = precioText.toDoubleOrNull()
                    if (nombre.isBlank() || precio == null || precio <= 0) {
                        errorMsg = "Verifique que todos los campos sean válidos (Nombre, Precio > 0)."
                    } else {
                        val newProduct = Producto(
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            precio = precio
                        )
                        onAdd(newProduct)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900))
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
