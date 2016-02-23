public class Spreadsheet4Types extends Spreadsheet {
    protected void initCellProcessor() {
        cellProcessor = (CellProcessor)new CellProcessor4Types(this);
    }

    protected void initCellCollection() {
        cells = (CellCollection)new CellCollectionHashMap();
    }
}