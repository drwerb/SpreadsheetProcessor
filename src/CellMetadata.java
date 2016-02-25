public abstract class CellMetadata {
    private String errorText;

    public int rowIndex;
    public int columnIndex;

    public boolean hasError() {
        return errorText != null;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String error) {
        errorText = error;
    }
}