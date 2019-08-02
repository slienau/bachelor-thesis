package algorithm.application;

public class AppMessage {
    private final String content;
    private final AppModule source; // sending module
    private final AppModule destination; // receiving module
    private final double dataPerMessage; // kb

    AppMessage(String content, AppModule source, AppModule destination, double dataPerMessage) {
        this.content = content;
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

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "AppMessage{" +
                "content='" + content + '\'' +
                ", source=" + source.getId() +
                ", destination=" + destination.getId() +
                ", dataPerMessage=" + dataPerMessage +
                '}';
    }
}
