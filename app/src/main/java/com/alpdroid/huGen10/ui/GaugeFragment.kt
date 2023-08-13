package main.java.com.alpdroid.huGen10.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.ui.UIFragment


// Classe GaugeFragment utilisant UIFragment
class GaugeFragment : UIFragment(500) {
    // Ajoutez ici les variables membres spécifiques à GaugeFragment
    private var gaugeValue: Int = 0
    private var gaugeLabel: String = ""
    private var selectedFunction: FunctionItem? = null
    private var progressBarCount: Int = 0

    private var doubleClickHandler: Handler? = null
    private var doubleClickRunnable: Runnable? = null

    // Liste des fonctions disponibles
    private val functionsList: ArrayList<FunctionItem> = ArrayList()

    // Liste des ProgressBars affichées
    private val progressBars: ArrayList<ProgressBar> = ArrayList()
    private val progressBarLayouts: ArrayList<LinearLayout> = ArrayList()


    // Classe représentant une fonction
    data class FunctionItem(
        val name: String,
        val minParam: Int,
        val maxParam: Int,
        val function: (Int) -> Unit
    )

    // Méthode pour ajouter une fonction à la liste
    fun addFunction(name: String, minParam: Int, maxParam: Int, function: (Int) -> Unit) {
        functionsList.add(FunctionItem(name, minParam, maxParam, function))
    }

    // Méthode pour définir la valeur de la jauge
    fun setGaugeValue(value: Int) {
        gaugeValue = value
        // Ici, vous pouvez mettre à jour l'affichage de la jauge en fonction de la nouvelle valeur
    }

    // Méthode pour définir l'étiquette de la jauge
    fun setGaugeLabel(label: String) {
        gaugeLabel = label
        // Ici, vous pouvez mettre à jour l'affichage de l'étiquette de la jauge si nécessaire
    }

    // Override de la méthode onCreateView pour inflater le layout de GaugeFragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Utilisez inflater pour inflater le layout de GaugeFragment ici
        val view = inflater.inflate(R.layout.telemetrics_parameter, container, false)

        // Initialisez ici les éléments de l'UI, comme les TextView pour afficher la valeur et l'étiquette de la jauge

        // Initialisez ici la liste déroulante avec les noms des fonctions
        val functionNames = functionsList.map { it.name }.toTypedArray()
        val spinner = view.findViewById<Spinner>(R.id.function_spinner)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, functionNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedFunction = functionsList[position]
                if (progressBarCount < 6) {
                    addProgressBar()
                } else {
                    showPopup()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedFunction = null
            }
        }


        return view
    }

    // Override de la méthode onViewCreated pour initialiser la liste de fonctions
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ajoutez ici les fonctions avec leurs noms, paramètres min, max et fonctions réelles associées
        addFunction("Function 1", 0, 100) { param ->
            // Implémentez ici la logique de la première fonction
            // param représente le paramètre de la fonction
        }

        addFunction("Function 2", -10, 10) { param ->
            // Implémentez ici la logique de la deuxième fonction
            // param représente le paramètre de la fonction
        }

        /// Initialiser le mécanisme de double-clic
        doubleClickHandler = Handler(Looper.getMainLooper())
        doubleClickRunnable = Runnable {
            // Reset le clic après un certain délai (par exemple, 500 ms)
            doubleClickHandler?.removeCallbacksAndMessages(null)
        }


        // Vous pouvez maintenant mettre à jour la liste déroulante avec les noms des fonctions
        val functionNames = functionsList.map { it.name }.toTypedArray()
        val spinner = view.findViewById<Spinner>(R.id.function_spinner)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, functionNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedFunction = functionsList[position]
                if (progressBarCount < 6) {
                    addProgressBar()
                } else {
                    showPopup()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedFunction = null
            }
        }
    }

    private fun addProgressBar() {
        val container = view?.findViewById<LinearLayout>(R.id.right_container)
        container?.let {
            val progressBarLayout = createProgressBarLayout()
            val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal)
            val params = LinearLayout.LayoutParams(
                dpToPx(128), // Largeur maximale de 128 pixels
                dpToPx(48)   // Hauteur de 32 pixels
            )
            progressBar.layoutParams = params
            progressBar.max = selectedFunction?.maxParam ?: 100

            progressBarLayout.addView(progressBar)

            progressBar.setOnLongClickListener {
                // Gérer le double-clic pour supprimer la ProgressBar
                if (doubleClickHandler != null && doubleClickRunnable != null) {
                    if (doubleClickHandler?.hasCallbacks(doubleClickRunnable!!) == true) {
                        removeProgressBar(progressBarLayout)
                    } else {
                        doubleClickHandler?.postDelayed(doubleClickRunnable!!, 500)
                        Toast.makeText(requireContext(), "Double-click to remove the function", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            container.addView(progressBarLayout)
            progressBarLayouts.add(progressBarLayout)
            progressBarCount++

            // Espacement de 16 pixels entre les ProgressBar
            val progressBarParamsLayout = progressBarLayout.layoutParams as ViewGroup.MarginLayoutParams
            progressBarParamsLayout.bottomMargin = dpToPx(48)
        }
    }

    // Méthode pour créer un LinearLayout contenant une ProgressBar
    private fun createProgressBarLayout(): LinearLayout {
        val progressBarLayout = LinearLayout(requireContext())
        progressBarLayout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dpToPx(32), 0, 0) // Marge de 80 pixels depuis le haut de l'écran
        progressBarLayout.layoutParams = params
        progressBarLayout.gravity = Gravity.CENTER_HORIZONTAL
        return progressBarLayout
    }

    // Méthode pour supprimer un LinearLayout contenant une ProgressBar de l'écran
    private fun removeProgressBar(progressBarLayout: LinearLayout) {
        val container = view?.findViewById<LinearLayout>(R.id.right_container)
        container?.let {
            container.removeView(progressBarLayout)
            progressBarLayouts.remove(progressBarLayout)
            progressBarCount--
        }
    }

    // Méthode pour convertir dp (density-independent pixels) en pixels
    private fun dpToPx(dp: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dp * density).toInt()
    }
    

    // Méthode pour afficher le popup lorsque la limite de ProgressBar est atteinte
    private fun showPopup() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Limit Reached")
            .setMessage("You have reached the maximum limit of 6 functions. Remove one to add another.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        alertDialog.show()
    }
}




