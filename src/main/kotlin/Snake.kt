/**
 * The Snake class represents the snake in the Snake Game.
 * It manages the snake's position, movement, collision detection,
 * and provides methods for handling neural network inputs.
 */
class Snake {
    var x = IntArray(SnakeGame.GAME_UNITS)
    var y = IntArray(SnakeGame.GAME_UNITS)
    val gameHeight: Int = SnakeGame.height
    val gameWidth: Int = SnakeGame.width
    val unit_size: Int = SnakeGame.unit_size
    var bodyParts: Int = SnakeGame.defaultBodyParts
    var applesEaten = 0
    var direction: Char = SnakeGame.defaultDirection
    var alive = true
    var moves = 0
    var view: Int = SnakeGame.INPUT_SCHEME - 2
    var apple = Apple()
    var directions = charArrayOf('R', 'L', 'U', 'D')
    var brain = NeuralNetwork(SnakeGame.brainArchitecture)
    var fitness = 0
    var inputs: DoubleArray = brain.nodes[0]!!
    var outputs: DoubleArray

    /**
     * Constructs a new Snake object. Initializes neural network inputs and outputs.
     * If random starting position is enabled, sets a random initial position;
     * otherwise, sets the position at the center of the game board.
     */
    init {
        // our inputs are the first layer of our nodes matrix
        // we can just create a reference to that array and edit it here. if porting to another language, just edit brain.nodes[0] directly

        // same with output nodes
        outputs = brain.nodes[brain.nodes.size - 1]!!
        if (SnakeGame.randomStartingPosition) {
            setRandomPosition()
        } else {
            setPosition(gameWidth.toInt() / 2, gameHeight.toInt() / 2)
        }
        direction = directions[(Math.random() * 4).toInt()]
    }

