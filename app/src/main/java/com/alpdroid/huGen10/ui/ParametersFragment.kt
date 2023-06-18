package main.java.com.alpdroid.huGen10.ui


import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.ui.UIFragment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson


class ParametersFragment : UIFragment(500) {


        private lateinit var paramRecyclerView: RecyclerView
        private lateinit var paramAdapter: ParameterAdapter
        private lateinit var gaugeList: MutableList<Gauge>
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var progressBarLayout: LinearLayout

    private val parameterList: List<Pair<String, () -> Float>> = listOf(
        Pair("Paramètre 1", ::function1),
        Pair("Paramètre 2", ::function2),
        Pair("Paramètre 3", ::function3)
        // Ajoutez autant de paramètres que nécessaire
    )

    private fun function1(): Float {
        // Logique de calcul pour le paramètre 1
        return 10f // Exemple de valeur de mise à jour
    }

    private fun function2(): Float {
        // Logique de calcul pour le paramètre 2
        return 20f // Exemple de valeur de mise à jour
    }

    private fun function3(): Float {
        // Logique de calcul pour le paramètre 3
        return 30f // Exemple de valeur de mise à jour
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.alpine_telemetrics, container, false)
            paramRecyclerView = view.findViewById(R.id.paramRecyclerView)
            progressBarLayout = view.findViewById(R.id.progressBarLayout)
            progressBarLayout.setOrientation(LinearLayout.HORIZONTAL);
            val linLayoutParam = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            // set LinearLayout as a root element of the screen
            // set LinearLayout as a root element of the screen
            progressBarLayout.layoutParams=linLayoutParam


            sharedPreferences = requireContext().getSharedPreferences("GaugePrefs", Context.MODE_PRIVATE)

            eraseGaugeList()

            return view
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gaugeList = initList().toMutableList() // Ajoutez cette ligne

   //     restoreGaugeList()

        paramAdapter = context?.let { ParameterAdapter(gaugeList, it) }!!
        paramRecyclerView.adapter = paramAdapter

        val layoutManager = GridLayoutManager(requireContext(), 1)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        paramRecyclerView.layoutManager = layoutManager

        val spacing = resources.getDimensionPixelSize(R.dimen.gauge_spacing)
        paramRecyclerView.addItemDecoration(GridSpacingItemDecoration(1, spacing, true))


