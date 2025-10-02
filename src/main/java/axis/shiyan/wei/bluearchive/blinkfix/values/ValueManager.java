package axis.shiyan.wei.bluearchive.blinkfix.values;

import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.exceptions.NoSuchValueException;

import java.util.ArrayList;
import java.util.List;

public class ValueManager {
    @Getter
    private final List<Value> values = new ArrayList<>();

    public void addValue(Value value) {
        values.add(value);
    }

    public List<Value> getValuesByHasValue(HasValue key) {
        List<Value> values = new ArrayList<>();

        for (Value value : this.values) {
            if (value.getKey() == key) {
                values.add(value);
            }
        }

        return values;
    }

    public Value getValue(HasValue key, String name) {
        for (Value value : values) {
            if (value.getKey() == key && value.getName().equals(name)) {
                return value;
            }
        }

        throw new NoSuchValueException();
    }
}
