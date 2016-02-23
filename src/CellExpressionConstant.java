public class CellExpressionConstant extends CellExpression {
    private double value;

    public CellExpressionConstant(double val) {
        value = val;
    }

    public double evaluate(Spreadsheet context) {
        return value;
    }
}