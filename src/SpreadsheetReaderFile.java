import java.io.*;
import java.util.Scanner;

class SpreadsheetReaderFile implements SpreadsheetReader {
    protected String cellDelimeter = "\t";
    protected String filePath;

    public SpreadsheetReaderFile(String path) {
        filePath = path;
    }

    public void readAndFillSpreadsheet(Spreadsheet spreadsheet) {
    	try (Scanner scanner = new Scanner(new File(filePath)).useDelimiter("\n")) {
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
    	    System.err.println("Spreadsheet file not found.");
    	}
    }
}
