package axis.shiyan.wei.bluearchive.blinkfix.values;

import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.StringValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ValueBuilder {
    private final HasValue key;
    private final String name;

    private ValueType valueType;
    private Consumer<Value> update;
    private Supplier<Boolean> visibility;

    // Boolean Value
    private boolean defaultBooleanValue;

    // Float Value
    private float defaultFloatValue;
    private float minFloatValue;
    private float maxFloatValue;
    private float step;

    // Mode Value
    private String[] modes;
    private int defaultModeIndex;

    // String Value
    private String defaultStringValue;

    private ValueBuilder(HasValue key, String name) {
        this.key = key;
        this.name = name;
    }

    public static ValueBuilder create(HasValue key, String name) {
        return new ValueBuilder(key, name);
    }

    public ValueBuilder setValueType(ValueType valueType) {
        this.valueType = valueType;
        return this;
    }

    public ValueBuilder setDefaultBooleanValue(boolean defaultBooleanValue) {
        if (valueType == null) {
            setValueType(ValueType.BOOLEAN);
        }

        if (valueType != ValueType.BOOLEAN) {
            throw new IllegalStateException("Value type is not boolean");
        }

        this.defaultBooleanValue = defaultBooleanValue;
        return this;
    }

    public ValueBuilder setDefaultFloatValue(float defaultFloatValue) {
        if (valueType == null) {
            setValueType(ValueType.FLOAT);
        }

        if (valueType != ValueType.FLOAT) {
            throw new IllegalStateException("Value type is not float");
        }

        this.defaultFloatValue = defaultFloatValue;
        return this;
    }

    public ValueBuilder setMinFloatValue(float minFloatValue) {
        if (valueType == null) {
            setValueType(ValueType.FLOAT);
        }

        if (valueType != ValueType.FLOAT) {
            throw new IllegalStateException("Value type is not float");
        }

        this.minFloatValue = minFloatValue;
        return this;
    }

    public ValueBuilder setMaxFloatValue(float maxFloatValue) {
        if (valueType == null) {
            setValueType(ValueType.FLOAT);
        }

        if (valueType != ValueType.FLOAT) {
            throw new IllegalStateException("Value type is not float");
        }

        this.maxFloatValue = maxFloatValue;
        return this;
    }

    public ValueBuilder setFloatStep(float step) {
        if (valueType == null) {
            setValueType(ValueType.FLOAT);
        }

        if (valueType != ValueType.FLOAT) {
            throw new IllegalStateException("Value type is not float");
        }

        this.step = step;
        return this;
    }

    public ValueBuilder setModes(String... modes) {
        if (valueType == null) {
            setValueType(ValueType.MODE);
        }

        if (valueType != ValueType.MODE) {
            throw new IllegalStateException("Value type is not mode");
        }

        this.modes = modes;
        return this;
    }

    public ValueBuilder setDefaultModeIndex(int defaultModeIndex) {
        if (valueType == null) {
            setValueType(ValueType.MODE);
        }

        if (valueType != ValueType.MODE) {
            throw new IllegalStateException("Value type is not mode");
        }

        this.defaultModeIndex = defaultModeIndex;
        return this;
    }

    public ValueBuilder setDefaultStringValue(String defaultStringValue) {
        if (valueType == null) {
            setValueType(ValueType.STRING);
        }

        if (valueType != ValueType.STRING) {
            throw new IllegalStateException("Value type is not string");
        }

        this.defaultStringValue = defaultStringValue;
        return this;
    }

    public ValueBuilder setOnUpdate(Consumer<Value> update) {
        this.update = update;
        return this;
    }

    public ValueBuilder setVisibility(Supplier<Boolean> visibility) {
        this.visibility = visibility;
        return this;
    }

    public Value build() {
        if (valueType == null) {
            throw new IllegalStateException("Value type is not set");
        }

        switch (valueType) {
            case BOOLEAN:
                return new BooleanValue(key, name, defaultBooleanValue, update, visibility);
            case FLOAT:
                return new FloatValue(key, name, defaultFloatValue, minFloatValue, maxFloatValue, step, update, visibility);
            case MODE:
                if (modes == null) {
                    throw new IllegalStateException("Modes are not set");
                }

                return new ModeValue(key, name, modes, defaultModeIndex, update, visibility);
            case STRING:
                if (defaultStringValue == null) {
                    throw new IllegalStateException("Default string value is not set");
                }

                return new StringValue(key, name, defaultStringValue, update, visibility);
            default:
                throw new IllegalStateException("Unknown value type");
        }
    }
}
