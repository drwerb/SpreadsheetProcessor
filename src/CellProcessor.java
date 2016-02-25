public abstract class CellProcessor {
    protected Spreadsheet spreadsheet;
    protected CellDependancyManager cellDependancyManager;

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
        walker.run();
        computeDependantData();
    }

    public void processCell(int rowIndex, int columnIndex) {
        SpreadsheetCell cell = spreadsheet.getCell(rowIndex, columnIndex);
        DataTypeProcessor dataTypeProcessor = selectDataType(cell);
        dataTypeProcessor.processCellData(cell, rowIndex, columnIndex);
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
}