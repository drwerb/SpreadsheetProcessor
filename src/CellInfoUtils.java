import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CellInfoUtils {
    public static int getReferencedRowIndex(String reference) {
        Pattern endingDigitsPattern = Pattern.compile("\\d+");
        Matcher digitsMatcher = endingDigitsPattern.matcher(reference);

        digitsMatcher.find();

        return Integer.parseInt(digitsMatcher.group()) - 1;
    }

    public static int getReferencedColumnIndex(String reference) {
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

    public static String convertReferenceToNumericForm(String reference) {
        int rowIndex = CellInfoUtils.getReferencedRowIndex(),
        int columnIndex = CellInfoUtils.getReferencedColumnIndex()

        return getCellNumericReference(rowIndex, columnIndex);
    }

    public static String getCellNumericReference(int rowIndex, int columnIndex) {
        return Integer.toString(rowIndex) + "_" + Integer.toString(columnIndex);
    }

    public static int[] converNumericFormToRowCol(String numericReference) {
        String[] splittedRowCol = numericReference.split("_");
        int [2] result;

        result[0] = Integer.parseInt(splittedRowCol[0]);
        result[1] = Integer.parseInt(splittedRowCol[1]);

        return result;
    }
}