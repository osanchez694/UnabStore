package me.oscarsanchez.unabstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickLogout: () -> Unit = {},
    productViewModel: ProductViewModel = viewModel()
) {
    val auth = Firebase.auth
    val emailUsuario = auth.currentUser?.email ?: "Invitado"

    // UI state
    var showAddDialog by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf(emptyList<Producto>()) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Colores base
    val brand = Color(0xFFFF9900)

    // Cargar productos
    fun loadProducts() {
        productViewModel.obtenerProductos { new ->
            productos = new
        }
    }

    LaunchedEffect(Unit) { loadProducts() }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text("UNAB Shop", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                        Spacer(Modifier.height(6.dp))
                        // chip con correo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                emailUsuario,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                    IconButton(onClick = { /* Carrito */ }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito", tint = Color.White)
                    }
                    FilledTonalButton(
                        onClick = {
                            auth.signOut()
                            onClickLogout()
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                        Spacer(Modifier.width(8.dp))
                        Text("Salir")
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = brand
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Agregar") },
                text = { Text("Agregar") },
                containerColor = brand,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Mis productos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            if (productos.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(brand.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = brand,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Aún no tienes productos", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Toca en “Agregar” para crear el primero", color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { showAddDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar producto")
                    }
                }
            } else {
                // Grid 2×
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productos, key = { it.id ?: it.nombre }) { p ->
                        ProductCard(
                            producto = p,
                            brand = brand,
                            onDelete = {
                                p.id?.let { id ->
                                    productViewModel.eliminarProducto(id) { ok ->
                                        if (ok) {
                                            loadProducts()
                                            scope.launch {
                                                snackbar.showSnackbar("Producto eliminado")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbar.showSnackbar("Error al eliminar")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { nuevo ->
                productViewModel.agregarProducto(nuevo) { ok, msg ->
                    showAddDialog = false
                    loadProducts()
                    scope.launch { snackbar.showSnackbar(if (msg.isBlank()) (if (ok) "Producto guardado" else "Error al guardar") else msg) }
                }
            },
            brand = brand
        )
    }
}

/* ---------- Tarjeta ----------- */
@Composable
private fun ProductCard(
    producto: Producto,
    brand: Color,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {
            // “avatar” del producto
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(brand.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = brand)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                producto.nombre.ifBlank { "Sin nombre" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                producto.descripcion.ifBlank { "Sin descripción" },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "$${"%,.2f".format(producto.precio)}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = brand)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDelete,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Eliminar")
            }
        }
    }
}

/* ---------- Diálogo Agregar ----------- */
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (Producto) -> Unit,
    brand: Color = Color(0xFFFF9900)
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = brand)
                Spacer(Modifier.width(8.dp))
                Text("Agregar producto", fontWeight = FontWeight.Bold)
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
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = precioText,
                    onValueChange = { s ->
                        precioText = s.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Precio") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    if (nombre.isBlank() || precio == null || precio <= 0.0) {
                        errorMsg = "Nombre y precio (> 0) son obligatorios."
                    } else {
                        onAdd(
                            Producto(
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                precio = precio
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = brand)
            ) { Text("Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
