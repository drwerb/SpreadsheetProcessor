interface CellCollection {
    public SpreadsheetCell getCell(int rowIndex, int columnIndex);
    public int getRowsCount();
    public int getColumnsCount();
    public void putCellData(int rowIndex, int columnIndex, String cellData);
}