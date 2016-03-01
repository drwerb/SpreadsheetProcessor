import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Stack;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

class CellDependancyManager {
    protected DependancyGraph graph;
    ConcurrentLinkedDeque<String> reverseOrderVertexes;
    HashSet<String> evaluatedReferences;
    HashMap<String, String> cycledReferences;
    private HashMap<String,String> translatedRefMap;

    public CellDependancyManager() {
        graph = new DependancyGraph();
        evaluatedReferences = new HashSet<String>();
        cycledReferences = new HashMap<String, String>();
        translatedRefMap = new HashMap<String,String>();
    }

    protected void addReferenceVertex(String reference) {
        if (!graph.hasVertex(reference)) {
            graph.addVertex(reference);
        }
    }

    public synchronized void addDependancy(String dependantRef, String dependantByRef) {
        addReferenceVertex(dependantRef);
        addReferenceVertex(dependantByRef);
        graph.addEdge(dependantByRef, dependantRef);
    }

    public void makeTopologicalSort() {
        Stack<String> processingVertexes = new Stack<String>();
        GraphVertex processingVertex;
        int processingStackHeight = 0;
        boolean markBlack;

        reverseOrderVertexes = new ConcurrentLinkedDeque<String>();

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
                        deepFirstWalkMarkCycled(processingVertex.name);
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
    
    private void deepFirstWalkMarkCycled(String headVertexName) {
        Stack<String> processingVertexes = new Stack<String>();
        GraphVertex processingVertex;
        
        processingVertexes.push(headVertexName);
        
        while (!processingVertexes.empty()){
            processingVertex = graph.getVertex(processingVertexes.pop());
            
            if (processingVertex.color == GraphVertex.V_COLOR_BLACK) {
                continue;
            }
            
            processingVertex.color = GraphVertex.V_COLOR_BLACK;
            markReferenceAsCycled(processingVertex.name, headVertexName);
            markReferenceAsEvaluated(processingVertex.name);
            
            for (String nextVertexName : processingVertex.nextVertexes) {
                processingVertexes.push(nextVertexName);
            }
        }
            
    }

    public boolean isCellCycledByReference(String cellReference) {
        return cycledReferences.containsKey(cellReference);
    }

    public void markReferenceAsCycled(String dependantReference, String dependantBy) {
        cycledReferences.put(dependantReference, dependantBy);
    }
    
    public boolean isCellEvaluatedByReference(String cellReference) {
        return evaluatedReferences.contains(cellReference);
    }

    public void markReferenceAsEvaluated(String evaluatedReference) {
        if (reverseOrderVertexes.peekLast() == evaluatedReference) reverseOrderVertexes.pollLast();
        evaluatedReferences.add(evaluatedReference);
    }

    public void evaluateDependenciesBy(String targetReference, Spreadsheet spreadsheet) {
        int[] indexes;
        String evaluatedReference;

        if (isCellEvaluatedByReference(targetReference)) return;

        while (!reverseOrderVertexes.isEmpty() && !reverseOrderVertexes.peekLast().equals(targetReference)) {
        	evaluatedReference = reverseOrderVertexes.peekLast();
            indexes = CellInfoUtils.converNumericFormToRowCol(evaluatedReference);
            String resultBuffer = spreadsheet.getCellComputedData(indexes[0], indexes[1]);
            markReferenceAsEvaluated(evaluatedReference);
        }
    }
    
    public void setCycledCells(Spreadsheet spreadsheet) {
        int[] indexes;
        String dependantByRef;
        CellMetadata meta;
        
        for (String cycledReference : cycledReferences.keySet()) {
            indexes = CellInfoUtils.converNumericFormToRowCol(cycledReference);
            meta = spreadsheet.getCell(indexes[0], indexes[1]).getMetadata();
            dependantByRef = cycledReferences.get(cycledReference);
            meta.setErrorText("depends on cycled ref " + translateReference(dependantByRef));
        }
    }
    
    public String translateReference(String reference) {
        return translatedRefMap.get(reference);
    }
    
    public String addReferenceTranslation(String reference, String refTranslation) {
        return translatedRefMap.put(reference, refTranslation);
    }

    protected class DependancyGraph {
        protected ConcurrentHashMap<String, GraphVertex> vertexes;
        public boolean isCycled;

        public DependancyGraph() {
            vertexes = new ConcurrentHashMap<String, GraphVertex>();
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
            synchronized (this) {
                nextVertexes.add(nextVertexName);
            }
        }
    }
}