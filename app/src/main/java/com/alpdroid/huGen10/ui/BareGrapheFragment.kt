package main.java.com.alpdroid.huGen10.ui

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.ui.UIFragment


class BargrapheFragment : UIFragment(500) {


    lateinit var alpineServices : VehicleServices

    private val chosenTypes = HashSet<String>() // Liste des types déjà choisis

    private lateinit var bargraphContainer: LinearLayout
    private var maxBargraphs = 6

    private var lastClickTime: Long = 0
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300 // Autant que nécessaire

    private var barGrapheList = mutableMapOf<String, View>() // Utilisez une Map
    private var barGrapheTypes= mutableListOf<BareGrapheType>() // Liste des types

    companion object {
        private const val PREFS_NAME = "BargrapheFragmentPrefs"
        private const val BARGRAPH_STATE_KEY = "bargraphState"
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bargraphe, container, false)

        val displayMetrics = resources.displayMetrics

        val heightPixels = displayMetrics.heightPixels
        val density = displayMetrics.density
        val spacing = 4*resources.getDimension(R.dimen.bargraph_height)

        maxBargraphs=(heightPixels*density/spacing).toInt()

        val questionView: ImageView = view.findViewById(R.id.question)
        questionView.setOnTouchListener { _, _ ->
            showTooltip("Cliquez sur l'écran pour ajouter un bargraphe")
            true
        }

        bargraphContainer = view.findViewById(R.id.bargraphContainer)



        view.setOnTouchListener { _, _ ->
            showBargraphSelectionPopup()
            false
        }

        // Restoring the fragment state
        restoreFragmentState()


