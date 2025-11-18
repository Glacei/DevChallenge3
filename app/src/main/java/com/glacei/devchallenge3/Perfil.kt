package com.glacei.devchallenge3 // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.glacei.devchallenge3.databinding.ActivityPerfilBinding // Importa la clase de View Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Perfil : AppCompatActivity() {

    // 1. Declara la variable para View Binding y Firebase Auth
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Infla el layout usando View Binding
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 4. Llama a la función que mostrará los datos del usuario
        mostrarDatosUsuario()
        binding.buttonVolver.setOnClickListener {
            volverMenuPrincipal()
        }
    }

    // Función para mostrar los datos del usuario
    private fun mostrarDatosUsuario() {
        // 5. Obtiene el usuario actual de Firebase
        val usuario: FirebaseUser? = auth.currentUser

        // 6. Comprueba si hay un usuario logueado
        if (usuario != null) {
            // Si hay un usuario, obtenemos su email
            val emailUsuario = usuario.email

            // 7. Asigna el email al TextView.
            // Usamos el ID "textView2" que definiste en tu XML.
            binding.textIdentificador.text = emailUsuario
        } else {
            // 8. (Opcional) Si no hay ningún usuario logueado, muestra un mensaje.
            // Esto no debería pasar si la pantalla de perfil solo es accesible tras el login.
            Toast.makeText(this, "No se encontró ningún usuario", Toast.LENGTH_SHORT).show()
            binding.textIdentificador.text = "Usuario no disponible"
        }
    }

    fun volverMenuPrincipal() {
        // Navega a la Activity principal
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
    }

}
