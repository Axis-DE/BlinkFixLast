package tech.skidonion.obfuscator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ControlFlowObfuscation {
  boolean obfuscated() default true;
}


/* Location:              D:\project\b\Southside-Public\libraries\phantomshield-annotations.jar!\tech\skidonion\obfuscator\annotations\ControlFlowObfuscation.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */