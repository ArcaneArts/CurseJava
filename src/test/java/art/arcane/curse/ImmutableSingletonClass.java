package art.arcane.curse;

import lombok.Getter;

public class ImmutableSingletonClass {
    private static final ImmutableSingletonClass INSTANCE = new ImmutableSingletonClass("THE ONLY KEY");

    @Getter
    private final String key;

    private ImmutableSingletonClass(String key) {
        if(INSTANCE != null) {
            throw new RuntimeException("Cannot create a new instance of this class");
        }

        this.key = key;
    }

    public static ImmutableSingletonClass getInstance() {
        return INSTANCE;
    }
}
