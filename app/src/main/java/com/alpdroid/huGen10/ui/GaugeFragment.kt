package main.java.com.alpdroid.huGen10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.ui.UIFragment

class GaugeFragment : UIFragment(500),AdapterView.OnItemLongClickListener {

    private lateinit var functionsList: LinearLayout
    lateinit var functionProgressBars: MutableMap<String, ProgressBar>
    lateinit var  functionProgressBar:ProgressBar
    private var currentHeight = 0
    val functions = listOf("Function 1", "Function 2", "Function 3")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for the fragment
        return inflater.inflate(R.layout.telemetrics_parameter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the list of functions from the class data.


        // Create a map to store the function progress bars.
        functionProgressBars = mutableMapOf()

        // Create a list view to display the functions.
        functionsList = view.findViewById(R.id.functions_list)

        // Add the functions to the list view.
        for (function in functions) {
             functionProgressBar = ProgressBar(requireContext()).apply {
                max = 100
                functionProgressBars[function] = this
            }

            functionsList.addView(functionProgressBar)

            // Get the function's min and max values from the class data.
            val functionData = FunctionData(function, 0, 100)
            val functionValue = functionData.getFuncitonValue()

            // Update the progress bar with the function value.
            functionProgressBar.progress = functionValue

            // Update the height of the list view.
            currentHeight += functionProgressBar.height
            functionsList.layoutParams.height = currentHeight
        }
    }

    // Set the listener for the long click on the list view.
    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        // Get the name of the selected function.
        val functionName = functions[position]

        // If the progress bar for the selected function exists, remove it from the list view.
        if (functionProgressBars.containsKey(functionName)) {
            functionsList.removeView(functionProgressBars[functionName])
            functionProgressBars.remove(functionName)
        }

        // Update the height of the list view.
        currentHeight -= functionProgressBar.height
        functionsList.layoutParams.height = currentHeight

        return true
    }

}

data class FunctionData(val name: String, val min: Int, val max: Int) {

    fun getFuncitonValue(): Int {
        // This is just a simple example of how to get the function value.
        return (52)
    }

}
