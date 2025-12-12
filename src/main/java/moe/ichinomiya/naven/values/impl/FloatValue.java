package moe.ichinomiya.naven.values.impl;

import lombok.Getter;
import moe.ichinomiya.naven.utils.MathUtils;
import moe.ichinomiya.naven.values.HasValue;
import moe.ichinomiya.naven.values.Value;
import moe.ichinomiya.naven.values.ValueType;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class FloatValue extends Value {
    private final float defaultValue;
    private final float minValue;
    private final float maxValue;
    private final float step;

    private final Consumer<Value> update;
    private float currentValue;

    public FloatValue(HasValue key, String name, float defaultValue, float minValue, float maxValue, float step, Consumer<Value> update, Supplier<Boolean> visibility) {
        super(key, name, visibility);

        this.update = update;
        this.currentValue = this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.FLOAT;
    }

    @Override
    public FloatValue getFloatValue() {
        return this;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = MathUtils.clampValue(currentValue, minValue, maxValue);

        if (update != null) {
            update.accept(this);
        }
    }
}
