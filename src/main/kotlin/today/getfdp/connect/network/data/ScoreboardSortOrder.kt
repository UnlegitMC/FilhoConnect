package today.getfdp.connect.network.data

enum class ScoreboardSortOrder {
    ASCENDING,
    DESCENDING;

    companion object {
        val values = values() // improve performance
    }
}