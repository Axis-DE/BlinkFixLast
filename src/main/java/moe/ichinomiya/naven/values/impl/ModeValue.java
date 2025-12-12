package moe.ichinomiya.naven.values.impl;

import lombok.Getter;
import moe.ichinomiya.naven.values.HasValue;
import moe.ichinomiya.naven.values.Value;
import moe.ichinomiya.naven.values.ValueType;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class ModeValue extends Value {
    private final String[] values;
    private final Consumer<Value> update;
    private int currentValue;

    public ModeValue(HasValue key, String name, String[] values, int defaultValue, Consumer<Value> update, Supplier<Boolean> visibility) {
        super(key, name, visibility);

        this.update = update;
        this.values = values;
        this.currentValue = defaultValue;
    }

    public boolean isCurrentMode(String mode) {
        return getCurrentMode().equalsIgnoreCase(mode);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.MODE;
    }

    @Override
    public ModeValue getModeValue() {
        return this;
    }

    public String getCurrentMode() {
        return values[currentValue];
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;

        if (update != null) {
            update.accept(this);
        }
    }
}
