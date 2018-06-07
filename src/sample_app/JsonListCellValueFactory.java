package sample_app;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.json.JSONObject;

public class JsonListCellValueFactory implements Callback<ListView<String>,ListCell<String>> {

    private final String property;

    public JsonListCellValueFactory(final String property) {
        this.property = property;
    }

    @Override
    public ListCell<String> call(ListView<String> param) {
        return new ListItem(property);
    }

    static class ListItem extends ListCell<String> {
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
            if (item != null) {
                JSONObject jsonObject = new JSONObject(item);
                setText(jsonObject.get(getProperty()).toString());
            }
        }
    }
}
