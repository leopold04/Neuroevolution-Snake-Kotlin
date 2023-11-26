import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.JPanel
import javax.swing.Timer

class GamePanel internal constructor() : JPanel(), ActionListener {
    var snake: Snake? = null
    var index = 0
    var running = false
    var timer: Timer? = null
    var random: Random
    lateinit var nodes: Array<DoubleArray?>
    lateinit var weights: Array<Array<DoubleArray>?>

    // graphical information
    var nodeSize = 20
    var nodeSpacing = 1.25
    var layerSpacing = 150
    var totalScreenHeight: Int = SCREEN_HEIGHT + SnakeGame.heightBuffer
    var totalScreenWidth: Int = SCREEN_WIDTH + SnakeGame.widthBuffer
    var startingXPos = SCREEN_WIDTH + 50

    init {
        random = Random()
        this.preferredSize = Dimension(SCREEN_WIDTH + SnakeGame.widthBuffer, SCREEN_HEIGHT + SnakeGame.heightBuffer)
        background = Color(242, 229, 194)
        this.isFocusable = true
        addKeyListener(MyKeyAdapter())
        startGame()
    }

    fun startGame() {
        index = SnakeGame.index
        snake = SnakeGame.mySnakes.get(index)
        running = true
        timer = Timer(DELAY, this)
        timer!!.start()
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        draw(g)
    }

    private fun draw(g: Graphics) {
        index = SnakeGame.index
        snake = SnakeGame.mySnakes[index]
        g.color = Color(134, 150, 219)
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)
        if (running) {
            // draw apple
            g.color = Color.red
            g.fillOval(snake!!.apple.x, snake!!.apple.y, UNIT_SIZE, UNIT_SIZE)

            // draw snake
            for (i in 0 until snake!!.bodyParts) {
                if (i == 0) {
                    g.color = Color(116, 173, 134)
                    g.fillRect(snake!!.x.get(i), snake!!.y.get(i), UNIT_SIZE, UNIT_SIZE)
                } else {
                    g.color = Color(45, 180, 0)
                    //g.setColor(new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255)));
                    g.fillRect(snake!!.x.get(i), snake!!.y.get(i), UNIT_SIZE, UNIT_SIZE)
                }
            }

            // draw nodes
            g.color = Color.black
            drawNodes(g)
            drawWeights(g)
            // draw weights

            //draw other information
            g.color = Color.red
            g.font = Font("Ink Free", Font.BOLD, 40)
            val metrics = getFontMetrics(g.font)
            g.drawString(
                "Score: " + snake!!.applesEaten,
                (SCREEN_WIDTH - metrics.stringWidth("Score: " + snake!!.applesEaten)) / 2,
                g.font.size
            )
        }
    }

    fun drawNodes(g: Graphics) {
        nodes = snake!!.brain.nodes
        startingXPos = SCREEN_WIDTH + 50
        for (i in nodes.indices) {
            for (j in nodes[i]!!.indices) {
                // drawing a white circle underneath so that low values show white, not transparent
                g.color = Color.white
                g.fillOval(
                    startingXPos + i * layerSpacing,
                    (totalScreenHeight - nodes[i]!!.size * nodeSize * nodeSpacing / 2 + nodeSize * j * nodeSpacing).toInt(),
                    nodeSize,
                    nodeSize
                )
                val nodeValue = Math.abs(nodes[i]!![j])
                // drawing inputs, 4 is bc the apple multiplier
                if (i == 0) {
                    g.color = Color(15, 40, 90, (NeuralNetwork.tanh(nodeValue) * 255).toInt())
                } else {
                    g.color = Color(15, 40, 90, (NeuralNetwork.tanh(nodeValue) * 255).toInt())
                }
                g.fillOval(
                    startingXPos + i * layerSpacing,
                    (totalScreenHeight - nodes[i]!!.size * nodeSize * nodeSpacing / 2 + nodeSize * j * nodeSpacing).toInt(),
                    nodeSize,
                    nodeSize
                )
            }
        }
    }

    fun drawWeights(g: Graphics) {
        weights = snake!!.brain.weights
        for (i in weights.indices) {
            for (j in weights[i]!!.indices) {
                for (k in weights[i]!![j].indices) {
                    val weightValue = weights[i]!![j][k]
                    // alpha value is based off of the activation of our weight (1 is bright blue, -0.25 is pale red)
                    // scaled so that colors don't exceed 1, (divided by weight range)
                    val blue = Color(25, 75, 135, (Math.abs(weightValue / SnakeGame.weightRange.get(1)) * 255).toInt())
                    val red = Color(175, 45, 45, (Math.abs(weightValue / SnakeGame.weightRange.get(0)) * 255).toInt())
                    if (weightValue >= 0) {
                        g.color = blue
                    } else {
                        g.color = red
                    }
                    // since java places the top left corner of object at coordinate, we need to shift it so that the lines go from edge to edge of circle
                    g.drawLine(
                        startingXPos + i * layerSpacing + nodeSize,
                        ((totalScreenHeight - nodes[i]!!.size * nodeSize * nodeSpacing) / 2 + nodeSize * j * nodeSpacing).toInt() + nodeSize / 2,
                        startingXPos + (i + 1) * layerSpacing,
                        ((totalScreenHeight - nodes[i + 1]!!.size * nodeSize * nodeSpacing) / 2 + nodeSize * k * nodeSpacing).toInt() + nodeSize / 2
                    )
                }
            }
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        if (running) repaint()
    }

    inner class MyKeyAdapter : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_LEFT -> if (snake!!.direction !== 'R') {
                    snake!!.direction = 'L'
                }

                KeyEvent.VK_RIGHT -> if (snake!!.direction !== 'L') {
                    snake!!.direction = 'R'
                }

                KeyEvent.VK_UP -> if (snake!!.direction !== 'D') {
                    snake!!.direction = 'U'
                }

                KeyEvent.VK_DOWN -> if (snake!!.direction !== 'U') {
                    snake!!.direction = 'D'
                }
            }
        }
    }

    companion object {
        val SCREEN_WIDTH: Int = SnakeGame.width
        val SCREEN_HEIGHT: Int = SnakeGame.height
        val UNIT_SIZE: Int = SnakeGame.unit_size
        val GAME_UNITS: Int = SnakeGame.GAME_UNITS
        const val DELAY = 60
    }
}