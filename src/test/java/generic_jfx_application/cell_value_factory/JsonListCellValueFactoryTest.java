package generic_jfx_application.cell_value_factory;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;


public class JsonListCellValueFactoryTest {

    private static final String PROPERTY = "name";

    private static final String JSON =
            "{\r\n" +
            "  name: \"susi\", \r\n" +
            "  surname: \"sample\"\r\n" +
            "}";

    private ListCell<String> listCell;
    private JsonListCellValueFactory.ListItem listItem;

    @BeforeMethod
    public void setUp() {
        JsonListCellValueFactory sut = new JsonListCellValueFactory(PROPERTY);
        ListView listView = Mockito.mock(ListView.class);
        listCell = sut.call(listView);
        listItem = (JsonListCellValueFactory.ListItem) listCell;
    }

    @Test
    public void setPropertyTest() {
        assertEquals(listItem.getProperty(), PROPERTY);
    }

    @Test
    public void updateNonemptyTest() {
        listItem.updateItem(JSON, false);

        assertEquals(listCell.getText(), "susi");
    }

    @Test
    public void updateEmptyTest() {
        listItem.updateItem("", true);

        assertNull(listCell.getText());
    }
}
