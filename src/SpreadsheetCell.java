class SpreadsheetCell {
    private String data;
    private CellMetadata meta;

    public SpreadsheetCell(String cellData) {
        setData(cellData);
    }

    public String getData() {
        return data;
    }

    public void setData(String cellData) {
        data = cellData;
    }

    public void setMetadata(CellMetadata cellMetadata) {
        meta = cellMetadata;
    }

    public CellMetadata getMetadata() {
        return meta;
    }
}
