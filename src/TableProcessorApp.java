class TableProcessorApp {
    public static void main(String[] args) {
        Spreadsheet spreadsheet = new Spreadsheet4Types();

        new SpreadsheetReaderFile("src/14_expression_with_negative_ref.table").readAndFillSpreadsheet(spreadsheet);

        spreadsheet.processData();

        SpreadsheetWriterStdout.writeSpreadsheetComputedData(spreadsheet);
    }
}
