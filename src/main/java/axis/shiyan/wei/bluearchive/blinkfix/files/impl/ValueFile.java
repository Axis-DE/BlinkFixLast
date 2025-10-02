package axis.shiyan.wei.bluearchive.blinkfix.files.impl;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.files.ClientFile;
import axis.shiyan.wei.bluearchive.blinkfix.values.*;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ValueFile extends ClientFile {
    private final static Logger logger = LogManager.getLogger(ValueFile.class);

    public ValueFile() {
        super("values.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        ValueManager valueManager = BlinkFix.getInstance().getValueManager();
        HasValueManager hasValueManager = BlinkFix.getInstance().getHasValueManager();

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                String[] split = line.split(":", 4);

                if (split.length != 4) {
                    logger.error("Failed to read line {}!", line);
                    continue;
                }

                String valueType = split[0];

                String name = split[1];
                String valueName = split[2];
                String value = split[3];

                HasValue module = hasValueManager.getHasValue(name);

                switch (valueType) {
                    case "B":
                        valueManager.getValue(module, valueName).getBooleanValue().setCurrentValue(Boolean.parseBoolean(value));
                        break;

                    case "F":
                        valueManager.getValue(module, valueName).getFloatValue().setCurrentValue(Float.parseFloat(value));
                        break;

                    case "S":
                        valueManager.getValue(module, valueName).getStringValue().setCurrentValue(value);
                        break;

                    case "M":
                        int index = Integer.parseInt(value);
                        ModeValue modeValue = valueManager.getValue(module, valueName).getModeValue();

                        if (index < 0 || index >= modeValue.getValues().length) {
                            logger.error("Failed to read mode value {}!", line);
                        } else {
                            modeValue.setCurrentValue(index);
                        }
                        break;

                    default:
                        logger.error("Unknown value type of {}!", name);
                        break;
                }
            } catch (Exception e) {
                logger.error("Failed to read value {}!", line);
            }
        }
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        ValueManager valueManager = BlinkFix.getInstance().getValueManager();

        for (Value value : valueManager.getValues()) {
            try {
                ValueType valueType = value.getValueType();

                switch (valueType) {
                    case BOOLEAN:
                        writer.write(String.format("B:%s:%s:%s\n", value.getKey().getName(), value.getName(), value.getBooleanValue().getCurrentValue()));
                        break;

                    case FLOAT:
                        writer.write(String.format("F:%s:%s:%s\n", value.getKey().getName(), value.getName(), value.getFloatValue().getCurrentValue()));
                        break;

                    case STRING:
                        writer.write(String.format("S:%s:%s:%s\n", value.getKey().getName(), value.getName(), value.getStringValue().getCurrentValue()));
                        break;

                    case MODE:
                        writer.write(String.format("M:%s:%s:%s\n", value.getKey().getName(), value.getName(), value.getModeValue().getCurrentValue()));
                        break;

                    default:
                        logger.error("Unknown value type of {}!", value.getKey().getName());
                        break;
                }
            } catch (Exception e) {
                logger.error("Failed to save value {}!", value.getKey().getName());
            }
        }
    }
}
