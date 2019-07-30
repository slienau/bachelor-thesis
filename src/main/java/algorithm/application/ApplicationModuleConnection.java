package algorithm.application;

public class ApplicationModuleConnection {
    private final ApplicationModule source; // sending module
    private final ApplicationModule destination; // receiving module
    private final double dataPerMessage; // kb

    public ApplicationModuleConnection(ApplicationModule source, ApplicationModule destination, double dataPerMessage) {
        this.source = source;
        this.destination = destination;
        this.dataPerMessage = dataPerMessage;
    }

    public ApplicationModule getSource() {
        return source;
    }

    public ApplicationModule getDestination() {
        return destination;
    }

    public double getDataPerMessage() {
        return dataPerMessage;
    }
}
