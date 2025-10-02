package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
    private static final Gson GSON = newBuilderNoPretty().create();

    public static GsonBuilder custom() {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                if (fieldAttributes.getAnnotation(GsonIgnore.class) != null) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                if (aClass.getAnnotation(GsonIgnore.class) != null) {
                    return true;
                }
                return false;
            }
        });
    }

    public static GsonBuilder newBuilder() {
        return custom().setPrettyPrinting();
    }

    public static GsonBuilder newBuilderNoPretty() {
        return custom();
    }
}
