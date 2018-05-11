package sample;

import javafx.event.ActionEvent;

public class SampleController extends AbstractController{

    public void sendEvent(ActionEvent actionEvent) {
        super.observable.notifyListeners(actionEvent);
    }
}
