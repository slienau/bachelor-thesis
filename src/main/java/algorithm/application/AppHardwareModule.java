package algorithm.application;

public class AppHardwareModule extends AppModule {

    public AppHardwareModule(String id, String outputType) {
        super(id, null, outputType);
    }

    @Override
    public String toString() {
        return "AppHardwareModule{" +
                "id='" + super.getId() + '\'' +
                '}';
    }
}
