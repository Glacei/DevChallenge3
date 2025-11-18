package com.glacei.devchallenge3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.glacei.devchallenge3.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {

    // 1. Declara la variable de FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Inicializa la instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance()

    }

    // Función para iniciar sesión ---------------------------------------------------------------------------
    fun iniciarSesion(email: String, password: String) {

        // ⚠️ Muestra un indicador de carga (ProgressBar) aquí

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                // ⚠️ Oculta el indicador de carga aquí

                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    val user = auth.currentUser
                    // Navega a la Activity principal
                    // Log.d("TAG", "signInWithEmail:success")
                    volverMenuPrincipal()

                } else {
                    // Si el inicio de sesión falla, muestra un mensaje
                    // Log.w("TAG", "signInWithEmail:failure", task.exception)
                    // Ejemplo: Muestra un Toast con el mensaje de error
                    Toast.makeText(baseContext, "Fallo de autenticación: Credenciales inválidas.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    // -------------------------------------------------------------------------------------------------------

    fun volverMenuPrincipal() {
        // Navega a la Activity principal
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
    }
}