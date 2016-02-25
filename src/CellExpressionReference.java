import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CellExpressionReference extends CellExpression {
    protected String reference;

    public CellExpressionReference(String cellReference) {
        reference = cellReference;
    }

    public double evaluate(Spreadsheet context) {
        String data = context.getCellComputedData(
                CellInfoUtils.getReferencedRowIndex(reference),
                CellInfoUtils.getReferencedColumnIndex(reference)
            );

        return Double.parseDouble(data);
    }
}