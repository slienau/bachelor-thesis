package de.tuberlin.aot.thesis.slienau.scheduler.application;

public class AppHardwareModule extends AppModule {

    public AppHardwareModule(String id, String outputType) {
        super(id, null, outputType);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public String toString() {
        return "AppHardwareModule{" +
                "id='" + super.getId() + '\'' +
                '}';
    }
}
