package mg.itu.myframework.util;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import mg.itu.myframework.model.MapSession;

public class SessionUtil {
    private static final String mapSessionKey = "mapsession";

    public static void setMapSession(HttpServletRequest request, MapSession mapSession) {
        if (mapSession == null) {
            mapSession = new MapSession();
        }

        HttpSession session = request.getSession();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            mapSession.add(name, session.getAttribute(name));
        }

        setMapSessionContext(request, mapSession);
    }

    public static void rectifySession(HttpServletRequest request, MapSession mapSession) {
        if (mapSession == null) {
            return;
        }

        HttpSession session = request.getSession();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (!mapSession.getAttributes().containsKey(name)) {
                session.removeAttribute(name);
            }
        }

        for (String name : mapSession.getKeys()) {
            session.setAttribute(name, mapSession.get(name));
        }
    }

    public static boolean verifyMapSessionObject(HttpServletRequest request, Object[] parameters) {
        if (parameters == null) {
            return false;
        }

        for (Object parameter : parameters) {
            if (parameter instanceof MapSession) {
                if (getMapSessionContext(request) == null) {
                    setMapSessionContext(request, (MapSession) parameter);
                }
                return true;
            }
        }

        return false;
    }

    public static void setMapSessionContext(HttpServletRequest request, MapSession mapSession) {
        Map<String, MapSession> mapSessions = getMapSessionsContext(request);
        mapSessions.put(request.getSession().getId(), mapSession);
    }

    public static MapSession getMapSessionContext(HttpServletRequest request) {
        Map<String, MapSession> mapSessions = getMapSessionsContext(request);
        return mapSessions.get(request.getSession().getId());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, MapSession> getMapSessionsContext(HttpServletRequest request) {
        synchronized (request.getServletContext()) {
            Object context = request.getServletContext().getAttribute(mapSessionKey);
            if (context == null) {
                Map<String, MapSession> mapSessions = new ConcurrentHashMap<>();
                request.getServletContext().setAttribute(mapSessionKey, mapSessions);
                return mapSessions;
            }

            return (Map<String, MapSession>) context;
        }
    }

}
