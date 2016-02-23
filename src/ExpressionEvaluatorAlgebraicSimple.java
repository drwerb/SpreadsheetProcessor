import java.lang.StringBuilder;
import java.util.ArrayList;

public class ExpressionEvaluatorAlgebraicSimple {
    private static final char TOKEN_SYMBOL_NUMBER = 'N';
    private static final char TOKEN_SYMBOL_REF = 'R';
    private static final char TOKEN_SYMBOL_SUM = '+';
    private static final char TOKEN_SYMBOL_SUB = '-';
    private static final char TOKEN_SYMBOL_MUL = '*';
    private static final char TOKEN_SYMBOL_DIV = '/';

    private static final int LOCALE_BIG_A_INDEX = Character.getNumericValue('A');
    private static final int LOCALE_BIG_Z_INDEX = Character.getNumericValue('Z');

    private static final int LOCALE_NUMBER_ZERO_INDEX = Character.getNumericValue('0');
    private static final int LOCALE_NUMBER_NINE_INDEX = Character.getNumericValue('9');

    private CellExpression ast = null;

    public ExpressionEvaluatorAlgebraicSimple() {}

    public double evaluate(Spreadsheet spreadsheet) {
        return ast.evaluate(spreadsheet);
    }

    public void process(String expr) throws Exception {
        ArrayList<ExpressionToken> tokens = splitTokens(expr);
        //registerDependencies(tokens);
        buildAst(tokens);
    }

    public void buildAst(ArrayList<ExpressionToken> tokens) throws Exception {
        CellExpression tmpCellExprOperand;
        ExpressionToken token;

        while (!tokens.isEmpty()) {
            token = tokens.remove(0);

            switch (token.symbol) {
                case TOKEN_SYMBOL_DIV:
                    tmpCellExprOperand = createEpressionOperand(tokens.remove(0));
                    ast = new CellExpressionDiv(ast, tmpCellExprOperand);
                    break;
                case TOKEN_SYMBOL_SUM:
                    tmpCellExprOperand = createEpressionOperand(tokens.remove(0));
                    ast = new CellExpressionSum(ast, tmpCellExprOperand);
                    break;
                case TOKEN_SYMBOL_SUB:
                    tmpCellExprOperand = createEpressionOperand(tokens.remove(0));
                    ast = new CellExpressionSub(ast, tmpCellExprOperand);
                    break;
                case TOKEN_SYMBOL_MUL:
                    tmpCellExprOperand = createEpressionOperand(tokens.remove(0));
                    ast = new CellExpressionMul(ast, tmpCellExprOperand);
                    break;
                default:
                    ast = createEpressionOperand(token);
                    break;
            }
        }
    }

    private CellExpression createEpressionOperand(ExpressionToken token) throws Exception {
        CellExpression operand;

        switch (token.symbol) {
            case TOKEN_SYMBOL_NUMBER:
                operand = (CellExpression) new CellExpressionConstant(Double.parseDouble(token.content));
                break;
            case TOKEN_SYMBOL_REF:
                operand = (CellExpression) new CellExpressionReference(token.content);
                break;
            default:
                throw(new Exception("Unexpected token symbol"));
        }

        return operand;
    }

    private ArrayList<ExpressionToken> splitTokens(String expr) throws Exception {
        int lastExprIndex = expr.length() - 1;
        StringBuilder accum = new StringBuilder("");
        char currChar;
        boolean numberState = false;
        boolean refState = false;
        boolean refStateRowPart = false;
        ArrayList<ExpressionToken> tokens = new ArrayList<ExpressionToken>();
        char tokenSymbol;

        for (int i = 0; i<=lastExprIndex; i++) {
            currChar = expr.charAt(i);

            if (isAlpha(currChar)) {
                if (numberState) {
                    throw(new Exception("Number expected"));
                }

                if (refState && !refStateRowPart) {
                    throw(new Exception("Row column incorrect format: number expected"));
                }

                if (!refState) {
                    refState = true;
                    refStateRowPart = true;
                }

                accum.append(currChar);

                if (i != lastExprIndex) continue;
            }

            if (isNumber(currChar)) {
                if (!numberState && !refState) {
                    numberState = true;
                }

                if (refState && refStateRowPart) {
                    refStateRowPart = false;
                }

                accum.append(currChar);

                if (i != lastExprIndex) continue;
            }

            if (accum.length() == 0) {
                throw(new Exception("Operand expected"));
            }

            if (numberState) {
                tokenSymbol = TOKEN_SYMBOL_NUMBER;
            }
            else {
                tokenSymbol = TOKEN_SYMBOL_REF;
            }

            tokens.add(new ExpressionToken(tokenSymbol, accum.toString()));
            accum = new StringBuilder("");
            numberState = false;
            refState = false;

            if (isOperation(currChar)) {
                tokens.add(new ExpressionToken(currChar, String.valueOf(currChar)));
                continue;
            }

            throw(new Exception("Unexpected character: " + currChar));
        }

        return tokens;
    }

    private boolean isAlpha(char c) {
        int ucCharIndex = Character.getNumericValue(Character.toUpperCase(c));

        return ucCharIndex >= LOCALE_BIG_A_INDEX || ucCharIndex <= LOCALE_BIG_Z_INDEX;
    }

    private boolean isNumber(char c) {
        int charIndex = Character.getNumericValue(c);

        return charIndex >= LOCALE_NUMBER_ZERO_INDEX || charIndex <= LOCALE_NUMBER_NINE_INDEX;
    }

    private boolean isOperation(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    class ExpressionToken {
        public char symbol;
        public String content;

        public ExpressionToken(char tokenSymbol, String tokenContent) {
            symbol = tokenSymbol;
            content = tokenContent;
        }
    }
}