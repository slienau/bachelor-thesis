package algorithm.application;

public class AppModule {
    private final String id;
    private final int requiredRam; // MB
    private final double requiredStorage; // GB

    public AppModule(String id, int requiredRam, double requiredStorage) {
        this.id = id;
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;

    }

    public String getId() {
        return id;
    }

    public int getRequiredRam() {
        return requiredRam;
    }

    public double getRequiredStorage() {
        return requiredStorage;
    }

    @Override
    public String toString() {
        return "AppModule{" +
                "id='" + id + '\'' +
                ", requiredRam=" + requiredRam +
                ", requiredStorage=" + requiredStorage +
                '}';
    }
}
