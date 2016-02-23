public class CellExpressionSub extends CellExpression {
    CellExpression operand1;
    CellExpression operand2;

    public CellExpressionSub(CellExpression op1, CellExpression op2) {
        operand1 = op1;
        operand2 = op2;
    }

    public double evaluate(Spreadsheet context) {
        return operand1.evaluate(context) - operand2.evaluate(context);
    }
}