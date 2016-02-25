import java.util.HashMap;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class CellProcessor4Types extends CellProcessor {

    private HashMap<String,DataTypeProcessor> dataTypeProcessorMap;

    public CellProcessor4Types(Spreadsheet tragetSpreadsheet) {
        super(tragetSpreadsheet);
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
            NumberFormat formatter = NumberFormat.getInstance();
            ParsePosition pos = new ParsePosition(0);
            formatter.parse(data, pos);
            return data.length() == pos.getIndex();
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

            double evaluatedValue;

            if (meta.hasError()) {
                return "#" + meta.getErrorText();
            }

            CellDependancyManager cellDependancyManager = cellProcessor.getCellDependancyManager();

            if (cellDependancyManager.isCellEvaluatedByReference(meta.numericReference)) {
                evaluatedValue = meta.evaluatedValue;
            }
            else {
                evaluatedValue = meta.evaluator.evaluate((Spreadsheet)spreadsheet);
                meta.evaluatedValue = evaluatedValue;
                cellDependancyManager.markReferenceAsEvaluated(meta.numericReference);
            }

            return String.valueOf(evaluatedValue);
        }

        private class CellMetadataExpression extends CellMetadata {
            public ExpressionEvaluatorAlgebraicSimple evaluator;
            public double evaluatedValue;
            public String numericReference;
        }
    }
}