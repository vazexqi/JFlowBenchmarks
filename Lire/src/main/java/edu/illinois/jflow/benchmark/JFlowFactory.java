package edu.illinois.jflow.benchmark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Just to signify that a method is a Factory method - it produces new instances of objects so we
 * want to spawn a new context each time it is called.
 * 
 * @author nchen
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface JFlowFactory {

}
