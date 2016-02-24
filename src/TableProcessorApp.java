class TableProcessorApp {
    public static void main(String[] args) {
        Spreadsheet spreadsheet = new Spreadsheet4Types();

        new SpreadsheetReaderFile().readAndFillSpreadsheet(spreadsheet);

        spreadsheet.processData();

        SpreadsheetWriterStdout.writeSpreadsheetComputedData(spreadsheet);
    }
}
