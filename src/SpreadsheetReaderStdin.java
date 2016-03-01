import java.io.*;
import java.util.Scanner;

class SpreadsheetReaderStdin implements SpreadsheetReader {
    private static String cellDelimeter = "\t";

    public void readAndFillSpreadsheet(Spreadsheet spreadsheet) {
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");
        int rowIndex = -1;
        int columnIndex = -1;

        while (scanner.hasNext()) {
            String rawRow = scanner.next();
            String[] rawRowCells = rawRow.split(cellDelimeter);

            rowIndex++;
            columnIndex = -1;

            for (String rawCellData : rawRowCells) {
                columnIndex++;
                spreadsheet.setCellRawData(rowIndex, columnIndex, rawCellData);
            }
        }
    }
}