    /**
     * Sets the neural network inputs based on the snake's current state.
     */
    fun setInputs() {
        // our inputs are the first layer of our nodes matrix
        // we can just create a reference to that array and edit it here. if porting to another language, just edit brain.nodes[0] directly
        // larger numbers in this case respond to a greater urgency to act
        if (SnakeGame.INPUT_SCHEME === 1) {
            inputs[0] = (1 - y[0] / gameHeight).toDouble()
            inputs[1] = (1 - (gameHeight - y[0]) / gameHeight).toDouble()
            inputs[2] = (x[0] / gameWidth).toDouble()
            inputs[3] = ((gameWidth - x[0]) / gameWidth).toDouble()
            inputs[4] = 1 - northTail() / gameHeight
            inputs[5] = 1 - southTail() / gameHeight
            inputs[6] = 1 - westTail() / gameWidth
            inputs[7] = 1 - eastTail() / gameWidth
            inputs[8] = ((y[0] - apple.y) / gameHeight).toDouble()
            inputs[9] = ((apple.x - x[0]) / gameWidth).toDouble()
            inputs[10] = (if (direction == 'R') 1 else 0).toDouble()
            inputs[11] = (if (direction == 'L') 1 else 0).toDouble()
            inputs[12] = (if (direction == 'U') 1 else 0).toDouble()
            inputs[13] = (if (direction == 'D') 1 else 0).toDouble()
        }
        if (SnakeGame.INPUT_SCHEME === 2) {
            // Input Scheme 2
            // detecting if the snake is right next to a wall
            inputs[0] = (if (y[0] == 0) 1 else 0).toDouble()
            inputs[1] = (if ((gameHeight - y[0]).toDouble() == unit_size.toDouble()) 1 else 0).toDouble()
            inputs[2] = (if (x[0] == 0) 1 else 0).toDouble()
            inputs[3] = (if ((gameHeight - x[0]).toDouble() == unit_size.toDouble()) 1 else 0).toDouble()
            // detecting if the snake is right next to its own tail
            inputs[4] = (if (northTail() == unit_size.toDouble()) 1 else 0).toDouble()
            inputs[5] = (if (southTail() == unit_size.toDouble()) 1 else 0).toDouble()
            inputs[6] = (if (westTail() == unit_size.toDouble()) 1 else 0).toDouble()
            inputs[7] = (if (eastTail() == unit_size.toDouble()) 1 else 0).toDouble()
            // apple left or right and up or down
            inputs[8] = (if (y[0] - apple.y > 0) 1 else -1).toDouble()
            inputs[9] = (if (apple.x - x[0] > 0) 1 else -1).toDouble()
        }
        if (SnakeGame.INPUT_SCHEME === 3) {
            inputs[0] =
                (if (Math.min(northTail(), (y[0] + unit_size).toDouble()) == unit_size.toDouble()) 1 else 0).toDouble()
            inputs[1] = (if (Math.min(southTail(), (gameHeight - y[0]).toDouble()) == unit_size.toDouble()) 1 else 0).toDouble()
            inputs[2] = (if (Math.min(westTail(), x[0].toDouble()) <= unit_size) 1 else 0).toDouble()
            inputs[3] = (if (Math.min(eastTail(), (gameWidth - x[0]).toDouble()) <= unit_size) 1 else 0).toDouble()
            inputs[4] = (if (y[0] - apple.y > 0) 1 else -1).toDouble()
            inputs[5] = (if (apple.x - x[0] > 0) 1 else -1).toDouble()
        }
        if (SnakeGame.INPUT_SCHEME === 4) {
            inputs[0] = 1 - (Math.min(northTail(), y[0].toDouble())) / gameHeight
            inputs[1] = 1 - (Math.min(southTail(), (gameHeight - y[0]).toDouble())) / gameHeight
            inputs[2] = 1 - (Math.min(westTail(), x[0].toDouble())) / gameWidth
            inputs[3] = 1 - (Math.min(eastTail(), (gameWidth - x[0]).toDouble())) / gameWidth
            inputs[4] = (if (y[0] - apple.y > 0) 1 else -1).toDouble()
            inputs[5] = (if (apple.x - x[0] > 0) 1 else -1).toDouble()
        }
        if (SnakeGame.INPUT_SCHEME > 4){
            var startX : Int = x[0] - (view / 2) * unit_size
            var startY : Int = y[0] - (view / 2) * unit_size
            for (i in 0 until view){
                for (j in 0 until view){
                    inputs[i * view + j] = obstacle(startX + i * unit_size, startY + j * unit_size).toDouble()
                }
            }

            // apple left/right
            var square : Int= view * view
            inputs[square] = (if (apple.x > x[0]) 1 else -1).toDouble()
            inputs[square+1] = (if (apple.y < y[0]) 1 else -1).toDouble()
        }
        /*
        // encoded
        // 3x3 box around snake
        // apple xy
        // facing up




 */
    }

    /**
     * Sets the snake's position to the specified coordinates.
     *
     * @param xP The x-coordinate.
     * @param yP The y-coordinate.
     */
    fun setPosition(xP: Int, yP: Int) {
        for (i in x.indices) {
            x[i] = xP
            y[i] = yP
        }
    }

    fun setRandomPosition() {
        // range is 0 to screendimension - unit size so it does not spawn on edge of screen
        val xP = (Math.random() * (gameWidth - 2 * unit_size) + unit_size).toInt()
        val yP = (Math.random() * (gameHeight - 2 * unit_size) + unit_size).toInt()
        for (i in x.indices) {
            // goes to the nearest number that is a multiple of Unit size so that collisions work
            x[i] = xP / unit_size * unit_size
            y[i] = yP / unit_size * unit_size
        }
    }

    fun move() {
        fitness = (applesEaten * 50 + 0 * distanceToApple()).toInt()
        //  fitness = (int) (moves + (Math.pow(2,applesEaten) + Math.pow(applesEaten,2.1) * 500) - (Math.pow(applesEaten,1.2) * Math.pow((0.25 * moves),1.3)));
        for (i in bodyParts downTo 1) {
            x[i] = x[i - 1]
            y[i] = y[i - 1]
        }
        moves++
        when (direction) {
            'U' -> y[0] = y[0] - SnakeGame.unit_size
            'D' -> y[0] = y[0] + SnakeGame.unit_size
            'L' -> x[0] = x[0] - SnakeGame.unit_size
            'R' -> x[0] = x[0] + SnakeGame.unit_size
        }
        if (moves > 4000) {
            alive = false
        }
    }

