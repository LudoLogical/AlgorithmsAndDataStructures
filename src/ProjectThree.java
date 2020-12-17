import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * CS 3345 HON
 * Project 3, GRAPH ALGORITHMS
 * Used compiler Java 14.0.1
 * @author UGQM
 * @version date 11/20/2020
 */

class DisjointSet {

    private final int[] set;

    /**
     * Creates and returns a new DisjointSet {0, 1, 2, ..., size - 1} of disjoint elements.
     * @param size the number of elements in this DisjointSet
     */
    public DisjointSet(int size) {
        set = new int[size];
        for (int i = 0; i < size; i++) {
            set[i] = -1;
        }
    }

    /**
     * Locates and returns the root element of tree
     * representing the set to which the specified element belongs.
     * @param subject the element to be located
     * @return the root element of the tree representing the set containing subject
     */
    public int find(int subject) {
        int target = set[subject];
        if (target >= 0) {
            int root = find(target);
            set[subject] = root;
            return root;
        } else {
            return subject;
        }
    }

    /**
     * Joins the sets containing the specified elements
     * if they are not already part of the same set.
     * @param a an element of the first set to be joined
     * @param b an element of the second set to be joined
     */
    public void union(int a, int b) {
        int aRoot = find(a);
        int bRoot = find(b);
        if (aRoot != bRoot) {
            // Note: union is by size
            if (set[aRoot] <= set[bRoot]) {
                set[aRoot] += set[bRoot];
                set[bRoot] = aRoot;
            } else {
                set[bRoot] += set[aRoot];
                set[aRoot] = bRoot;
            }
        }
    }

}

class UnweightedGraph {

    public static final int DISTANCE = 0;
    public static final int PREDECESSOR = 1;

    private final int[][] adjacencies;

    /**
     * Creates and returns a new UnweightedGraph with the specified number
     * of vertices (labeled 0, 1, 2, ..., numVertices - 1) and no edges.
     * @param numVertices the number of vertices in this UnweightedGraph; cannot be changed after object creation
     */
    public UnweightedGraph(int numVertices) {
        adjacencies = new int[numVertices][numVertices]; // entries automatically initialized to 0
    }

    /**
     * Gives the number of vertices in this UnweightedGraph.
     * @return the number of vertices in this UnweightedGraph
     */
    public int getNumVertices() {
        return adjacencies.length;
    }

    /**
     * Creates a (directed) edge beginning at vertex a and ending at vertex b. Note that
     * an "undirected" edge can be created by calling both connect(a, b) and connect(b, a).
     * @param a the tail (origin vertex) of the edge to be created
     * @param b the head (terminal vertex) of the edge to be created
     */
    public void connect(int a, int b) {
        adjacencies[a][b] = 1;
    }

    /**
     * Calculates and returns information about shortest paths from
     * every vertex to every other vertex in this UnweightedGraph using the
     * Floyd-Warshall All Pairs Shortest Path Algorithm and ignoring self-loops.
     * @return a two-dimensional array x whose entries are ordered pairs such that
     *         x[i][j][UnweightedGraph.DISTANCE] contains the number of vertices on a
     *         shortest path from vertex i to vertex j (or Integer.MAX_VALUE if those
     *         vertices are unreachable from each other) and
     *         x[i][j][UnweightedGraph.PREDECESSOR] contains the vertex immediately
     *         preceding vertex j on a shortest path from vertex i to vertex j unless
     *         vertices i and j are adjacent or unreachable from each other, in which
     *         case it contains -1.
     */
    public int[][][] shortestPathsFW() {

        // Initialize distance and predecessor variables
        int[][][] output = new int[adjacencies.length][adjacencies.length][2];
        for (int i = 0; i < adjacencies.length; i++) {
            for (int j = 0; j < adjacencies.length; j++) {
                if (i == j) {
                    output[i][j][DISTANCE] = 0; // required assumption
                } else {
                    if (adjacencies[j][i] == 0) {
                        output[i][j][DISTANCE] = Integer.MAX_VALUE; // i.e., unreachable (so far)

                    } else {
                        output[i][j][DISTANCE] = 1;
                    }
                }
                output[i][j][PREDECESSOR] = -1; // i.e., no predecessor (yet)
            }
        }

        // For all vertices k, check if the shortest path from every
        // i to every j would be improved by routing through k
        for (int k = 0; k < adjacencies.length; k++) {
            for (int i = 0; i < adjacencies.length; i++) {
                for (int j = 0; j < adjacencies.length; j++) {
                    // Avoid integer overflow by checking for MAX_VALUE before addition
                    if (output[i][k][DISTANCE] < Integer.MAX_VALUE && output[k][j][DISTANCE] < Integer.MAX_VALUE) {
                        if (output[i][k][DISTANCE] + output[k][j][DISTANCE] < output[i][j][DISTANCE]) {
                            output[i][j][DISTANCE] = output[i][k][DISTANCE] + output[k][j][DISTANCE];
                            output[i][j][PREDECESSOR] = k;
                        }
                    }
                }
            }
        }

        return output;

    }

