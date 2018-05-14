package sample_app.sample;

import sample_app.events.AbstractController;

public class SampleController extends AbstractController {
    @Override
    public String getControllerName() {
        return this.getClass().getName();
    }
}
