public abstract class CellCollection {
    public abstract SpreadsheetCell getCell(int rowIndex, int columnIndex);
    public abstract void putCellData(int rowIndex, int columnIndex, String cellData);
}