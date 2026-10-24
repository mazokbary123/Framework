package mg.itu.myframework.util;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import mg.itu.myframework.annotation.Auth;

public class AuthUtil {
    public static final String USER_KEY = "user";
    public static final String ROLE_KEY = "role";

    public static final int AUTHORIZED = 0;
    public static final int UNAUTHENTICATED = 401;
    public static final int FORBIDDEN = 403;

    // L'annotation de la methode prime sur celle de la classe
    public static Auth getAuth(Class<?> classe, Method method) {
        Auth auth = method.getAnnotation(Auth.class);
        return auth != null ? auth : classe.getAnnotation(Auth.class);
    }

    public static int check(HttpServletRequest req, Class<?> classe, Method method) {
        Auth auth = getAuth(classe, method);
        if (auth == null) {
            return AUTHORIZED;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(USER_KEY) == null) {
            return UNAUTHENTICATED;
        }

        if (auth.roles().length == 0) {
            return AUTHORIZED;
        }

        Object role = session.getAttribute(ROLE_KEY);
        for (String allowed : auth.roles()) {
            if (allowed.equals(role)) {
                return AUTHORIZED;
            }
        }
        return FORBIDDEN;
    }
}
