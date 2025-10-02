package axis.shiyan.wei.bluearchive.blinkfix.values;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HasValueManager {
    @Getter
    private final List<HasValue> hasValues = new ArrayList<>();
    private final Map<String, HasValue> nameMap = new HashMap<>();

    public void registerHasValue(HasValue hasValue) {
        hasValues.add(hasValue);
        nameMap.put(hasValue.getName().toLowerCase(), hasValue);
    }

    public HasValue getHasValue(String name) {
        return nameMap.get(name.toLowerCase());
    }
}
