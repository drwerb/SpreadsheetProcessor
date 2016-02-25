import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

class CellDependancyManager {
    protected DependancyGraph graph;
    ConcurrentLinkedQueue<String> reverseOrderVertexes;
    HashSet<String> evaluatedReferences;

    public CellDependancyManager() {
        graph = new DependancyGraph();
        evaluatedReferences = new HashSet<String>();
    }

    protected void addReferenceVertex(String reference) {
        if (!graph.hasVertex(reference)) {
            graph.addVertex(reference);
        }
    }

    public void addDependancy(String dependantRef, String dependantByRef) {
        addReferenceVertex(dependantRef);
        addReferenceVertex(dependantByRef);
        graph.addEdge(dependantRef, dependantByRef);
    }

    public void makeTopologicalSort() {
        Stack<String> processingVertexes = new Stack<String>();
        GraphVertex processingVertex;
        int processingStackHeight = 0;
        boolean markBlack;

        reverseOrderVertexes = new ConcurrentLinkedQueue<String>();

        for (String vertexName: graph.getAllVertexNames()) {
            processingVertexes.push(vertexName);
            processingStackHeight++;

            while (!processingVertexes.empty()) {
                processingVertex = graph.getVertex(processingVertexes.peek());
                markBlack = false;

                if (processingVertex.color == GraphVertex.V_COLOR_GRAY) {

                    if (processingVertex.stackPos == processingStackHeight) {
                	markBlack = true;
                    }
                    else {
                	graph.isCycled = true;
                	return;
                    }
                }

                if (processingVertex.color == GraphVertex.V_COLOR_BLACK) {
                	processingVertexes.pop();
                    processingStackHeight--;
                    continue;
                }
                
                if (processingVertex.nextVertexes.isEmpty() || markBlack) {
                    processingVertex.color = GraphVertex.V_COLOR_BLACK;
                    reverseOrderVertexes.add(processingVertexes.pop());
                    processingStackHeight--;
                    continue;
                }

                processingVertex.color = GraphVertex.V_COLOR_GRAY;
                processingVertex.stackPos = processingStackHeight;

                for (String nextVertexName : processingVertex.nextVertexes) {
                    processingVertexes.push(nextVertexName);
                    processingStackHeight++;
                }
            }
        }
    }

    public boolean isCellEvaluatedByReference(String cellReference) {
        return evaluatedReferences.contains(cellReference);
    }

    public void markReferenceAsEvaluated(String evaluatedReference) {
        if (reverseOrderVertexes.peek() == evaluatedReference) reverseOrderVertexes.poll();
        evaluatedReferences.add(evaluatedReference);
    }

    public void evaluateDependenciesBy(String targetReference, Spreadsheet spreadsheet) {
        int[] indexes;
        String evaluatedReference;

        if (isCellEvaluatedByReference(targetReference)) return;

        while (!reverseOrderVertexes.isEmpty() && reverseOrderVertexes.peek().equals(targetReference)) {
        	evaluatedReference = reverseOrderVertexes.peek();
            indexes = CellInfoUtils.converNumericFormToRowCol(evaluatedReference);
            String resultBuffer = spreadsheet.getCellComputedData(indexes[0], indexes[1]);
            markReferenceAsEvaluated(evaluatedReference);
        }
    }

    protected class DependancyGraph {
        protected HashMap<String, GraphVertex> vertexes;
        public boolean isCycled;

        public DependancyGraph() {
            vertexes = new HashMap<String, GraphVertex>();
            isCycled = false;
        }

        public GraphVertex getVertex(String vertexName) {
            return vertexes.get(vertexName);
        }

        public Set<String> getAllVertexNames() {
            return vertexes.keySet();
        }

        public boolean hasVertex(String name) {
            return vertexes.containsKey(name);
        }

        public void addVertex(String name) {
            vertexes.put(name, new GraphVertex(name));
        }

        public void addEdge(String vertexNameFrom, String vertexNameTo) {
            GraphVertex vertexFrom = vertexes.get(vertexNameFrom);

            vertexFrom.addNextVertex(vertexNameTo);
        }
    }

    protected class GraphVertex {
        public static final int V_COLOR_WHITE = 0;
        public static final int V_COLOR_GRAY = 1;
        public static final int V_COLOR_BLACK = 2;

        public String name;
        public HashSet<String> nextVertexes;
        public int color;
        public int stackPos;

        public GraphVertex(String vertexName) {
            nextVertexes = new HashSet<String>();
            name = vertexName;
            color = V_COLOR_WHITE;
        }

        public void addNextVertex(String nextVertexName) {
            nextVertexes.add(nextVertexName);
        }
    }
}