    // Gives an array containing the vertices of this UnweightedGraph
    // MergeSorted by their outDegrees from least to greatest
    private int[] sortByOutDegrees() {

        // Obtain out-degrees of all vertices
        int vertexTotal;
        int[] outDegrees = new int[adjacencies.length];
        for (int i = 0; i < adjacencies.length; i++) {
            vertexTotal = 0;
            for (int j = 0; j < adjacencies.length; j++) {
                if (adjacencies[i][j] == 1) {
                    vertexTotal++;
                }
            }
            outDegrees[i] = vertexTotal;
        }

        // Set up vertices array
        int[] vertices = new int[adjacencies.length];
        for (int i = 0; i < adjacencies.length; i++) {
            vertices[i] = i;
        }

        sortByOutDegrees(vertices, outDegrees, 0, vertices.length - 1);
        return vertices;

    }

    // Recursive facilitator method for the modified MergeSort outlined above
    private void sortByOutDegrees(int[] vertices, int[] outDegrees, int left, int right) {
        if (left < right) {

            // Recurse to sort left and right halves
            int center = (left + right) / 2;
            sortByOutDegrees(vertices, outDegrees, left, center);
            sortByOutDegrees(vertices, outDegrees, center + 1, right);

            // Prep to merge halves
            int currentInLeftHalf = left;
            int currentInRightHalf = center + 1;
            boolean leftHalfDone, rightHalfDone;
            int[] merged = new int[right - left + 1];

            // Merge halves
            for (int i = 0; i < merged.length; i++) {

                leftHalfDone = currentInLeftHalf > center;
                rightHalfDone = currentInRightHalf > right;

                if (leftHalfDone) {
                    merged[i] = vertices[currentInRightHalf];
                    currentInRightHalf++;
                } else if (rightHalfDone) {
                    merged[i] = vertices[currentInLeftHalf];
                    currentInLeftHalf++;
                } else {
                    if (outDegrees[vertices[currentInLeftHalf]] <= outDegrees[vertices[currentInRightHalf]]) {
                        merged[i] = vertices[currentInLeftHalf];
                        currentInLeftHalf++;
                    } else {
                        merged[i] = vertices[currentInRightHalf];
                        currentInRightHalf++;
                    }
                }

            }

            // Replace unsorted entries with sorted entries
            if (right + 1 - left >= 0) System.arraycopy(merged, 0, vertices, left, right + 1 - left);

        }
    }

    /**
     * Calculates and returns an upper bound (estimate) for the chromatic
     * number of this UnweightedGraph using a modified "greedy" approach.
     * @return the estimated chromatic number
     */
    public int estimateChromaticNumber() {

        int chromaticNumber = 1;
        int[] verticesByOutDegree = sortByOutDegrees();
        int[] vertexColors = new int[adjacencies.length]; // for storing colors of vertices once decided on
        for (int i = 0; i < adjacencies.length; i++) {
            vertexColors[i] = -1; // signals no color yet
        }

        // Starting with the vertex of highest out-degree and working our way down...
        for (int i = verticesByOutDegree.length - 1; i >= 0; i--) {

            // ...figure out which colors have already been claimed
            // by neighbors of the vertex currently being considered...
            boolean[] colorsTaken = new boolean[chromaticNumber];
            for (int j = 0; j < adjacencies.length; j++) {
                if (adjacencies[verticesByOutDegree[i]][j] == 1 || adjacencies[j][verticesByOutDegree[i]] == 1) {
                    if (vertexColors[j] >= 0) {
                        colorsTaken[vertexColors[j]] = true;
                    }
                }
            }

            // ...and assign an existing color to the vertex currently being considered (if possible)...
            for (int color = 0; color < chromaticNumber; color++) {
                if (!colorsTaken[color]) {
                    vertexColors[verticesByOutDegree[i]] = color;
                    break;
                }
            }

            // ...or create a new color if every existing color has already been
            // assigned to a neighbor of the vertex currently being considered
            if (vertexColors[verticesByOutDegree[i]] == -1) {
                vertexColors[verticesByOutDegree[i]] = chromaticNumber;
                chromaticNumber++;
            }

        }

        return chromaticNumber;

    }

}

