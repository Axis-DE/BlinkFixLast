package axis.shiyan.wei.bluearchive.blinkfix.values.impl;

import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.values.HasValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.Value;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueType;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class StringValue extends Value {
    private final String defaultValue;
    private final Consumer<Value> update;

    private String currentValue;

    public StringValue(HasValue key, String name, String defaultValue, Consumer<Value> update, Supplier<Boolean> visibility) {
        super(key, name, visibility);

        this.update = update;
        this.defaultValue = defaultValue;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    @Override
    public StringValue getStringValue() {
        return this;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;

        if (update != null) {
            update.accept(this);
        }
    }
}
