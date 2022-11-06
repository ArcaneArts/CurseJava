package art.arcane.curse.model;

public class CursedProperty {
    private final CursedField field;
    private final CursedMethod getter;
    private final CursedMethod setter;

    public CursedProperty(CursedField field, CursedMethod getter, CursedMethod setter) {
        this.field = field;
        this.getter = getter;
        this.setter = setter;
    }

    public CursedProperty(CursedMethod getter, CursedMethod setter) {
        this.getter = getter;
        this.setter = setter;
        this.field = null;
    }

    public CursedProperty(CursedField field) {
        this.getter = null;
        this.setter = null;
        this.field = field;
    }

    public <T> T get() {
        if (field != null) {
            return field.get();
        }

        if (getter != null) {
            return getter.invoke();
        }

        return null;
    }

    public void set(Object o) {
        if (setter != null) {
            setter.invoke(o);
            return;
        }

        if (field != null) {
            field.set(o);
        }
    }
}