class WeightedGraph {

    private final double[][] edgeWeights;

    /**
     * Creates and returns a new WeightedGraph with the specified number
     * of vertices (labeled 0, 1, 2, ..., numVertices - 1) and no edges.
     * @param numVertices the number of vertices in this UnweightedGraph; cannot be changed after object creation
     */
    public WeightedGraph(int numVertices) {
        edgeWeights = new double[numVertices][numVertices];
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < numVertices; j++) {
                edgeWeights[i][j] = Double.MAX_VALUE; // denotes that there is no edge between i and j
            }
        }
    }

    /**
     * Gives the weight of the edge from vertex a to vertex b if that
     * edge exists and throws an IllegalArgumentException if it does not.
     * @param a the tail (origin vertex) of the edge in question
     * @param b the head (terminal vertex) of the edge in question
     * @return the weight of the edge starting at vertex a and ending at vertex b
     */
    public double getWeightOfEdge(int a, int b) {
        if (edgeWeights[a][b] == Double.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot getWeightOfEdge() that does not exist!");
        } else {
            return edgeWeights[a][b];
        }
    }

    /**
     * Converts this WeightedGraph into an UnweightedGraph with the same vertices and edges.
     * @return an unweighted version of this WeightedGraph
     */
    public UnweightedGraph bifurcate() {
        UnweightedGraph output = new UnweightedGraph(edgeWeights.length);
        for (int i = 0; i < edgeWeights.length; i++) {
            for (int j = 0; j < edgeWeights.length; j++) {
                if (edgeWeights[i][j] != Double.MAX_VALUE) {
                    output.connect(i, j);
                }
            }
        }
        return output;
    }

    /**
     * Creates a (directed) edge beginning at vertex a and ending at vertex b with the specified weight. Note
     * that an "undirected" edge can be created by calling both connect(a, b, weight) and connect(b, a, weight).
     * @param a the tail (origin vertex) of the edge to be created
     * @param b the head (terminal vertex) of the edge to be created
     * @param weight the weight of the edge to be created
     */
    public void connect(int a, int b, double weight) {
        edgeWeights[a][b] = weight;
    }

    /**
     * Generates a Minimal Euclidean Spanning Tree for this WeightedGraph using Kruskal's Algorithm.
     * @return an array of edges (i.e., ordered pairs of vertices) that comprise
     *         a Minimal Euclidean Spanning tree for this WeightedGraph
     */
    public int[][] getKruskalMEST() {

        int[][] minSpanTree = new int[edgeWeights.length - 1][];
        DisjointSet vertices = new DisjointSet(edgeWeights.length);

        int[] minUsefulEdge;
        double minUsefulEdgeWeight;

        // until the MEST is complete (i.e., contains |V|-1 edges)...
        for (int k = 0; k < minSpanTree.length; k++) {

            minUsefulEdge = new int[]{-1, -1};
            minUsefulEdgeWeight = Double.MAX_VALUE;

            // ...find the smallest-weight edge that won't create a cycle...
            for (int i = 0; i < edgeWeights.length; i++) {
                for (int j = 0; j < edgeWeights.length; j++) {
                    if (edgeWeights[i][j] < minUsefulEdgeWeight) {
                        // ...(automatically ignoring possible self-loops)...
                        if (vertices.find(i) != vertices.find(j)) {
                            minUsefulEdge[0] = i;
                            minUsefulEdge[1] = j;
                            minUsefulEdgeWeight = edgeWeights[i][j];
                        }
                    }
                }
            }

            // ...and, unless none is found (in which case there is no *single* MEST)...
            if (minUsefulEdge[0] == -1) {
                throw new UnsupportedOperationException("Cannot find an MEST for a disconnected graph!");
            }

            // ... add it to the MEST
            minSpanTree[k] = minUsefulEdge;
            vertices.union(minUsefulEdge[0], minUsefulEdge[1]);

        }

        return minSpanTree;

    }

}

