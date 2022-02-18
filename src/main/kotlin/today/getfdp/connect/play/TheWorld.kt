package today.getfdp.connect.play

import kotlin.math.abs

class TheWorld(private val client: Client) {
    private var realRain = 0.0f
    private var realThunder = 0.0f
    var rain = 0.0f
    var thunder = 0.0f

    fun update() {
        if(rain != realRain) {
            realRain += (if(rain > realRain) 1 else -1) * abs(rain - realRain).coerceAtMost(0.05f)
        }
        if(thunder != realThunder) {
            realThunder += (if(thunder > realThunder) 1 else -1) * abs(thunder - realThunder).coerceAtMost(0.05f)
        }
    }
}