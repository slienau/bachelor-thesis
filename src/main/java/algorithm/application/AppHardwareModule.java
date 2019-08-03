package algorithm.application;

public class AppHardwareModule extends AppModule {

    public AppHardwareModule(String id) {
        super(id);
    }

    @Override
    public String toString() {
        return "AppHardwareModule{" +
                "id='" + super.getId() + '\'' +
                '}';
    }
}
