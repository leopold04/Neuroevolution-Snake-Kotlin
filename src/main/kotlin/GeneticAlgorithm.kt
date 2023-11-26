import java.util.*

object GeneticAlgorithm {
    /**
     * Creates a population of Snake objects with the specified size.
     *
     * @param size The size of the population.
     * @return An array of Snake objects representing the population.
     */
    fun createPopulation(size: Int): Array<Snake?> {
        val snakeArray = arrayOfNulls<Snake>(size)
        for (i in snakeArray.indices) {
            snakeArray[i] = Snake()
        }
        return snakeArray
    }

    /**
     * Sorts an array of Snake objects by fitness in descending order.
     *
     * @param array The array of Snake objects to be sorted.
     */
    fun sortSnakes(array: Array<Snake?>) {
        array.sortWith(compareByDescending { it?.fitness })
    }

    /**
     * Selects a random parent Snake from an array based on fitness.
     *
     * @param array The array of Snake objects to choose a parent from.
     * @return A randomly selected Snake object.
     */
    fun selectRandomParent(array: Array<Snake?>): Snake? {
        var fitnessSum = 0
        for (value in array) {
            fitnessSum += value!!.fitness
        }
        val position = (Math.random() * fitnessSum).toInt()
        var sum = 0
        for (snake in array) {
            sum += snake!!.fitness
            if (sum >= position) {
                return snake
            }
        }
        return null
    }

    /**
     * Creates a child Snake by combining genes from two parent Snakes.
     *
     * @param parent1 The first parent Snake.
     * @param parent2 The second parent Snake.
     * @return A new Snake representing the child.
     */
    fun createChild(parent1: Snake, parent2: Snake): Snake {
        val child = Snake()
        child.brain.architecture = parent1.brain.architecture
        val parent1Genes: DoubleArray = NeuralNetwork.tensorToVector(parent1.brain.weights)
        val parent2Genes: DoubleArray = NeuralNetwork.tensorToVector(parent2.brain.weights)
        val childGenes: DoubleArray
        childGenes = if (SnakeGame.crossoverType === 0) {
            uniformCrossover(parent1Genes, parent2Genes, SnakeGame.crossoverRate)
        } else {
            kPointCrossover(parent1Genes, parent2Genes, SnakeGame.crossoverType)
        }

        // importing the resulting genes back into the child
        child.brain.weights = NeuralNetwork.vectorToTensor(childGenes, child.brain.architecture)
        return child
    }

    /**
     * Integrates an array of children Snakes into a population.
     *
     * @param children   The array of child Snakes.
     * @param population The array representing the population to integrate the children into.
     */
    fun integrateChildren(children: Array<Snake?>, population: Array<Snake?>?) {
        System.arraycopy(children, 0, population, 0, children.size)
    }

    /**
     * Performs uniform crossover on the genes of two parent Snakes.
     *
     * @param parent1Genes   The genes of the first parent Snake.
     * @param parent2Genes   The genes of the second parent Snake.
     * @param crossoverRate  The percentage of genes each parent contributes to the child.
     * @return An array representing the genes of the resulting child Snake.
     */
    fun uniformCrossover(parent1Genes: DoubleArray, parent2Genes: DoubleArray, crossoverRate: Double): DoubleArray {
        val childGenes = DoubleArray(parent1Genes.size)
        for (i in childGenes.indices) {
            val r = Math.random()
            if (r > crossoverRate) {
                childGenes[i] = parent1Genes[i]
            } else {
                childGenes[i] = parent2Genes[i]
            }
        }
        return childGenes
    }

    /**
     * Performs k-point crossover on the genes of two parent Snakes.
     *
     * @param parent1Genes The genes of the first parent Snake.
     * @param parent2Genes The genes of the second parent Snake.
     * @param k            The number of crossover points.
     * @return An array representing the genes of the resulting child Snake.
     */
    fun kPointCrossover(parent1Genes: DoubleArray, parent2Genes: DoubleArray, k: Int): DoubleArray {
        val points = IntArray(k + 2)
        points[0] = 0
        points[k + 1] = parent1Genes.size
        for (i in 1 until points.size - 1) {
            points[i] = (Math.random() * parent1Genes.size).toInt()
        }
        Arrays.sort(points)
        for (i in 0 until points.size - 1) {
            // swap elements in a and b from index points[i] to points[i+1], but it only happens every other turn
            if (i % 2 == 0) {
                for (j in points[i] until points[i + 1]) {
                    // swap elements a[j] and b[j]
                    val temp = parent1Genes[j]
                    parent1Genes[j] = parent2Genes[j]
                    parent2Genes[j] = temp
                }
            }
        }
        return parent1Genes
    }
}