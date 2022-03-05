package today.getfdp.connect.play

import today.getfdp.connect.network.data.ScoreboardSortOrder
import today.getfdp.connect.play.entity.Entity
import java.util.UUID
import kotlin.math.abs

class TheWorld(private val client: Client) {
    private var realRain = 0.0f
    private var realThunder = 0.0f
    var rain = 0.0f
    var thunder = 0.0f
    val scoreboardSorts = mutableMapOf<String, ScoreboardSortOrder>() // the string is scoreboard objectiveId
    val playerUuids = mutableListOf<UUID>()
    val entities = mutableMapOf<Int, Entity>()

    fun update() {
        if(rain != realRain) {
            realRain += (if(rain > realRain) 1 else -1) * abs(rain - realRain).coerceAtMost(0.05f)
        }
        if(thunder != realThunder) {
            realThunder += (if(thunder > realThunder) 1 else -1) * abs(thunder - realThunder).coerceAtMost(0.05f)
        }
    }

    fun spawn(entity: Entity) {
        if(!entity.spawned) {
            entity.spawn()
        }
        entities[entity.runtimeId] = entity
    }

    fun despawn(entity: Entity) {
        if(entity.spawned) {
            entity.despawn()
        }
        entities.remove(entity.runtimeId)
    }
}