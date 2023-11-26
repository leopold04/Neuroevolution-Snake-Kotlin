import GeneticAlgorithm.createChild
import GeneticAlgorithm.createPopulation
import GeneticAlgorithm.integrateChildren
import GeneticAlgorithm.selectRandomParent
import GeneticAlgorithm.sortSnakes

object SnakeGame {
    // graphical
    var width = 500
    var height = 500
    const val widthBuffer = 700
    const val heightBuffer = 150
    const val displayDelay = 50
    const val userControl = false

    // snake settings
    var defaultBodyParts = 4
    var defaultDirection = 'R'
    const val unit_size = 25
    var randomStartingPosition = true
    val GAME_UNITS = width * height / (unit_size * unit_size)

    /** binds the input scheme to the number of input nodes for that scheme  */
    val bindings: HashMap<Int?, Int?> = object : HashMap<Int?, Int?>() {
        init {
            // unencoded
            put(1, 14)
            put(2, 10)
            put(3, 6)
            put(4, 6)
            //encoded starting with 3x3
            put(5,11)
            put(6,18)
            put(7,27)
        }
    }

    const val INPUT_SCHEME = 1

    /**list containing the number of nodes in each layer (including input and output layers)  */
    val brainArchitecture = intArrayOf(bindings[INPUT_SCHEME]!!, 8,8,4)
    val weightRange = intArrayOf(-4, 4)
    const val randomApple = true
    const val visualsEnabled = true
    var testingMode = false
    const val testWeights =
        "[18, 4][1.0011797534121474, 3.9621084886293207, 3.826860266600198, 3.485202598759492, 3.5786087597506873, -1.6246329899090455, -1.0596746063069062, 0.5573112302712913, 2.937717145320266, -0.7604677363677679, 1.5086894099925505, -0.09917544520295252, -3.262967375499639, 2.6701006445989206, 1.685241188729508, -2.8548126018709032, 2.9900031745736726, -0.6258890573293723, 3.815245208237342, 1.6382631851941687, 0.22126972774238496, -1.7013383772270387, -1.69821260727886, 1.8406503762242812, 3.0949958200764254, -3.310898151737378, 3.3491592048388243, 0.01745906139750275, 1.3444954608272282, -0.6894005479733964, -0.45015102741879875, -3.1404180768832486, 0.042883865275999966, 0.18193829567918396, -2.7966022107067436, -0.5918233900309504, 2.5645695608755323, -2.0220126031639927, -3.5936861856314275, 2.137010174178913, 1.833674759872383, -2.447784417562109, 0.7485898328795848, 1.4523231192140171, 0.2822590540720826, 2.882057005326674, 0.3716841181173409, -3.8876773517630303, 2.6904656711970487, 0.8162114535598564, 0.6616166250689854, 0.31820488103891353, -3.6861408986517104, 3.8569308038670576, -0.9324681774788006, 1.245534991274309, -3.3138156934896097, 1.9937446349044103, -0.7026480036499478, 2.273918680429234, 0.6298343482618698, 3.0030636981353593, 3.4746279990361053, 2.59609044944626, 2.7974049916490156, -3.5408270886344546, 3.6705189270275698, -3.671951626961312, -3.8522899730761138, 1.3364722688986932, 3.856283164939808, -1.4759661195869]"
    var numTesters = 15
    lateinit var mySnakes: Array<Snake?>
    var index = 0
    var bestFitness = 0
    var generationLimit = 5000

    // NN Settings
    // GA settings
    var generation = 0
    var populationSize = 500
    var numParents = 50
    var numChildren = 400
    var crossoverRate = 0.5
    var crossoverType = 0
    var probabilisticParentSelection = false
    var probabilisticChoice = false
    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // create initial population
        mySnakes = createPopulation(populationSize)

        // allow testing mode
        if (testingMode) {
            for (i in 0 until numTesters) {
                mySnakes[i]!!.brain.importWeightVector(testWeights)
            }
        }
        if (visualsEnabled) {
            val display = GameFrame()
            for (i in mySnakes.indices) {
                index = i
                while (mySnakes[i]!!.alive) {
                    Thread.sleep(displayDelay.toLong())
                    mySnakes[i]!!.setInputs()
                    mySnakes[i]!!.brain.forwardPropagation()
                    if (!userControl) {
                        mySnakes[i]!!.chooseDirection()
                    }
                    mySnakes[i]!!.move()
                    mySnakes[i]!!.checkApple()
                    mySnakes[i]!!.checkCollisions()
                }
            }
        } else {
            for (g in 0 until generationLimit) {
                if (generation % 10 == 0) {
                    probabilisticChoice = true
                } else {
                    probabilisticChoice = false
                }
                generation += 1
                for (snake in mySnakes) {
                    while (snake!!.alive) {
                        snake.setInputs()
                        snake.brain.forwardPropagation()
                        snake.chooseDirection()
                        snake.move()
                        snake.checkApple()
                        snake.checkCollisions()
                    }
                    if (snake.fitness > bestFitness) {
                        bestFitness = snake.fitness
                        println("Generation: " + generation + " | " + "Fitness: " + bestFitness + " | " + "Apples: " + snake.applesEaten + " | " + "Moves: " + snake.moves)
                        println(snake.brain.exportWeightVector())
                        println()
                    }
                }

                // perform GA
                sortSnakes(mySnakes)
                val children = arrayOfNulls<Snake>(numChildren)
                for (i in 0 until numChildren) {
                    var parent1: Snake?
                    var parent2: Snake?
                    var child: Snake
                    // weighted parent selection based off of fitness
                    if (probabilisticParentSelection) {
                        parent1 = selectRandomParent(mySnakes)
                        parent2 = selectRandomParent(mySnakes)
                    } else {
                        // only the n best snakes can be chosen as parents
                        parent1 = mySnakes[(Math.random() * numParents).toInt()]
                        parent2 = mySnakes[(Math.random() * numParents).toInt()]
                    }
                    child = createChild(parent1!!, parent2!!)
                    children[i] = child
                }

                // do mutations


                // populate next generation
                mySnakes = createPopulation(populationSize)
                integrateChildren(children, mySnakes)
            }
        }


        // *** headless training **
        // generation += 1
        // loop through snakes array

        // while snake.alive
        //snake.setInputs
        //snake.forwardProp
        //snake.chooseDirection (probabalistic, with softmax)
        // snake.move();
        //snake.checkApple();
        //snake.checkCollisions();
        // update best fitness

        // after loop is done
        // sort snakes
        // take n parents, take n children using k crossover
        // add new n best into the next generation
        // do random mutation


        // *** showing progression visually ***
        // loop
        // run generation
        // sort snakes
        // display best
        // repeat
    }
}