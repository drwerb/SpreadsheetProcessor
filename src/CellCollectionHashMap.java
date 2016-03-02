import java.util.HashMap;

class CellCollectionHashMap extends CellCollection {
    HashMap<String, SpreadsheetCell> cells;

    public CellCollectionHashMap() {
        cells = new HashMap<String,SpreadsheetCell>();
    }

    public SpreadsheetCell getCell(int rowIndex, int columnIndex) {
        return cells.get(CellInfoUtils.getCellNumericReference(rowIndex, columnIndex));
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
    }
}