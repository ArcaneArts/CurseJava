package art.arcane.curse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
public class CursedContext {
    private Class<?> type;
    private Object instance;
}
