import java.util.*

class Apple {
    var x = 0
    var y = 0
    var random = Random()
    var width: Int = SnakeGame.width
    var height: Int = SnakeGame.height
    var units: Int = SnakeGame.unit_size
    var pos = 0
    var xP = intArrayOf(300, 150, 0, 210)
    var yP = intArrayOf(400, 110, 25, 300)

    init {
        moveApple()
    }

    fun moveApple() {
        if (SnakeGame.randomApple) {
            x = random.nextInt((width / units)) * units
            y = random.nextInt((height / units)) * units
        } else {
            x = (xP[pos] / units) * units
            y = (yP[pos] / units) * units
            pos++
        }
    }
}