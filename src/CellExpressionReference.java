public class CellExpressionReference extends CellExpression {
    protected String reference;
    protected int multiplier;

    public CellExpressionReference(String cellReference, boolean doNegation) {
        reference = cellReference;
        multiplier = doNegation ? -1 : 1;
    }

    public double evaluate(Spreadsheet context) {
        String data = context.getCellComputedData(
                CellInfoUtils.getReferencedRowIndex(reference),
                CellInfoUtils.getReferencedColumnIndex(reference)
            );

        try {
            return Double.parseDouble(data) * multiplier;
        }
        catch (NumberFormatException e) {
            throw new NumberFormatException("numeric format expected: " + reference);
        }
    }
}