package axis.shiyan.wei.bluearchive.blinkfix.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String description();
    String[] aliases() default {};
}