        return view
    }

    private fun saveFragmentState() {
        val prefs = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val editor = prefs.edit()

        // Save the selected types to SharedPreferences
        val selectedTypesSet = HashSet(chosenTypes)
        editor.putStringSet("selectedTypes", selectedTypesSet)

        // Save the state of each progress bar
        for ((type, bargrapheView) in barGrapheList) {
            editor.putString("$type-type", type)
            editor.putString("$type-maxObserved", (bargrapheView.findViewById<TextView>(R.id.maxObserved)).text.toString())
            editor.putString("$type-maxValue", bargrapheView.findViewById<TextView>(R.id.maxValueTextView).text.toString())
            editor.putString("$type-minValue", (bargrapheView.findViewById<TextView>(R.id.minValueTextView)).text.toString())
        }

        // Save the list of types to SharedPreferences

        editor.apply()
    }

    private fun restoreFragmentState() {
        val prefs = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedTypesSet = prefs?.getStringSet("selectedTypes", emptySet())
        selectedTypesSet?.let { chosenTypes.addAll(it) }

        barGrapheList.clear()

        // Iterate through SharedPreferences entries and restore bargraphs
        val allEntries: Map<String, *> = prefs!!.all
        for ((key, value) in allEntries) {
            if (key.endsWith("-type") && value is String) {
                val type = value
                val maxValueKey = "$type-maxValue"
                val minValueKey = "$type-minValue"
                val maxObservedKey = "$type-maxObserved"

                // Check if the corresponding keys for maxValue, minValue, and maxObserved exist
                if (prefs!!.contains(maxValueKey) && prefs.contains(minValueKey) && prefs.contains(maxObservedKey)) {
                    val maxValue = prefs.getString(maxValueKey, "0")?.toInt() ?: 0
                    val minValue = prefs.getString(minValueKey, "0")?.toInt() ?: 0
                    val maxObserved = prefs.getString(maxObservedKey, "0")?.toInt() ?: 0

                    // Create and add the bargraph to the barGrapheList
                    createBargraphProgressbar(type, minValue, maxValue, maxObserved)

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Save the fragment state when it is destroyed
        saveFragmentState()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barGrapheTypes = mutableListOf(
            BareGrapheType("Oil Temp.", 0, 150, 0) { barGrapheView, maxObs ->
                //  logique de mise à jour de la ProgressBar pour le type 1
                val progressValue =
                    (alpineServices.get_OilTemperature() - 40)

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }
            },
            BareGrapheType("Cool Temp.", 0, 130, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type 2
                val progressValue =
                    (alpineServices.get_EngineCoolantTemp() - 40)

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },


            BareGrapheType("Gear Temp.", 60, 140, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type 3
                val progressValue =
                    (alpineServices.get_RST_ATClutchTemperature() + 60)

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },

                    BareGrapheType("Air Temp.", -10, 60, 0) { barGrapheView, maxObs ->
             //  logique de mise à jour de la ProgressBar pour le type x
            val progressValue =
                (alpineServices.get_IntakeAirTemperature() - 40)

            if (progressValue > maxObs) {
                //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                return@BareGrapheType progressValue
            } else {
                updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                return@BareGrapheType maxObs
            }

        },
            BareGrapheType("Speed", 0, 300, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type x
                val progressValue =
                    (alpineServices.get_Disp_Speed_MM() / 100)

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },
            BareGrapheType("Engine Pressure", 0, 70, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type x
                val progressValue =
                    alpineServices.get_EngineOilPressure()

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },
            BareGrapheType("Engine RPM", 0, 7500, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type x
                val progressValue =
                    alpineServices.get_EngineRPM_MMI()/8

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },
            BareGrapheType("Torque Nm", 0, 350, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type x
                val progressValue =
                    (alpineServices.get_EstimatedPowertrainWheelTorque()-6400)/2

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },

            BareGrapheType("HorsePower", 0, 350, 0) { barGrapheView, maxObs ->
                //  logique de mise à jour de la ProgressBar pour le type x
                val progressValue =
                    (alpineServices.get_SharpInstantMecanicalPowerByAlt()*40)

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },

            BareGrapheType("Braking Pressure", 0, 120, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type 2
                val progressValue =
                    alpineServices.get_BrakingPressure()/2

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }
            },
            BareGrapheType("Throttle", 0, 100, 0) { barGrapheView, maxObs ->
                 //  logique de mise à jour de la ProgressBar pour le type 2
                val progressValue =
                    alpineServices.get_RawSensor()/8

                if (progressValue > maxObs) {
                    //  currentValue et maxValue sont les valeurs actuelles et maximales pour la ProgressBar
                    updateBargrapheWithMarker(barGrapheView, progressValue, progressValue)
                    return@BareGrapheType progressValue
                } else {
                    updateBargrapheWithMarker(barGrapheView, progressValue, maxObs)
                    return@BareGrapheType maxObs
                }

            },

 // ici les autres fonctions

        )

        var bargrapheListCopy =
            barGrapheList.toList() // Créez une copie immuable de progressBarList
        var currentMillis = System.currentTimeMillis()

        timerTask = {
            activity?.runOnUiThread {
                if (AlpdroidApplication.app.isBound) {

                    alpineServices = AlpdroidApplication.app.alpdroidData

                    bargrapheListCopy =
                        barGrapheList.toList() // Créez une copie immuable de progressBarList

                    for ((type, bargraphe) in bargrapheListCopy) { // Itérez sur la Map
                        val find = barGrapheTypes.find { it.type == type }
                        find?.maxObservedValue =
                            find?.updater?.invoke(bargraphe, find.maxObservedValue)!!


                    }

                }
            }

        }
    }





    private fun showTooltip(text: String) {
        val tooltipView = LayoutInflater.from(context).inflate(R.layout.tooltip_layout, null)

        val tooltipText = tooltipView.findViewById<TextView>(R.id.tooltipText)
        tooltipText.text = text

        val tooltipPopup = PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )

        tooltipPopup.isOutsideTouchable = true
        tooltipPopup.showAtLocation(view, Gravity.CENTER, 0, 0)

        tooltipView.setOnTouchListener { _, _ ->
            tooltipPopup.dismiss()
            true
        }
    }


    private fun updateBargrapheWithMarker(bargrapheWithMarker: View, currentValue: Int, maxValue: Int) {


        activity?.runOnUiThread {

            val maxObsTextView = bargrapheWithMarker.findViewById<TextView>(R.id.maxObserved)
            val progressBar=bargrapheWithMarker.findViewById<ProgressBar>(R.id.progressBar)


            if (currentValue <= progressBar.max && currentValue >= progressBar.min) {

                if (currentValue >= maxValue) {
                    maxObsTextView.text=currentValue.toString()
                    progressBar.progress=currentValue

                }

                progressBar.secondaryProgress = currentValue

            }

            progressBar.invalidate()
        }
    }

    private fun showBargraphSelectionPopup() {

        if (bargraphContainer.childCount >= maxBargraphs) {
            Toast.makeText(context, "Maximum limit of $maxBargraphs bargraphs reached", Toast.LENGTH_SHORT).show()
            return
        }

        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_layout, null)

        val typeSpinner = popupView.findViewById<Spinner>(R.id.typeSpinner)
        val minValueEditText = popupView.findViewById<EditText>(R.id.minValueEditText)
        val maxValueEditText = popupView.findViewById<EditText>(R.id.maxValueEditText)
        val cancelButton = popupView.findViewById<Button>(R.id.cancelButton)
        val addButton = popupView.findViewById<Button>(R.id.addButton)

        // Obtenez la liste des types disponibles
        val types = getListOfAvailableTypes()
        val adapter = CustomSpinnerAdapter(requireContext(), types)
        typeSpinner.adapter = adapter

        // Gestionnaire de sélection de type
        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Lorsqu'un type est sélectionné, mettre à jour la valeur de minValueEditText
                val selectedType = types[position]
                val minValueEditText = popupView.findViewById<EditText>(R.id.minValueEditText)
                val maxValueEditText = popupView.findViewById<EditText>(R.id.maxValueEditText)

                // Mettre à jour la valeur de minValueEditText en fonction du type sélectionné

                minValueEditText.setText((barGrapheTypes.find { it.type == selectedType }?.minValue).toString())
                maxValueEditText.setText((barGrapheTypes.find { it.type == selectedType }?.maxValue).toString())

            }

         override fun onNothingSelected(parentView: AdapterView<*>) {
                // Lorsqu'aucun élément n'est sélectionné,  ajouter un comportement personnalisé ici si nécessaire
            }
        }

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )


        cancelButton.setOnClickListener {

            popupWindow.dismiss()

        }

        addButton.setOnClickListener {

            if (!getListOfAvailableTypes().isEmpty()) {
                val selectedType = typeSpinner.selectedItem.toString()
                val minValue = minValueEditText.text.toString().toIntOrNull() ?: 0
                val maxValue = maxValueEditText.text.toString().toIntOrNull() ?: 100

                createBargraphProgressbar(selectedType, minValue, maxValue, 0)

            }
            popupWindow.dismiss()

        }

        val minValue = minValueEditText.text.toString().toIntOrNull() ?: 0
        val maxValue = maxValueEditText.text.toString().toIntOrNull() ?: 100

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }


    private fun getListOfAvailableTypes(): List<String> {
        // Crée une liste de types disponibles en excluant ceux déjà choisis
        val availableTypes = mutableListOf<String>()
        val allTypes = listOf("Oil Temp.", "Cool Temp.", "Gear Temp.","Air Temp.","Speed","Engine Pressure","Engine RPM", "Torque Nm","Braking Pressure","Throttle","HorsePower", /* ... 37 more types */)

        for (type in allTypes) {
            if (!chosenTypes.contains(type)) {
                availableTypes.add(type)
            }
        }

        return availableTypes
    }

    private fun createBargraphProgressbar(type: String, minValue: Int, maxValue: Int, maxObserved:Int) {
        val bargraphWithMarker = LayoutInflater.from(context).inflate(R.layout.bargraph_with_title, null)
        val titleTextView = bargraphWithMarker.findViewById<TextView>(R.id.titleTextView)
        val maxTextView = bargraphWithMarker.findViewById<TextView>(R.id.maxValueTextView)
        val minTextView = bargraphWithMarker.findViewById<TextView>(R.id.minValueTextView)
        val maxObsValue = bargraphWithMarker.findViewById<TextView>(R.id.maxObserved)

        val progressBar = bargraphWithMarker.findViewById<ProgressBar>(R.id.progressBar)


        // Set title text and ProgressBar properties based on type
        titleTextView.text = type+" (peak) :"
        progressBar.max = maxValue
        maxTextView.text=maxValue.toString()
        minTextView.text=minValue.toString()
        maxObsValue.text=maxObserved.toString()
        progressBar.min = minValue

        // Ajoutez le type à la liste des types choisis
        chosenTypes.add(type)

        barGrapheList[type] = bargraphWithMarker // Stocke le baregraphe complet dans la Map

        // ... (Configurer les autres propriétés de la ProgressBar) ...

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        if (bargraphContainer.childCount > 0) {
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.bargraph_spacing)
        }

        bargraphWithMarker.layoutParams = layoutParams

        progressBar.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                barGrapheList.remove(type) // Retire la ProgressBar de la Map
                chosenTypes.remove(type)
                bargraphContainer.removeView(bargraphWithMarker)
            }
            lastClickTime = clickTime
        }

        bargraphContainer.addView(bargraphWithMarker)
    }
}

class BareGrapheType(val type: String,  val minValue: Int, val maxValue: Int, var maxObservedValue:Int, val updater: (View,Int) -> Int)

class CustomSpinnerAdapter(
    context: Context,
    private val types: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, types) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val type = types[position]

        // Personnalise la vue du Spinner pour afficher le type
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = type

        return view
    }
}

