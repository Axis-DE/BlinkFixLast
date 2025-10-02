package axis.shiyan.wei.bluearchive.blinkfix.values;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.exceptions.BadValueTypeException;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.StringValue;

import java.util.function.Supplier;

@Getter
public abstract class Value {
    private final HasValue key;
    private final String name;
    private final Supplier<Boolean> visibility;

    protected Value(HasValue key, String name, Supplier<Boolean> visibility) {
        this.key = key;
        this.name = name;
        this.visibility = visibility;

        BlinkFix.getInstance().getValueManager().addValue(this);
    }

    public abstract ValueType getValueType();

    public BooleanValue getBooleanValue() {
        throw new BadValueTypeException();
    }

    public FloatValue getFloatValue() {
        throw new BadValueTypeException();
    }

    public StringValue getStringValue() {
        throw new BadValueTypeException();
    }

    public ModeValue getModeValue() {
        throw new BadValueTypeException();
    }

    public boolean isVisible() {
        return visibility == null || visibility.get();
    }
}
