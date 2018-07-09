package jfx_4_matlab_java.cell_value_factory;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JsonTableCellvalueFactoryTest {

    private static final String PROPERTY = "surname";

    private static final String JSON =
            "{\r\n" +
            "  name: \"susi\", \r\n" +
            "  surname: \"sample\"\r\n" +
            "}";


    private JsonTableCellValueFactory sut;
    private TableColumn.CellDataFeatures<String, String> param;

    @BeforeTest
    public void setUp() {
        sut = new JsonTableCellValueFactory(PROPERTY);
        param =
                Mockito.mock(TableColumn.CellDataFeatures.class);
        Mockito.when(param.getValue()).thenReturn(JSON);
    }

    @Test
    public void getPropertyTest() {
        assertEquals(sut.getProperty(), PROPERTY);
    }

    @Test
    public void callTest() {
        ObservableValue<String> value = sut.call(param);

        assertEquals(value.getValue(),"sample");
    }
}
