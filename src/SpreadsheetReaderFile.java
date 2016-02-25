import java.io.*;
import java.util.Scanner;

class SpreadsheetReaderFile implements SpreadsheetReader {
    // TODO: delegate input format to separate class
    private static String cellDelimeter = "\t";

    public void readAndFillSpreadsheet(Spreadsheet spreadsheet) {
	try (Scanner scanner = new Scanner(new File("src/03_from_assignment_test.table")).useDelimiter("\n")) {
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
	catch (FileNotFoundException ex) {
	    System.err.println("Spreadsheet file not found");
	}
    }
}