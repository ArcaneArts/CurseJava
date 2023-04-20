package art.arcane.curse.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class FuzzyField {
    @Builder.Default
    private final boolean staticField = false;
    private final Class<?> type;
    private final List<String> possibleNames;
}
