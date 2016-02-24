import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CellExpressionReference extends CellExpression {
    protected String reference;

    public CellExpressionReference(String cellReference) {
        reference = cellReference;
    }

    private int getReferencedRowIndex() {
    Pattern endingDigitsPattern = Pattern.compile("\\d+");
        Matcher digitsMatcher = endingDigitsPattern.matcher(reference);

        digitsMatcher.find();

        return Integer.parseInt(digitsMatcher.group()) - 1;
    }

    private int getReferencedColumnIndex() {
        int columnIndex = 0;
        Pattern beginningLettersPattern = Pattern.compile("^[A-Z]+");
        Matcher lettersMatcher = beginningLettersPattern.matcher(reference.toUpperCase());
        lettersMatcher.find();
        String columnReference = lettersMatcher.group();
        int localeBigAIndex = Character.getNumericValue('A');
        int positionMultiplier = 1;
        int positionBase = Character.getNumericValue('Z') - localeBigAIndex + 1;
        int lastCharIndex = columnReference.length() - 1;
        int localeColCharIndex;

        for (int i = lastCharIndex; i >= 0; i--) {
            localeColCharIndex = Character.getNumericValue(columnReference.charAt(i));

            if (i != lastCharIndex) {
                positionMultiplier *= positionBase;
            }

            columnIndex += (localeColCharIndex - localeBigAIndex) * positionMultiplier;
        }

        return columnIndex;
    }

    public double evaluate(Spreadsheet context) {
        String data = context.getCellComputedData(getReferencedRowIndex(), getReferencedColumnIndex());
        return Double.parseDouble(data);
    }
}