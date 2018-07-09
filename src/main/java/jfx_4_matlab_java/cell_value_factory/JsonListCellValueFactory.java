package jfx_4_matlab_java.cell_value_factory;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.json.JSONObject;

/**
 * Cell value factory which allows filling a list view from MATLAB.
 * Because no custom MATLAB objects can be sent to java json strings
 * are transferred.
 */
public class JsonListCellValueFactory implements Callback<ListView<String>,ListCell<String>> {

    /**
     * The property of the json to be shown.
     */
    private final String property;

    /**
     * @param property  The property of the json to be shown.
     */
    public JsonListCellValueFactory(final String property) {
        this.property = property;
    }

    @Override
    public ListCell<String> call(ListView<String> param) {
        return new ListItem(property);
    }

    public static class ListItem extends ListCell<String> {
        final String property;

        public ListItem(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                JSONObject jsonObject = new JSONObject(item);
                setText(jsonObject.get(getProperty()).toString());
            }
        }
    }
}
