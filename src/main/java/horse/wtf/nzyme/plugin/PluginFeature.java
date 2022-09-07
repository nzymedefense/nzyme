package horse.wtf.nzyme.plugin;

public class PluginFeature<T> {

    private final T feature;

    public PluginFeature(T feature) {
        this.feature = feature;
    }

    public boolean available() {
        return false;
    }

    public T get() {
        return feature;
    }

}
