package mg.itu.myframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Auth {
    // Roles autorises separes par des virgules (ex : "admin,manager").
    // Vide : il suffit d'etre authentifie.
    String role() default "";
}
