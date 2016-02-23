public abstract class Spreadsheet {
    protected CellCollection cells;
    protected CellProcessor cellProcessor;

    protected abstract void initCellProcessor();
    protected abstract void initCellCollection();

    public Spreadsheet() {
        initCellProcessor();
        initCellCollection();
    }

    public int getRowsCount() {
        return cells.getRowsCount();
    }

    public int getColumnsCount() {
        return cells.getColumnsCount();
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
    }

    public void processData() {
        cellProcessor.processSpreadsheetCells();
    }
}