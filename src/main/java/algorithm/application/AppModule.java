package algorithm.application;

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
    public abstract String toString();
}
