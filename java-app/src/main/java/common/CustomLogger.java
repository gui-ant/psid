package common;

public abstract class CustomLogger<C> {
    private C component;

    public CustomLogger() {
        this.component = getLogComponent();
    }

    public void write(String text) {
        if (getLogComponent() != null)
            writeToComponent(text);
    }

    protected abstract C getLogComponent();

    protected abstract void writeToComponent(String text);
}
