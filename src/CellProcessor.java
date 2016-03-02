import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;

public abstract class CellProcessor {
    protected Spreadsheet spreadsheet;
    protected CellDependancyManager cellDependancyManager;
    private ArrayBlockingQueue<CellPosition> processingCellsShared;
    private ArrayList<Thread> workers;

    final int WORKERS_NUMBER = 2;

    protected abstract void initCellDependancyManager();
    protected abstract void initDataTypeProcessorMap();
    public abstract void computeDependantData();

    public abstract DataTypeProcessor selectDataType(SpreadsheetCell cell);

    public CellProcessor(Spreadsheet s) {
        spreadsheet = s;
        initCellDependancyManager();
        initDataTypeProcessorMap();
    }

    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    public CellDependancyManager getCellDependancyManager() {
        return cellDependancyManager;
    }

    public void processSpreadsheetCells() {
        CellProcessorWalker walker = new CellProcessorWalker(this);
        startProcessorWorkers();
        walker.run();
        stopProcessorWorkers();
        computeDependantData();
    }

    private void startProcessorWorkers() {
        workers = new ArrayList<Thread>();
        processingCellsShared = new ArrayBlockingQueue<CellPosition>(WORKERS_NUMBER);
        Thread worker;

        for (int i = 0; i < WORKERS_NUMBER; i++) {
            worker = new Thread(new ProcessorWorker(processingCellsShared, this));
            workers.add(worker);
            worker.start();
        }
    }

    private boolean hasAliveWorkers() {
        return processingCellsShared.peek() != null;
    }

    private void stopProcessorWorkers() {
        for (int i = 0; i < WORKERS_NUMBER; i++) {
            try {
                processingCellsShared.put(new CellPosition(-1, 0));
            }
            catch (InterruptedException e) {
                System.exit(1);
            }
        }

        while (hasAliveWorkers()) {
        }
    }

    public void processCell(int rowIndex, int columnIndex) {
        try {
            processingCellsShared.put(new CellPosition(rowIndex, columnIndex));
        }
        catch (InterruptedException e) {
            System.exit(1);
        }
    }

    public String getCellComputedData(SpreadsheetCell cell) {
        DataTypeProcessor dataTypeProcessor = selectDataType(cell);
        return dataTypeProcessor.getCellComputedData(cell);
    }

    protected abstract class DataTypeProcessor {
        protected CellProcessor cellProcessor;

        public DataTypeProcessor(CellProcessor c) {
            cellProcessor = c;
        }

        public void processCellData(SpreadsheetCell cell, int rowIndex, int columnIndex) {
            // method does nothing by default
        }

        public abstract String getCellComputedData(SpreadsheetCell cell);
    }

    protected class CellProcessorWalker extends SpreadsheetRowByRowCellWalker {
        protected CellProcessor processor;

        public CellProcessorWalker(CellProcessor cellProcessor) {
            super(cellProcessor.getSpreadsheet());
            processor = cellProcessor;
        }

        public void processCell(int rowIndex, int columnIndex) {
            processor.processCell(rowIndex, columnIndex);
        }
    }

    public class CellPosition {
        public int rowIndex;
        public int columnIndex;

        public CellPosition(int row, int column) {
            rowIndex = row;
            columnIndex = column;
        }
    }

    public class ProcessorWorker implements Runnable {
        private ArrayBlockingQueue<CellPosition> processingCellsShared;
        final private CellProcessor processor;

        public ProcessorWorker(ArrayBlockingQueue<CellPosition> processingCells, CellProcessor p) {
            processingCellsShared = processingCells;
            processor = p;
        }

        public void run() {
            CellPosition cellPos = null;
            while (true) {
                try {
                    cellPos = processingCellsShared.take();
                }
                catch (InterruptedException e) {
                }

                if (cellPos.rowIndex == -1) {
                    break;
                }

                SpreadsheetCell cell = processor.getSpreadsheet().getCell(cellPos.rowIndex, cellPos.columnIndex);
                DataTypeProcessor dataTypeProcessor = processor.selectDataType(cell);
                dataTypeProcessor.processCellData(cell, cellPos.rowIndex, cellPos.columnIndex);
            }
        }
    }
}