package mg.itu.myframework.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import mg.itu.myframework.model.MethodClassMapping;

// Une seule instance par controller (singleton), conservee dans le ServletContext
public class ControllerUtil {
    private static final String mapControllersKey = "mapControllers";

    public static Object getInstance(MethodClassMapping mapping, HttpServletRequest req) throws Exception {
        Map<String, Object> mapControllers = getMapControllers(req.getServletContext());
        String className = mapping.getClasse().getName();

        synchronized (mapControllers) {
            Object instance = mapControllers.get(className);
            if (instance == null) {
                instance = mapping.getClasse().getDeclaredConstructor().newInstance();
                mapControllers.put(className, instance);
            }
            return instance;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapControllers(ServletContext context) {
        synchronized (context) {
            Object mapControllers = context.getAttribute(mapControllersKey);
            if (mapControllers == null) {
                mapControllers = new ConcurrentHashMap<String, Object>();
                context.setAttribute(mapControllersKey, mapControllers);
            }
            return (Map<String, Object>) mapControllers;
        }
    }
}
