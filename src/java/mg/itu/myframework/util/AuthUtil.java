package mg.itu.myframework.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import mg.itu.myframework.annotation.Auth;

public class AuthUtil {
    // Cles de session par defaut, remplacables par les context-param sessionAuth et roleAuth
    public static final String DEFAULT_SESSION_AUTH = "user";
    public static final String DEFAULT_ROLE_AUTH = "role";

    public static final int AUTHORIZED = 0;
    public static final int UNAUTHENTICATED = 401;
    public static final int FORBIDDEN = 403;

    // L'annotation de la methode prime sur celle de la classe
    public static Auth getAuth(Class<?> classe, Method method) {
        Auth auth = method.getAnnotation(Auth.class);
        return auth != null ? auth : classe.getAnnotation(Auth.class);
    }

    public static List<String> getRolesFromAuthAnnotation(String roleNames) {
        List<String> roles = new ArrayList<>();
        if (roleNames != null) {
            for (String role : roleNames.split(",")) {
                if (!role.trim().isEmpty()) {
                    roles.add(role.trim());
                }
            }
        }
        return roles;
    }

    public static boolean verifyRoleAccess(Object roleAuth, String roleNames) {
        List<String> roles = getRolesFromAuthAnnotation(roleNames);
        if (roles.isEmpty()) {
            return true;
        }
        if (roleAuth == null) {
            return false;
        }
        return roles.contains(roleAuth.toString());
    }

    public static int check(HttpServletRequest req, Class<?> classe, Method method) {
        Auth auth = getAuth(classe, method);
        if (auth == null) {
            return AUTHORIZED;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(getSessionAuthKey(req)) == null) {
            return UNAUTHENTICATED;
        }

        Object roleAuth = session.getAttribute(getRoleAuthKey(req));
        return verifyRoleAccess(roleAuth, auth.role()) ? AUTHORIZED : FORBIDDEN;
    }

    public static String getMessage(int status) {
        if (status == UNAUTHENTICATED) {
            return "Cette methode exige une authentification.";
        }
        return "Vous n'avez pas le role requis pour acceder a cette methode.";
    }

    private static String getSessionAuthKey(HttpServletRequest req) {
        Object key = req.getServletContext().getAttribute("sessionAuth");
        return key != null ? key.toString() : DEFAULT_SESSION_AUTH;
    }

    private static String getRoleAuthKey(HttpServletRequest req) {
        Object key = req.getServletContext().getAttribute("roleAuth");
        return key != null ? key.toString() : DEFAULT_ROLE_AUTH;
    }
}
