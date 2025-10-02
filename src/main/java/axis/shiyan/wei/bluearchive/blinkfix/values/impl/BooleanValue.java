package axis.shiyan.wei.bluearchive.blinkfix.values.impl;

import axis.shiyan.wei.bluearchive.blinkfix.values.HasValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.Value;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanValue extends Value {
    private final boolean defaultValue;
    private final Consumer<Value> update;

    public boolean currentValue;

    public BooleanValue(HasValue key, String name, boolean defaultValue, Consumer<Value> update, Supplier<Boolean> visibility) {
        super(key, name, visibility);
        this.update = update;
        this.currentValue = this.defaultValue = defaultValue;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.BOOLEAN;
    }

    @Override
    public BooleanValue getBooleanValue() {
        return this;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(boolean currentValue) {
        this.currentValue = currentValue;

        if (update != null) {
            update.accept(this);
        }
    }
}
