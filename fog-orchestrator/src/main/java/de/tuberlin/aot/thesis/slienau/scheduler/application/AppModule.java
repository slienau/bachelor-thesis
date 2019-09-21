package de.tuberlin.aot.thesis.slienau.scheduler.application;

import java.util.Objects;

public abstract class AppModule {
    private final String id;
    private final String inputType;
    private final String outputType;

    public AppModule(String id, String inputType, String outputType) {
        this.id = id;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public String getId() {
        return id;
    }

    public String getInputType() {
        return inputType;
    }

    public String getOutputType() {
        return outputType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppModule appModule = (AppModule) o;
        return id.equals(appModule.id) &&
                Objects.equals(inputType, appModule.inputType) &&
                Objects.equals(outputType, appModule.outputType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inputType, outputType);
    }

    @Override
    public abstract String toString();
}
