package sample_app;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONObject;

public class JsonCellValueFactory extends PropertyValueFactory<String, String> {

    /**
     * Creates a default PropertyValueFactory to extract the value from a given
     * TableView row item reflectively, using the given property name.
     *
     * @param property The name of the property with which to attempt to
     *                 reflectively extract a corresponding value for in a given object.
     */
    public JsonCellValueFactory(String property) {
        super(property);
    }

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<String, String> param) {
        JSONObject jsonObject = new JSONObject(param.getValue());
        return new ReadOnlyStringWrapper((String) jsonObject.get(getProperty()));
    }
}