        view.setOnDragListener { _, event ->
            val action = event.action

            Log.d("call by drag action bool : ", (action==3).toString())

            when (action) {
                DragEvent.ACTION_DROP -> {
                    val parameterID =
                        event.clipData.getItemAt(0).text
                    Log.d("Dropped parameter:", parameterID.toString() )

                    val gauge = gaugeList.find { it.id == parameterID }
                    if (gauge != null) {
                        showParameterDialog(gauge)
                    }
                }
            }
            true
        }
    }

        private fun showParameterDialog(parameter: Gauge) {
            val dialogView = layoutInflater.inflate(R.layout.telemetrics_dialog, null)
            val editMinValue = dialogView.findViewById<EditText>(R.id.editMinValue)
            val editMaxValue = dialogView.findViewById<EditText>(R.id.editMaxValue)
            val editUnit = dialogView.findViewById<EditText>(R.id.editUnit)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

            // Récupérer la jauge correspondante (si elle existe)
            val gauge = gaugeList.find { it.id == parameter.id }

            editMinValue.setText(gauge?.minValue.toString())
            editMaxValue.setText(gauge?.maxValue.toString())
            editUnit.setText(gauge?.unit)

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle(parameter.id)
                .setCancelable(true)
                .create()

            btnSave.setOnClickListener {
                val minValue = editMinValue.text.toString().toFloat()
                val maxValue = editMaxValue.text.toString().toFloat()
                val unit = editUnit.text.toString()

                // Créer une nouvelle jauge ou mettre à jour une jauge existante
                if (gauge == null) {
                    val functionName =
                        parameterList.find { it.first == parameter.id }?.second?.let { it1 ->
                            getFunctionName(
                                it1
                            )
                        }
                    if (functionName != null) {
                        val newGauge = Gauge(parameter.id, minValue, maxValue, unit, functionName)
                        gaugeList.add(newGauge)
                        Log.d("AddProgressBar", "jadd progressbar")
                        addProgressBar(newGauge)
                        alertDialog.dismiss()

                    }
                } else {
                    gauge.minValue = minValue
                    gauge.maxValue = maxValue
                    gauge.unit = unit
                }

                saveGaugeList(gaugeList)
                paramAdapter.notifyDataSetChanged()

                alertDialog.dismiss()
            }

            alertDialog.show()
        }



    private fun addProgressBar(gauge: Gauge) {
        // Créer une nouvelle ProgressBar
        val progressBar = ProgressBar(this.context)
        // Customize the progressBar properties if needed
        progressBar.setProgress(150)
        progressBar.setMax(gauge.maxValue.toInt())
        progressBar.setMin(gauge.minValue.toInt())
        progressBar.visibility=ProgressBar.VISIBLE

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Add the progressBar to the progressBarLayout


        progressBar.layoutParams=layoutParams

        progressBarLayout.addView(progressBar, layoutParams)

        // Save the progressBar to SharedPreferences or any other desired storage
        // Save the ProgressBar and its properties in SharedPreferences
        saveProgressBar(gauge.id, progressBar)

    }

    private fun saveProgressBar(gaugeId: String, progressBar: ProgressBar) {
        val editor = sharedPreferences.edit()
        editor.putInt("${gaugeId}_progress", progressBar.progress)
        // Save other properties of the ProgressBar if needed
        editor.apply()
    }

    private fun restoreProgressBar(gaugeId: String): ProgressBar {
        val progressBar = ProgressBar(requireContext())
        // Retrieve the saved properties of the ProgressBar from SharedPreferences
        val progress = sharedPreferences.getInt("${gaugeId}_progress", 0)
        progressBar.progress = progress
        // Restore other properties of the ProgressBar if needed
        return progressBar
    }

    private fun saveGaugeList(gaugeList: List<Gauge>) {
        val gson = Gson()
        val type = object : TypeToken<List<Gauge>>() {}.type

        val gaugeListWithFunctionInfo = gaugeList.map { gauge ->
            val functionInfo = FunctionInfo(
                functionName = gauge.functionInfo.functionName,
                className = gauge.functionInfo.javaClass.name
            )
            gauge.copy(functionInfo = functionInfo)
        }

        val gaugeListJson = gson.toJson(gaugeListWithFunctionInfo, type)
        sharedPreferences.edit().putString("gaugeList", gaugeListJson).apply()
    }

    private fun getFunctionName(function: () -> Float): FunctionInfo {
        val lambdaClass = function.javaClass
        val functionName = lambdaClass.declaredMethods.find { method ->
            method.parameterTypes.isEmpty()
        }?.name

        return FunctionInfo(functionName ?: "",lambdaClass.name)
    }


    private fun restoreGaugeList(): List<Gauge> {
        val gaugeListJson = sharedPreferences.getString("gaugeList", null)

        return if (gaugeListJson != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Gauge>>() {}.type
            val gaugeListWithFunctionInfo = gson.fromJson<List<Gauge>>(gaugeListJson, type)

            val restoredGaugeList = gaugeListWithFunctionInfo.map { gauge ->
//                val function = getFunctionFromInfo(gauge.functionInfo)
                gauge.copy(functionInfo = gauge.functionInfo )
            }

            restoredGaugeList.forEach { gauge ->
                addProgressBar(gauge)
            }

            restoredGaugeList
        } else {
            initList()
        }
    }

    private fun getFunctionFromInfo(functionInfo: FunctionInfo): () -> Float {
        val className = functionInfo.className
        val functionName = functionInfo.functionName

        val clazz = Class.forName(className)
        val method = clazz.getDeclaredMethod(functionName)
        @Suppress("UNCHECKED_CAST")
        val function = method.invoke(null) as () -> Float

        return function
    }


    private fun eraseGaugeList() {
        val sharedPreferences = requireContext().getSharedPreferences("GaugePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("gaugeList")
        editor.apply()
    }

    private fun initList(): List<Gauge> {
        val gaugeList = mutableListOf<Gauge>()

        val gauge1 = Gauge("Paramètre 1", 0f, 100f, "Fonction 1", FunctionInfo(::function1.name,::function1.javaClass.name))
        val gauge2 = Gauge("Paramètre 2", 0f, 50f, "Fonction 2", FunctionInfo(::function2.name,::function2.javaClass.name))
        val gauge3 = Gauge("Paramètre 3", 0f, 25f, "Fonction 3", FunctionInfo(::function3.name,::function3.javaClass.name))

        gaugeList.add(gauge1)
        gaugeList.add(gauge2)
        gaugeList.add(gauge3)

        saveGaugeList(gaugeList)

        return gaugeList
    }
}