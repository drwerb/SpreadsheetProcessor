public abstract class SpreadsheetRowByRowCellWalker {
    protected Spreadsheet spreadsheet;

    public SpreadsheetRowByRowCellWalker(Spreadsheet targetSpreadsheet) {
        spreadsheet = targetSpreadsheet;
    }

    public abstract void processCell(int rowIndex, int columnIndex);
    public void onNextRow() {};

    public void run() {
        int rowsCount = spreadsheet.getRowsCount();
        int columnsCount = spreadsheet.getColumnsCount();

        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {

            for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
                processCell(rowIndex, columnIndex);
            }

            onNextRow();
        }
    }
}
