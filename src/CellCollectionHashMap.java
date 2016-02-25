import java.util.HashMap;

class CellCollectionHashMap implements CellCollection {
    HashMap<String, SpreadsheetCell> cells;
    private int maxColumnIndex;
    private int maxRowIndex;

    public CellCollectionHashMap() {
        cells = new HashMap<String,SpreadsheetCell>();
        maxRowIndex = -1;
        maxColumnIndex = -1;
    }

    public SpreadsheetCell getCell(int rowIndex, int columnIndex) {
        return cells.get(CellInfoUtils.getCellNumericReference(rowIndex, columnIndex));
    }

    public int getRowsCount() {
        return maxRowIndex + 1;
    }

    public int getColumnsCount() {
        return maxColumnIndex + 1;
    }

    public void putCellData(int rowIndex, int columnIndex, String cellData) {
        String key = CellInfoUtils.getCellNumericReference(rowIndex, columnIndex);
        SpreadsheetCell cell = cells.get(key);

        if (cell == null) {
            cell = new SpreadsheetCell(cellData);
            cells.put(key, cell);
        }
        else {
            cell.setData(cellData);
        }

        if (maxRowIndex < rowIndex) {
            maxRowIndex = rowIndex;
        }

        if (maxColumnIndex < columnIndex) {
            maxColumnIndex = columnIndex;
        }
    }
}