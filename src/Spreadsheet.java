public abstract class Spreadsheet {
    protected CellCollection cells;
    protected CellProcessor cellProcessor;

    protected int maxColumnIndex;
    protected int maxRowIndex;

    protected abstract void initCellProcessor();
    protected abstract void initCellCollection();

    public Spreadsheet() {
        maxRowIndex = -1;
        maxColumnIndex = -1;
        initCellProcessor();
        initCellCollection();
    }

    public int getRowsCount() {
        return maxRowIndex + 1;
    }

    public int getColumnsCount() {
        return maxColumnIndex + 1;
    }

    public SpreadsheetCell getCell(int rowIndex, int columnIndex) {
        return cells.getCell(rowIndex, columnIndex);
    }

    public String getCellComputedData(int rowIndex, int columnIndex) {
        SpreadsheetCell cell = cells.getCell(rowIndex, columnIndex);

        if (cell == null) {
            return null;
        }

        return cellProcessor.getCellComputedData(cell);
    }

    public void setCellRawData(int rowIndex, int columnIndex, String rawCellData) {
        cells.putCellData(rowIndex, columnIndex, rawCellData);

        if (maxRowIndex < rowIndex) {
            maxRowIndex = rowIndex;
        }

        if (maxColumnIndex < columnIndex) {
            maxColumnIndex = columnIndex;
        }
    }

    public void processData() {
        cellProcessor.processSpreadsheetCells();
    }
}