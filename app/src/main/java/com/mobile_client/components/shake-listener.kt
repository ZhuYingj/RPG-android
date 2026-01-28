import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

@Composable
fun ShakeListener(
    onVerticalShake: () -> Unit,
    onHorizontalShake: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val shakeThreshold  = 10f
    val cooldownMillis = 500L
    val lastShakeTime = 0L

    val listener = remember {
        object : SensorEventListener {
            var lastX = 0f
            var filteredX = 0f

            var lastY = 0f
            var filteredY = 0f
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]

                val now = System.currentTimeMillis()

                val deltaX = x - lastX
                filteredX = filteredX * 0.9f + deltaX
                lastX = x
                if (abs(filteredX) > shakeThreshold && now - lastShakeTime > cooldownMillis)
                    onVerticalShake()

                val deltaY = y - lastY
                filteredY = filteredY * 0.9f + deltaY
                lastY = y
                if (abs(filteredY) > shakeThreshold && now - lastShakeTime > cooldownMillis)
                    onHorizontalShake()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(sensorManager) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
}
