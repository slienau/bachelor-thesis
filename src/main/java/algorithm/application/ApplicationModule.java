package algorithm.application;

public class ApplicationModule {
    private final String id;
    private final int requiredRam; // MB
    private final double requiredStorage; // GB

    public ApplicationModule(String id, int requiredRam, double requiredStorage) {
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
        return "ApplicationModule{" +
                "id='" + id + '\'' +
                ", requiredRam=" + requiredRam +
                ", requiredStorage=" + requiredStorage +
                '}';
    }
}
