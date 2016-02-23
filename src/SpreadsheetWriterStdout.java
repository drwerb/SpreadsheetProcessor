public class SpreadsheetWriterStdout extends SpreadsheetWriter {
    public static void writeSpreadsheetComputedData(Spreadsheet spreadsheet) {
        StdoutWriteWalker walker = new StdoutWriteWalker((Spreadsheet)spreadsheet);
        walker.run();
    }
}