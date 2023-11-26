import java.util.*

class NeuralNetwork(
    /** The architecture of the neural network, specifying the number of nodes in each layer.  */
    var architecture: IntArray
) {
    /** The weights between layers in the neural network.  */
    var weights: Array<Array<DoubleArray>?>

    /** The nodes (neurons) in each layer of the neural network.  */
    var nodes: Array<DoubleArray?>

    /** Enumeration representing different activation functions.  */
    enum class ActivationFunction {
        SIGMOID, TANH, RELU, SOFTMAX
    }

    /** The activation function used for hidden layers.  */
    var layerActivation = ActivationFunction.RELU

    /** The activation function used for the output layer.  */
    var outputActivation = ActivationFunction.SIGMOID

    /**
     * Constructor for the NeuralNetwork class.
     *
     * @param arch The architecture of the neural network, specifying the number of nodes in each layer.
     */
    init {
        // creating matrix (2D array) of nodes
        nodes = arrayOfNulls(architecture.size)
        // creating the nodes in each layer
        for (i in nodes.indices) {
            // for example, if architecture = [14,8,4], then our first layer of nodes has 14, our second has 8, and our third has 4
            nodes[i] = DoubleArray(architecture[i])
        }

        // creating tensor (list of 2D array) of weights
        // the weights between each layer can be seen as a 2D array
        // if our architecture is [14,8,4], then our weights array will be an array containing 2D arrays
        // of dimensions [14x8],[8x4]
        weights = arrayOfNulls(architecture.size - 1)
        for (i in 0 until architecture.size - 1) {
            // for 1 layer, each node corresponds to a row, for example, a NN that is [1,2] has a weight array that has 1 row, and 2 columns
            // so weights[0] = new double[1][2];
            // w[i][j][k] = w[i][src][dest]
            weights[i] = Array(architecture[i]) { DoubleArray(architecture[i + 1]) }
        }

        // we also initialize with random weights
        setRandomWeights()
    }

    /**
     * Initializes the weights with random values within a specified range.
     */
    fun setRandomWeights() {
        val minWeight: Int = SnakeGame.weightRange.get(0)
        val maxWeight: Int = SnakeGame.weightRange.get(1)
        for (i in weights.indices) {
            for (j in weights[i]!!.indices) {
                for (k in weights[i]!![j].indices) {
                    // numbers from -1 to 1
                    weights[i]!![j][k] = (maxWeight - minWeight) * Math.random() + minWeight
                }
            }
        }
    }

    /**
     * Performs forward propagation through the neural network.
     */
    fun forwardPropagation() {
        // if we have 3 layers, we propagate twice, since there are 2 sets of weights
        for (i in weights.indices) {
            for (j in nodes[i + 1]!!.indices) {
                var value = 0.0
                for (k in nodes[i]!!.indices) {
                    // since w[i][a][b] = w[i][src][dest], for a fixed dest in the next layer, we loop through all the other nodes attached to it
                    // this is correct
                    value += nodes[i]!![k] * weights[i]!![k][j]
                }
                nodes[i + 1]!![j] = relu(value)
                // output layer
                if (i == weights.size - 1) {
                    nodes[i + 1]!![j] = sigmoid(value)
                }
            }
        }
    }

    /**
     * Applies the softmax activation function to an array of values.
     *
     * @param array The input array.
     * @return The result of the softmax activation function.
     */
    fun softmax(array: DoubleArray): DoubleArray {
        var sum = 0.0
        var s = 0.0
        for (i in array.indices) {
            sum += Math.exp(array[i])
        }
        for (j in array.indices) {
            array[j] = Math.exp(array[j]) / sum
            s += array[j]
        }
        println(s)
        return array
    }

    /**
     * Applies the rectified linear unit (ReLU) activation function to a given value.
     *
     * @param num The input value.
     * @return The result of the ReLU activation function.
     */
    fun relu(num: Double): Double {
        return Math.max(num, 0.0)
    }

    /**
     * Exports the architecture and weight vector of the neural network.
     *
     * @return A string containing the architecture and weight vector.
     */
    fun exportWeightVector(): String {
        // exports architecture and weight vector
        val arc = Arrays.toString(architecture)
        val wei = Arrays.toString(tensorToVector(weights))
        return arc + wei
    }

    /**
     * Imports the architecture and weight vector into the neural network.
     *
     * @param vector The string containing the architecture and weight vector.
     */
    fun importWeightVector(vector: String) {
        val archString = vector.substring(vector.indexOf('[') + 1, vector.indexOf(']'))
        val archStringArray = archString.split(", ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        architecture = Arrays.stream(archStringArray).mapToInt { s: String -> s.toInt() }.toArray()
        val weightString = vector.substring(vector.lastIndexOf('[') + 1, vector.lastIndexOf(']'))
        val weightStringArray = weightString.split(", ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val weightVector = Arrays.stream(weightStringArray).mapToDouble { s: String -> s.toDouble() }
            .toArray()
        weights = vectorToTensor(weightVector, architecture)
    }

    companion object {
        /**
         * Applies the sigmoid activation function to a given value.
         *
         * @param num The input value.
         * @return The result of the sigmoid activation function.
         */
        fun sigmoid(num: Double): Double {
            return 1 / (1 + Math.exp(-1 * num))
        }

        /**
         * Applies the hyperbolic tangent (tanh) activation function to a given value.
         *
         * @param num The input value.
         * @return The result of the tanh activation function.
         */
        fun tanh(num: Double): Double {
            return Math.tanh(num)
        }

        /**
         * Converts a 3D tensor to a 1D vector.
         *
         * @param tensor The input tensor.
         * @return The resulting 1D vector.
         */
        fun tensorToVector(tensor: Array<Array<DoubleArray>?>): DoubleArray {
            // flattens the tensor into a vector (3D array to 1D array)
            var elementsInTensor = 0
            for (matrix in tensor) {
                if (matrix != null) {
                    for (vector in matrix) {
                        for (element in vector) {
                            elementsInTensor++
                        }
                    }
                }
            }
            val newVector = DoubleArray(elementsInTensor)
            var pointer = 0
            for (matrix in tensor) {
                if (matrix != null) {
                    for (vector in matrix) {
                        for (element in vector) {
                            newVector[pointer] = element
                            pointer++
                        }
                    }
                }
            }
            return newVector
        }

        /**
         * Converts a 1D vector and architecture to a 3D tensor.
         *
         * @param vector      The input vector.
         * @param architecture The architecture of the neural network.
         * @return The resulting 3D tensor.
         */
        fun vectorToTensor(vector: DoubleArray, architecture: IntArray): Array<Array<DoubleArray>?> {
            // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20
            // [1,2,3,4]
            // [axb] = [number of mini arrays x length of those mini arrays] (or rows x columns)
            // so we have t[0] = [1x2], t[1] = [2x3], t[2] = [3x4] for a total of 20 weights
            //    t[0] = [1,2]
            //    t[1] = [3,4,5][6,7,8]
            //    t[2] = [9,10,11,12][13,14,15,16][17,18,19,20]
            val tensor: Array<Array<DoubleArray>?> = arrayOfNulls(architecture.size - 1)
            // index we are taking from
            var pointer = 0
            for (i in 0 until architecture.size - 1) {
                tensor[i] = Array(architecture[i]) {
                    DoubleArray(
                        architecture[i + 1]
                    )
                }
                for (j in tensor[i]!!.indices) {
                    for (k in tensor[i]!![j].indices) {
                        tensor[i]!![j][k] = vector[pointer]
                        pointer += 1
                    }
                }
            }
            return tensor
        }
    }
}