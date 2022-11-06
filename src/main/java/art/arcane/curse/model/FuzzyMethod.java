package art.arcane.curse.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Builder
@Data
public class FuzzyMethod {
    @Builder.Default
    private final boolean staticMethod = false;
    private final Class<?> returns;
    @Singular
    private final List<Class<?>> parameters;
    private final List<String> possibleNames;
}
