package com.glacei.devchallenge3

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.glacei.devchallenge3.databinding.ActivityMenuPrincipalBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MenuPrincipal : AppCompatActivity() {
    private lateinit var binding: ActivityMenuPrincipalBinding

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance("https://devchallenge3-bd-default-rtdb.europe-west1.firebasedatabase.app").reference

        binding.buttonPerfil.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        // Modifiquem el listener del botó de Duelo
        binding.buttonDuelo.setOnClickListener {
            // En lloc d'iniciar l'activitat directament, mostrem el diàleg d'opcions
            mostrarDialogoJugar()
        }
    }

    /**
     * Mostra un diàleg per escollir entre crear una partida nova o unir-se a una existent.
     */
    private fun mostrarDialogoJugar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Duel")
        builder.setMessage("Vols crear una partida o unir-te a una existent?")

        // Botó per crear una partida nova
        builder.setPositiveButton("Crear") { _, _ ->
            crearNovaPartida()
        }

        // Botó per unir-se a una partida
        builder.setNeutralButton("Unir-se") { _, _ ->
            mostrarDialogoUnirse()
        }

        // Botó per cancel·lar
        builder.setNegativeButton("Cancel·lar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    /**
     * Crea una nova partida a Firebase, obté una ID única i mostra un diàleg per compartir-la.
     */
    private fun crearNovaPartida() {
        val database = FirebaseDatabase.getInstance().reference
        // Genera una ID única utilitzant push() a la branca "partides"
        val gameId = database.child("partides").push().key

        if (gameId.isNullOrEmpty()) {
            Toast.makeText(this, "Error al crear la partida. Intenta-ho de nou.", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrem a l'usuari la ID creada perquè la pugui compartir
        val dialog = AlertDialog.Builder(this)
            .setTitle("Partida Creada")
            .setMessage("Comparteix aquesta ID amb el teu rival:\n\n$gameId")
            .setPositiveButton("D'acord") { _, _ ->
                // En fer clic a "D'acord", naveguem a la partida
                navegarADuelo(gameId, true) // L'usuari que crea és l'atacant (isAttacker = true)
            }
            .setNeutralButton("Copiar ID", null) // Deixem el listener a null de moment
            .setCancelable(false) // Evitem que l'usuari tanqui el diàleg sense prémer el botó
            .create()

        dialog.setOnShowListener {
            val copyButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            copyButton.setOnClickListener {
                // Lògica per copiar la ID al portapapeles
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Game ID", gameId)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "ID copiada al portapapeles!", Toast.LENGTH_SHORT).show()
                // IMPORTANT: No cridem a dialog.dismiss() aquí, per mantenir el diàleg obert
            }

            // El botó "D'acord" tancarà el diàleg automàticament i després executarà la navegació
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                dialog.dismiss()
                navegarADuelo(gameId, true)
            }
        }

        dialog.show()
    }


    /**
     * Mostra un diàleg que demana a l'usuari la ID de la partida a la qual vol unir-se.
     */
    private fun mostrarDialogoUnirse() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Unir-se a Partida")

        // Afegim un camp de text (EditText) al diàleg
        val input = EditText(this)
        input.hint = "Introdueix la ID de la partida"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Botó per confirmar i unir-se
        builder.setPositiveButton("Unir-se") { _, _ ->
            val gameId = input.text.toString().trim()
            if (gameId.isNotEmpty()) {
                // Aquí podríem verificar si la partida existeix a Firebase abans de navegar
                navegarADuelo(gameId, false) // L'usuari que s'uneix no és l'atacant (isAttacker = false)
            } else {
                Toast.makeText(this, "La ID no pot estar buida.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botó per cancel·lar
        builder.setNegativeButton("Cancel·lar") { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    /**
     * Funció centralitzada per iniciar l'activitat Duelos.
     * @param gameId La ID de la partida.
     * @param isAttacker Booleà que indica si l'usuari és qui inicia la partida (true) o qui s'uneix (false).
     */
    private fun navegarADuelo(gameId: String, isAttacker: Boolean) {
        val intent = Intent(this, Duelos::class.java).apply {
            putExtra("GAME_ID", gameId)
            putExtra("IS_ATTACKER", isAttacker)
        }
        startActivity(intent)
    }
}