    fun distanceToApple(): Double {
        return Math.sqrt(2.0) - Math.sqrt(
            Math.pow(
                ((x[0] - apple.x) / gameWidth).toDouble(),
                2.0
            ) + Math.pow(((y[0] - apple.y) / gameHeight).toDouble(), 2.0)
        )
    }

    fun chooseDirection() {
        // outputs = {R, L, U, D}
        // greedy selection (always picking the highest output)
        if (!SnakeGame.probabilisticChoice) {
            var maxNum = 0.0
            var maxIdx = 0
            for (i in outputs.indices) {
                if (outputs[i] > maxNum) {
                    maxNum = outputs[i]
                    maxIdx = i
                }
            }
            direction = directions[maxIdx]
        } else {
            var outputSum = 0.0
            for (i in outputs.indices) {
                outputSum += outputs[i]
            }
            val position = Math.random() * outputSum
            var sum = 0.0
            for (j in outputs.indices) {
                sum += outputs[j]
                if (sum >= position) {
                    direction = directions[j]
                    break
                }
            }
        }

        // probabilistic selection (selecting based off a weighted probability with the output vals)
    }

    fun checkApple() {
        if ((x[0] == apple.x) && y[0] == apple.y) {
            bodyParts++
            applesEaten++
            apple.moveApple()
        }
    }

    fun checkCollisions() {
        //checks if head collides with body
        for (i in bodyParts downTo 1) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                alive = false
                break
            }
        }
        //check if head touches left border
        if (x[0] < 0) {
            alive = false
            x[0] = 0
        }
        //check if head touches right border
        if (x[0] > SnakeGame.width - unit_size) {
            alive = false
        }
        //check if head touches top border
        if (y[0] < 0) {
            alive = false
        }
        //check if head touches bottom border
        if (y[0] > SnakeGame.height - unit_size) {
            alive = false
        }
    }

    fun northTail(): Double {
        for (i in 0 until bodyParts) {
            if ((x[i] == x[0]) && (y[0] > y[i] && direction != 'D')) {
                // absolute value distance from closest north tail segment
                return Math.abs(y[0] - y[i]).toDouble()
                // remember 0,0 is top left corner of screen
            }
        }
        return gameHeight.toDouble()
    }

    fun southTail(): Double {
        for (i in 0 until bodyParts) {
            if ((x[i] == x[0]) && (y[0] < y[i] && direction != 'U')){
                // absolute value distance from closest south tail segment
                return Math.abs(y[i] - y[0]).toDouble()
            }
        }
        return gameHeight.toDouble()
    }

    fun eastTail(): Double {
        for (i in 0 until bodyParts) {
            if ((y[i] == y[0]) && (x[i] > x[0] && direction != 'L')) {
                // absolute value distance from closest east tail segment
                return (x[i] - x[0]).toDouble()
            }
        }
        return gameWidth.toDouble()
    }

    fun westTail(): Double {
        for (i in 0 until bodyParts) {
            if ((y[i] == y[0]) && (x[i] < x[0] && direction != 'R')) {
                // absolute value distance from closest west tail segment
                return (x[0] - x[i]).toDouble()
            }
        }
        return gameWidth.toDouble()
    }

    // 0 is empty space
    // 1 is any wall/tail
    fun obstacle(xP: Int, yP: Int): Int {
        // wall
        if ( (xP < 0) || xP > gameWidth - unit_size || yP < 0 || yP > gameHeight - unit_size) {
            return 1
        }

        // tail (excluding the head bc we dont wanna count that)
        for (i in 0 until bodyParts) {
            if (x[i] == xP && y[i] == yP) {
                return 1
            }
        }
        return 0
    }
}