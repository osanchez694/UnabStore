package me.oscarsanchez.unabstore

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProductViewModel : ViewModel() {

    private val db = Firebase.firestore

    //  Agregar producto
    fun agregarProducto(producto: Producto, onResult: (Boolean, String) -> Unit) {
        db.collection("Producto")
            .add(producto)
            .addOnSuccessListener {
                onResult(true, "Producto agregado correctamente")
            }
            .addOnFailureListener { e: Exception ->
                onResult(false, "Error al agregar: ${e.message}")
            }
    }

    //  Listar productos
    fun obtenerProductos(onResult: (List<Producto>) -> Unit) {
        db.collection("Producto")
            .get()
            .addOnSuccessListener { result ->
                val productos = result.map { doc ->
                    doc.toObject(Producto::class.java).copy(id = doc.id)
                }
                onResult(productos)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    //  Eliminar producto
    fun eliminarProducto(id: String, onResult: (Boolean) -> Unit) {
        db.collection("Producto").document(id)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false)}
    }
}