import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ExpressionEvaluatorAlgebraicSimple {
    private static final char TOKEN_SYMBOL_NUMBER = 'N';
    private static final char TOKEN_SYMBOL_REF = 'R';
    private static final char TOKEN_SYMBOL_SUM = '+';
    private static final char TOKEN_SYMBOL_SUB = '-';
    private static final char TOKEN_SYMBOL_MUL = '*';
    private static final char TOKEN_SYMBOL_DIV = '/';
    private static final char TOKEN_SYMBOL_NULL = '0';

    private CellExpression ast = null;
    private CellDependancyManager dependancyManager;
    private String expressionHolderReference;

    public ExpressionEvaluatorAlgebraicSimple(CellDependancyManager depMngr, String expressionHolderRef) {
        dependancyManager = depMngr;
        expressionHolderReference = expressionHolderRef;
    }

    public double evaluate(Spreadsheet spreadsheet) {
        dependancyManager.evaluateDependenciesBy(expressionHolderReference, spreadsheet);
        return ast.evaluate(spreadsheet);
    }

    public void process(String expr) throws Exception {
        ArrayList<ExpressionToken> tokens = splitTokens(expr);
        buildAst(tokens);
        registerDependencies(tokens);
    }

    @SuppressWarnings("unchecked")
    public void buildAst(ArrayList<ExpressionToken> tokens) throws Exception {
        CellExpression tmpCellExprOperand;
        ExpressionToken token;

        ArrayList<ExpressionToken> tokensCopy = (ArrayList<ExpressionToken>) tokens.clone();

        while (!tokensCopy.isEmpty()) {
            token = tokensCopy.remove(0);

            switch (token.symbol) {
                case TOKEN_SYMBOL_DIV:
                    tmpCellExprOperand = createEpressionOperand(tokensCopy.remove(0));
                    ast = new CellExpressionDiv(ast, tmpCellExprOperand);
                    break;
                case TOKEN_SYMBOL_SUM:
                    tmpCellExprOperand = createEpressionOperand(tokensCopy.remove(0));
                    ast = new CellExpressionSum(ast, tmpCellExprOperand);
                    break;
                case TOKEN_SYMBOL_SUB:
                    tmpCellExprOperand = createEpressionOperand(tokensCopy.remove(0));
                    ast = new CellExpressionSub(ast, tmpCellExprOperand);
                    break;
                case TOKEN_SYMBOL_MUL:
                    tmpCellExprOperand = createEpressionOperand(tokensCopy.remove(0));
                    ast = new CellExpressionMul(ast, tmpCellExprOperand);
                    break;
                default:
                    ast = createEpressionOperand(token);
                    break;
            }
        }
    }

    public void registerDependencies(ArrayList<ExpressionToken> tokens) {
        for (ExpressionToken token : tokens) {
            if (token.symbol == TOKEN_SYMBOL_REF) {
                String referenceNumericForm = CellInfoUtils.convertReferenceToNumericForm(token.content);
                dependancyManager.addReferenceTranslation(referenceNumericForm, token.content);
                dependancyManager.registerDependancy(expressionHolderReference, referenceNumericForm);
            }
        }
    }

    private CellExpression createEpressionOperand(ExpressionToken token) throws Exception {
        CellExpression operand;

        switch (token.symbol) {
            case TOKEN_SYMBOL_NUMBER:
                operand = (CellExpression) new CellExpressionConstant(Double.parseDouble(token.content) * (token.negative ? -1 : 1));
                break;
            case TOKEN_SYMBOL_REF:
                operand = (CellExpression) new CellExpressionReference(token.content, token.negative);
                break;
            default:
                throw(new Exception("Unexpected token symbol"));
        }

        return operand;
    }

    private ArrayList<ExpressionToken> splitTokens(String expr) throws Exception {
        int lastExprIndex = expr.length() - 1;
        ArrayList<ExpressionToken> tokens = new ArrayList<ExpressionToken>();

        TokenParseState tokenParseState = new TokenParseState();

        for (int i = 0; i<=lastExprIndex; i++) {
            tokenParseState.appendChar(expr.charAt(i));

            while (tokenParseState.areTokensReady()) {
                tokens.add(tokenParseState.popToken());
            }            
        }

        tokens.add(tokenParseState.popToken());

        return tokens;
    }

    class TokenParseState {
        StringBuilder accum;
        boolean isOperandExpectedPositiveOnly = false;
        boolean doOperandNegation = false;
        boolean isAccumFlushed = false;
        char operationChar;

        public TokenParseState() {
            isAccumFlushed = true;
            isOperandExpectedPositiveOnly = false;
            doOperandNegation = false;
            operationChar = TOKEN_SYMBOL_NULL;
        }

        public void appendChar(char currChar) throws Exception {
            if (isOperation(currChar)) {
                processOperation(currChar);
            }
            else {
                processChar(currChar);
            }
        }

        public void processChar(char currChar) throws Exception {
            if (isAccumFlushed) {
                accum = new StringBuilder("");
                isAccumFlushed = false;
            }

            accum.append(currChar);
        }

        public void processOperation(char currChar) throws Exception {
            if (currChar == '-' && isOperandExpectedPositiveOnly) {
                throw new Exception("unexpected 'minus'");
            }

            if (currChar == '-' && isAccumFlushed) {
                doOperandNegation = true;
                isOperandExpectedPositiveOnly = true;
            }
            else {
                if (operationChar != TOKEN_SYMBOL_NULL) {
                    throw new Exception("unexpected operator");
                }

                operationChar = currChar;
                isOperandExpectedPositiveOnly = (currChar == '+' || currChar == '-');
            }
        }

        private boolean areTokensReady() {
            return operationChar != TOKEN_SYMBOL_NULL;
        }

        public ExpressionToken popToken() throws Exception {
            if (!isAccumFlushed) {
                return popOperandToken();
            }

            return popOperationToken();
        }

        public ExpressionToken popOperandToken() throws Exception {
            String accumulatedOperand = accum.toString();
            char tokenSymbol;

            if (isDigit(accumulatedOperand)) {
                tokenSymbol = TOKEN_SYMBOL_NUMBER;
            }
            else if (isReference(accumulatedOperand)) {
                tokenSymbol = TOKEN_SYMBOL_REF;
            }
            else {
                throw new Exception("unexpected operand: " + accumulatedOperand);
            }

            ExpressionToken operandToken = new ExpressionToken(tokenSymbol, accumulatedOperand, doOperandNegation);

            isAccumFlushed = true;
            doOperandNegation = false;

            return operandToken;
        }

        public ExpressionToken popOperationToken() throws Exception {
            if (operationChar == TOKEN_SYMBOL_NULL) {
                throw new Exception("incorrect exprression");
            }

            ExpressionToken opToken = new ExpressionToken(operationChar, String.valueOf(operationChar), false);

            operationChar = TOKEN_SYMBOL_NULL;

            return opToken;
        }

        private boolean isDigit(String s) {
            Pattern digitPattern = Pattern.compile("\\A\\d+(?:\\.\\d+)?\\Z");
            return digitPattern.matcher(s).matches();
        }

        private boolean isReference(String s) {
            Pattern refPattern = Pattern.compile("\\A[A-Z]+\\d+\\Z");
            return refPattern.matcher(s).matches();
        }

        private boolean isOperation(char c) {
            return c == '+' || c == '-' || c == '*' || c == '/';
        }
    }

    class ExpressionToken {
        public char symbol;
        public String content;
        public boolean negative;

        public ExpressionToken(char tokenSymbol, String tokenContent, boolean doNegation) {
            symbol = tokenSymbol;
            content = tokenContent;
            negative = doNegation;
        }
    }
}