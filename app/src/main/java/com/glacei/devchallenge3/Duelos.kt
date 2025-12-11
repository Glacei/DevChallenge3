package com.glacei.devchallenge3

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.glacei.devchallenge3.databinding.ActivityDuelosBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Duelos : AppCompatActivity() {

    private lateinit var binding: ActivityDuelosBinding
    private lateinit var database: DatabaseReference

    private var gameId: String = ""
    private var isAttacker: Boolean = false
    private var mySelection: String? = null

    // Listener per al resultat final de la partida
    private var gameResultListener: ValueEventListener? = null
    private lateinit var gameResultRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuelosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Recuperem dades de l'Intent
        gameId = intent.getStringExtra("GAME_ID") ?: ""
        isAttacker = intent.getBooleanExtra("IS_ATTACKER", false)

        binding.tvGameId.text = "CODI: $gameId" // Mostrem el codi de la partida a la UI

        if (gameId.isEmpty()) {
            Toast.makeText(this, "Error: ID de partida no vàlid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Mostrem el rol del jugador
        binding.tvRoleInfo.text = if (isAttacker) "ROL: ATACANT" else "ROL: DEFENSA"

        // 3. Inicialitzem Firebase
        database = FirebaseDatabase.getInstance("https://devchallenge3-bd-default-rtdb.europe-west1.firebasedatabase.app").reference

        setupGridButtons()

        // 4. Comencem a escoltar el resultat final de la partida
        listenForGameResult()
    }

    /**
     * Neteja el listener de Firebase quan l'activitat es destrueix per evitar pèrdues de memòria.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Eliminem el listener si l'usuari tanca l'activitat
        gameResultListener?.let {
            gameResultRef.removeEventListener(it)
        }
    }

    private fun setupGridButtons() {
        val buttons = mapOf(
            binding.a1 to "a1", binding.a2 to "a2", binding.a3 to "a3",
            binding.b1 to "b1", binding.b2 to "b2", binding.b3 to "b3",
            binding.c1 to "c1", binding.c2 to "c2", binding.c3 to "c3"
        )

        for ((button, buttonKey) in buttons) {
            button.setOnClickListener {
                if (mySelection == null) {
                    onButtonSelected(button, buttonKey)
                }
            }
        }
    }

    private fun onButtonSelected(button: Button, buttonKey: String) {
        if (gameId.isEmpty()) return

        mySelection = buttonKey
        button.setBackgroundColor(getColor(android.R.color.holo_blue_light))
        disableAllButtons()

        Toast.makeText(this, "Has seleccionat $buttonKey. Esperant el rival...", Toast.LENGTH_SHORT).show()

        val rolePath = if (isAttacker) "atacant" else "defensa"

        database.child("partides").child(gameId).child(rolePath).child("seleccio").setValue(buttonKey)
            .addOnSuccessListener {
                // Un cop desada la nostra selecció, comprovem si la partida ja pot acabar.
                checkForOpponentSelection()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error guardant la selecció: ${it.message}", Toast.LENGTH_SHORT).show()
                enableAllButtons()
                mySelection = null
                button.setBackgroundColor(getColor(android.R.color.transparent))
            }
    }

    /**
     * Estableix un listener a la branca "resultat" de la partida.
     * S'activarà quan s'escrigui el resultat final, notificant a AMBDÓS jugadors.
     */
    private fun listenForGameResult() {
        if (gameId.isEmpty()) return
        gameResultRef = database.child("partides").child(gameId).child("resultat")

        gameResultListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resultMsg = snapshot.getValue(String::class.java)
                if (!resultMsg.isNullOrEmpty()) {
                    // Si hi ha un resultat, el mostrem i netegem el listener.
                    showGameOverDialog(resultMsg)
                    gameResultRef.removeEventListener(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Duelos, "Error escoltant el resultat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        gameResultRef.addValueEventListener(gameResultListener!!)
    }

    private fun disableAllButtons() {
        val buttons = listOf(
            binding.a1, binding.a2, binding.a3,
            binding.b1, binding.b2, binding.b3,
            binding.c1, binding.c2, binding.c3
        )
        buttons.forEach { it.isEnabled = false }
    }

    private fun enableAllButtons() {
        val buttons = listOf(
            binding.a1, binding.a2, binding.a3,
            binding.b1, binding.b2, binding.b3,
            binding.c1, binding.c2, binding.c3
        )
        buttons.forEach { it.isEnabled = true }
    }

    private fun keyToCoords(key: String): Pair<Int, Int> {
        return when (key) {
            "a1" -> Pair(0, 0); "a2" -> Pair(0, 1); "a3" -> Pair(0, 2)
            "b1" -> Pair(1, 0); "b2" -> Pair(1, 1); "b3" -> Pair(1, 2)
            "c1" -> Pair(2, 0); "c2" -> Pair(2, 1); "c3" -> Pair(2, 2)
            else -> Pair(-1, -1)
        }
    }

    /**
     * Calcula el resultat i l'escriu a Firebase. No mostra cap diàleg.
     * El listener 'gameResultListener' reaccionarà a l'escriptura d'aquest resultat.
     */
    private fun calculateAndWriteResult(attRow: Int, attCol: Int, defRow: Int, defCol: Int) {
        var points = 0
        val message: String

        val rowMatch = (attRow == defRow)
        val colMatch = (attCol == defCol)

        when {
            rowMatch && colMatch -> {
                points = 2
                message = "IMPACTE DIRECTE! El defensor ha bloquejat l'atac. (Mateixa fila i columna)"
            }
            rowMatch || colMatch -> {
                points = 1
                message = "FREGAT! El defensor ha endevinat la " + (if (rowMatch) "fila" else "columna") + "."
            }
            else -> {
                points = 0
                message = "FALLADA! L'atac ha passat les defenses."
            }
        }

        val finalMsg = if (isAttacker) {
            "El defensor ha aconseguit $points punts.\n$message"
        } else {
            "Has aconseguit $points punts defensant!\n$message"
        }

        // Escrivim el resultat a Firebase. Això activarà el listener en ambdós clients.
        database.child("partides").child(gameId).child("resultat").setValue(finalMsg)
    }

    /**
     * Fa una consulta ÚNICA a Firebase per comprovar si ambdós jugadors han seleccionat.
     */
    private fun checkForOpponentSelection() {
        if (gameId.isEmpty()) return

        val gameRef = database.child("partides").child(gameId)

        gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val attackerSelKey = snapshot.child("atacant/seleccio").getValue(String::class.java)
                val defenderSelKey = snapshot.child("defensa/seleccio").getValue(String::class.java)

                if (attackerSelKey != null && defenderSelKey != null) {
                    val (attRow, attCol) = keyToCoords(attackerSelKey)
                    val (defRow, defCol) = keyToCoords(defenderSelKey)

                    if (attRow != -1 && defRow != -1) {
                        // Només calculem i escrivim el resultat.
                        calculateAndWriteResult(attRow, attCol, defRow, defCol)
                    } else {
                        Toast.makeText(this@Duelos, "Error: S'ha rebut una selecció invàlida.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Duelos, "Error de DB en comprovar l'oponent: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Mostra el diàleg final amb el resultat de la partida.
     */
    private fun showGameOverDialog(msg: String) {
        if (isFinishing || isDestroyed) return

        AlertDialog.Builder(this)
            .setTitle("Resultat del Duel")
            .setMessage(msg)
            .setPositiveButton("Tornar al menú") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
