package com.glacei.devchallenge3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.glacei.devchallenge3.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa la instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance()

        binding.buttonRegistro.setOnClickListener {
            if (binding.editTextUser.text.isNotEmpty() && binding.editTextPassword.text.isNotEmpty()) {
                registrarUsuario(
                    binding.editTextUser.text.toString(),
                    binding.editTextPassword.text.toString()
                )

            }
        }
    }
    // Función para Registrarse  -----------------------------------------------------------------------------
    fun registrarUsuario(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso, el usuario ya está logueado
                    Toast.makeText(baseContext, "Registro exitoso.", Toast.LENGTH_SHORT).show()
                    volverMenuPrincipal()

                } else {
                    // Si el registro falla (ej. email mal formado, contraseña débil)
                    // Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Fallo al registrar: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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