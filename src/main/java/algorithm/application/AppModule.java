package algorithm.application;

public abstract class AppModule {
    private final String id;

    public AppModule(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public abstract String toString();
}
