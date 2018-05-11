package sample;

public class SampleController extends AbstractController{

    @Override
    String getControllerName() {
        return this.getClass().getName();
    }

}
