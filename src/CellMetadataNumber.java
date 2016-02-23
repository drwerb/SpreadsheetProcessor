import java.text.NumberFormat;
import java.text.ParsePosition;

class CellMetadataNumber extends CellMetadata {
    protected boolean isValidData(String data) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(data, pos);
        return data.length() == pos.getIndex();
    }
}