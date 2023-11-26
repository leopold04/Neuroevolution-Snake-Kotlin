import javax.swing.JFrame

class GameFrame internal constructor() : JFrame() {
    init {
        this.add(GamePanel())
        title = "Snake"
        defaultCloseOperation = EXIT_ON_CLOSE
        this.isResizable = false
        pack()
        this.isVisible = true
        setLocationRelativeTo(null)
    }
}