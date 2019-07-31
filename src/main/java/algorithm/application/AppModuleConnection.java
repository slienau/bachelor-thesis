package algorithm.application;

public class AppModuleConnection {
    private final AppModule source; // sending module
    private final AppModule destination; // receiving module
    private final double dataPerMessage; // kb

    public AppModuleConnection(AppModule source, AppModule destination, double dataPerMessage) {
        this.source = source;
        this.destination = destination;
        this.dataPerMessage = dataPerMessage;
    }

    public AppModule getSource() {
        return source;
    }

    public AppModule getDestination() {
        return destination;
    }

    public double getDataPerMessage() {
        return dataPerMessage;
    }
}
