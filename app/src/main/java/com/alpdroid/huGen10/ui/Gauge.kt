package main.java.com.alpdroid.huGen10.ui

data class Gauge(
    val id: String,
    var minValue: Float,
    var maxValue: Float,
    var unit: String,
    val functionInfo: FunctionInfo
)

data class FunctionInfo(
    val functionName: String,
    val className: String
)