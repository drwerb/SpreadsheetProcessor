import java.util.HashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

public class CellProcessor4Types extends CellProcessor {

    private HashMap<String,DataTypeProcessor> dataTypeProcessorMap;
    private DecimalFormat floatFormat;
    private DecimalFormat integerFormat;

    public CellProcessor4Types(Spreadsheet tragetSpreadsheet) {
        super(tragetSpreadsheet);
        DecimalFormatSymbols dfSymbols = new DecimalFormatSymbols(Locale.getDefault());
        dfSymbols.setDecimalSeparator('.');
        floatFormat = new DecimalFormat("0.0#", dfSymbols);
        integerFormat = new DecimalFormat("0");
    }

    protected void initDataTypeProcessorMap() {
        dataTypeProcessorMap = new HashMap<String,DataTypeProcessor>();
        dataTypeProcessorMap.put("Null", (DataTypeProcessor)new FourTypesNullProcessor(this));
        dataTypeProcessorMap.put("Text", (DataTypeProcessor)new FourTypesTextProcessor(this));
        dataTypeProcessorMap.put("Number", (DataTypeProcessor)new FourTypesNumberProcessor(this));
        dataTypeProcessorMap.put("Expression", (DataTypeProcessor)new FourTypesExpressionProcessor(this));
    }

    protected void initCellDependancyManager() {
        cellDependancyManager = new CellDependancyManager();
    }

    public void computeDependantData() {
	    cellDependancyManager.makeTopologicalSort();
	    cellDependancyManager.setCycledCells(spreadsheet);
    }

    public DataTypeProcessor selectDataType(SpreadsheetCell cell) {
        char firstChar;
        DataTypeProcessor selectedProcessor;

        if (cell == null) {
            return dataTypeProcessorMap.get("Null");
        }

        firstChar = cell.getData().charAt(0);

        switch (firstChar) {
            case '\'': selectedProcessor = dataTypeProcessorMap.get("Text");
                       break;
            case '=':  selectedProcessor = dataTypeProcessorMap.get("Expression");
                       break;
            default:   selectedProcessor = dataTypeProcessorMap.get("Number");
                       break;
        }

        return selectedProcessor;
    }
    
    private class FourTypesNullProcessor extends DataTypeProcessor {
        public FourTypesNullProcessor(CellProcessor cellProcessor) {
            super((CellProcessor) cellProcessor);
        }

        public void processCellData(SpreadsheetCell cell, int rowIndex, int columnIndex) {
            cell.setMetadata((CellMetadata)new CellMetadataNull());
        }

        public String getCellComputedData(SpreadsheetCell cell) {
            return "";
        }
    }

    private class FourTypesTextProcessor extends DataTypeProcessor {
        public FourTypesTextProcessor(CellProcessor cellProcessor) {
            super((CellProcessor) cellProcessor);
        }

        public void processCellData(SpreadsheetCell cell, int rowIndex, int columnIndex) {
            cell.setMetadata((CellMetadata)new CellMetadataText());
        }

        public String getCellComputedData(SpreadsheetCell cell) {
            return cell.getData().substring(1);
        }
    }

    private class FourTypesNumberProcessor extends DataTypeProcessor {
        public FourTypesNumberProcessor(CellProcessor cellProcessor) {
            super((CellProcessor) cellProcessor);
        }

        public void processCellData(SpreadsheetCell cell, int rowIndex, int columnIndex) {
            cell.setMetadata((CellMetadata)new CellMetadataNumber());
            if (!isValidData(cell.getData())) {
                cell.getMetadata().setErrorText("invalid number format");
            }
        }

        public String getCellComputedData(SpreadsheetCell cell) {
            CellMetadata meta = cell.getMetadata();
            
            if (meta.hasError()) {
                return "#" + meta.getErrorText();
            }

            return cell.getData();
        }

        private boolean isValidData(String data) {
            Pattern digitPattern = Pattern.compile("\\A-?\\d+(?:\\.\\d+)?\\Z");

            return digitPattern.matcher(data).matches();
        }
    }

    private class FourTypesExpressionProcessor extends DataTypeProcessor {
        public FourTypesExpressionProcessor(CellProcessor cp) {
            super((CellProcessor) cp);
        }

        public void processCellData(SpreadsheetCell cell, int rowIndex, int columnIndex) {
            CellMetadataExpression cellMeta = new CellMetadataExpression();
            cellMeta.numericReference = CellInfoUtils.getCellNumericReference(rowIndex, columnIndex);
            cellMeta.evaluator = new ExpressionEvaluatorAlgebraicSimple(cellProcessor.getCellDependancyManager(), cellMeta.numericReference);
            try {
                cellMeta.evaluator.process(cell.getData().substring(1));
            }
            catch (Exception ex) {
                cellMeta.setErrorText(ex.getMessage());
            }
            cell.setMetadata((CellMetadata)cellMeta);
        }

        public String getCellComputedData(SpreadsheetCell cell) {
            CellMetadataExpression meta = (CellMetadataExpression)cell.getMetadata();

            CellDependancyManager cellDependancyManager = cellProcessor.getCellDependancyManager();

            if (!cellDependancyManager.isCellEvaluatedByReference(meta.numericReference)) {
                try {
                    meta.evaluatedValue = meta.evaluator.evaluate((Spreadsheet)spreadsheet);
                }
                catch (NumberFormatException e) {
                    meta.setErrorText(e.getMessage());
                }
                finally {
                    cellDependancyManager.markReferenceAsEvaluated(meta.numericReference);
                }
            }

            if (meta.hasError()) {
                return "#" + meta.getErrorText();
            }
            
            if (meta.evaluatedValue % 1 == 0) {
                return integerFormat.format(meta.evaluatedValue);
            }
            else {
                return floatFormat.format(meta.evaluatedValue);
            }
        }

        private class CellMetadataExpression extends CellMetadata {
            public ExpressionEvaluatorAlgebraicSimple evaluator;
            public double evaluatedValue;
            public String numericReference;
        }
    }
}