public class ProjectThree {

    public static void main(String[] args) throws IOException {
        // Produce output from input based on project spec
        WeightedGraph graph = makeGraphFromInputFile("GraphData.txt"); // step (1)
        produceMinimalSpanningTree(graph); // step (2)
        UnweightedGraph unweighted = graph.bifurcate(); // step (3a)
        produceShortestPathsAndDiameter(unweighted); // step (3b) and step (4)
        System.out.println(unweighted.estimateChromaticNumber()); // step (5)
    }

    public static WeightedGraph makeGraphFromInputFile(String path) throws IOException {

        Scanner input = new Scanner(new File(path));

        // get number of radios
        int numRadios = Integer.parseInt(input.nextLine());
        WeightedGraph graph = new WeightedGraph(numRadios);

        // get all radio locations
        String[] now;
        double[][] locations = new double[numRadios][2];
        for (int i = 0; i < numRadios; i++) {
            now = input.nextLine().split(" ");
            locations[i][0] = Double.parseDouble(now[0]);
            locations[i][1] = Double.parseDouble(now[1]);
        }

        // get the range of the radios
        double radius = Double.parseDouble(input.nextLine());

        input.close();

        // convert radio locations into distances, then edges where applicable
        for (int i = 0; i < numRadios; i++) {
            for (int j = i + 1; j < numRadios; j++) {
                double pythagoreanDistance = Math.sqrt(Math.pow(locations[i][0] - locations[j][0], 2)
                        + Math.pow(locations[i][1] - locations[j][1], 2));
                if (pythagoreanDistance <= radius) {
                    graph.connect(i, j, pythagoreanDistance);
                    graph.connect(j, i, pythagoreanDistance);
                }
            }
        }

        return graph;

    }

    public static void produceMinimalSpanningTree(WeightedGraph graph) {

        // Set up and retrieve MEST
        double nowWeight, totalWeight = 0;
        int[][] minSpanTree = graph.getKruskalMEST();
        DecimalFormat twoDecimalPlaces = new DecimalFormat("0.00");

        // Print MEST and total length
        for (int[] edge : minSpanTree) {
            nowWeight = graph.getWeightOfEdge(edge[0], edge[1]);
            System.out.println((edge[0] + 1) + " " + (edge[1] + 1) + " " + twoDecimalPlaces.format(nowWeight));
            totalWeight += nowWeight;
        }
        System.out.println(twoDecimalPlaces.format(totalWeight));

    }

    public static void produceShortestPathsAndDiameter(UnweightedGraph graph) {

        int[][][] shortestPaths = graph.shortestPathsFW(); // retrieve shortest paths info

        for (int i = 1; i < graph.getNumVertices(); i++) {

            // End of output line contains node ID (i + 1 according to spec) followed by hop count
            StringBuilder outputLine = new StringBuilder((i + 1) + " " + shortestPaths[0][i][UnweightedGraph.DISTANCE]);
            int predecessor = shortestPaths[0][i][UnweightedGraph.PREDECESSOR];

            // Now, we step backwards along the predecessor chain, adding to the input line until reaching node ID 1
            while (predecessor != -1) {
                outputLine.insert(0, (predecessor + 1) + " ");
                predecessor = shortestPaths[0][predecessor][UnweightedGraph.PREDECESSOR];
            }

            // Prepend node ID 1 before printing
            System.out.println("1 " + outputLine);

        }

        // The diameter is simply the greatest value of shortestPaths[i][j][UnweightedGraph.DISTANCE]
        int diameter = 0;
        for (int i = 0; i < graph.getNumVertices(); i++) {
            // Undirected means we can skip 1/2 of vertex pairs
            for (int j = i + 1; j < graph.getNumVertices(); j++) {
                if (shortestPaths[i][j][UnweightedGraph.DISTANCE] > diameter) {
                    diameter = shortestPaths[i][j][UnweightedGraph.DISTANCE];
                }
            }
        }

        System.out.println(diameter);

    }

}