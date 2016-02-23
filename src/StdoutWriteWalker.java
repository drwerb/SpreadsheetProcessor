public class StdoutWriteWalker extends SpreadsheetRowByRowCellWalker {
    public StdoutWriteWalker(Spreadsheet targetSpreadsheet) {
        super(targetSpreadsheet);
    }

    public void processCell(int rowIndex, int columnIndex) {
        System.out.print(spreadsheet.getCellComputedData(rowIndex, columnIndex) + "\t");
    }

    public void onNextRow() {
        System.out.println("");
    }
}