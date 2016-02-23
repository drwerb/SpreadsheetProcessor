import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CellExpressionReference extends CellExpression {
    String reference;

    public CellExpressionReference(String cellReference) {
        reference = cellReference;
    }

    private int getReferencedRowIndex() {
        Pattern endingDigitsPattern = Pattern.compile("\\d+$");
        Matcher matchedDigits = endingDigitsPattern.matcher(reference);

        return Integer.parseInt(matchedDigits.group());
    }

    private int getReferencedColumnIndex() {
        int columnIndex = 0;
        Pattern beginningLettersPattern = Pattern.compile("^[A-Z]+");
        Matcher matchedLetters = beginningLettersPattern.matcher(reference.toUpperCase());
        String columnReference = matchedLetters.group();
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