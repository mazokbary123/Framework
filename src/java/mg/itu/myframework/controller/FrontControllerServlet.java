package mg.itu.myframework.controller;

import java.io.*;
import java.lang.reflect.Parameter;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import org.springframework.context.ApplicationContext;

import mg.itu.myframework.annotation.Controller;
import mg.itu.myframework.model.MethodClassMapping;
import mg.itu.myframework.model.UrlMethod;
import mg.itu.myframework.model.ModelAndView;
import mg.itu.myframework.model.MapFile;
import mg.itu.myframework.model.MapSession;
import mg.itu.myframework.util.SessionUtil;

import mg.itu.myframework.annotation.WebApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import mg.itu.myframework.annotation.ObjectArg;

import mg.itu.myframework.util.BindingUtil;

@Controller
@MultipartConfig
public class FrontControllerServlet extends HttpServlet {

    private List<String> listController = new ArrayList<>();
    private Map<UrlMethod, MethodClassMapping> listUrlMapping = new HashMap<>();
    private String prefix;
    private String suffix;
    private ApplicationContext springContext;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        try {
            ServletContext context = getServletContext();

            this.prefix = (String) context.getAttribute("prefix");
            this.suffix = (String) context.getAttribute("suffix");
            this.springContext = (ApplicationContext) context.getAttribute("springContext");

            List<String> controllersFromContext = (List<String>) context.getAttribute("listController");
            if (controllersFromContext != null) {
                this.listController = controllersFromContext;
            }

            Map<UrlMethod, MethodClassMapping> mappingsFromContext =
                    (Map<UrlMethod, MethodClassMapping>) context.getAttribute("listUrlMapping");
            if (mappingsFromContext != null) {
                this.listUrlMapping = mappingsFromContext;
            }
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation", e);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }


    private void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        String path = uri.substring(req.getContextPath().length());

        String method = req.getMethod();
        MethodClassMapping mapping = getMapping(path, method);

        if (mapping != null) {
            invokeMethod(mapping, req, res);
        } else {
            res.setContentType("text/html");
            res.setCharacterEncoding("UTF-8");

            PrintWriter out = res.getWriter();

            out.println("URL : " + path + "<br>");

            out.println("Aucune correspondance trouvée pour l'URL : "
                    + path + "<br>");

            out.println("<br>Liste des URL disponibles : <br>");
            out.println("<table border='1'>");
            out.println("<tr><th>URL</th><th>Classe</th><th>Méthode</th></tr>");

            for (Map.Entry<UrlMethod, MethodClassMapping> entry
                    : listUrlMapping.entrySet()) {

                UrlMethod url = entry.getKey();
                MethodClassMapping m = entry.getValue();

                out.println("<tr><td>"
                        + url.getUrl()
                        + " (" + url.getMethod() + ")</td><td>");

                out.println(m.getClasse().getName() + "</td><td>");

                out.println(m.getMethode().getName()
                        + "</td></tr>");
            }

            out.println("</table>");

            out.println("<br>Liste des classes contrôleurs : <br>");

            for (String controller : listController) {
                out.println("- " + controller + "<br>");
            }
        }
    }

    private MethodClassMapping getMapping(String urlName, String method) {
        for (Map.Entry<UrlMethod, MethodClassMapping> entry : listUrlMapping.entrySet()) {
            UrlMethod url = entry.getKey();
            if (url.getUrl().equals(urlName) && url.getMethod().equals(method)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void invokeMethod(MethodClassMapping mapping, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        try {
            Object instance = mapping.getClasse().getDeclaredConstructor().newInstance();

            Parameter[] parameters = mapping.getMethode().getParameters();  
            Object result;

            if (parameters.length == 0) {
                result = mapping.getMethode().invoke(instance);
            } else {
                Object[] args = getArgs(springContext, parameters, req);
                result = mapping.getMethode().invoke(instance, args);
                if (SessionUtil.verifyMapSessionObject(req, args)) {
                    SessionUtil.rectifySession(req, SessionUtil.getMapSessionContext(req));
                }
            }
            if (mapping.getMethode().isAnnotationPresent(WebApi.class)) {

                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");

                PrintWriter jsonWriter = res.getWriter();

                if (result instanceof String) {
                    jsonWriter.println((String) result);

                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String json = objectMapper.writeValueAsString(result);

                    jsonWriter.println(json);
                }

            }
            else if (result instanceof ModelAndView) {
                ModelAndView modelAndView = (ModelAndView) result;
                for (Map.Entry<String, Object> entry : modelAndView.getModel().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                String viewPath = getPrefix() + modelAndView.getView() + getSuffix();
                req.getRequestDispatcher(viewPath).forward(req, res);
            } else {
                out.println("La méthode " + mapping.getMethode().getName() +
                        " de la classe " + mapping.getClasse().getName() +
                        " ne retourne pas un objet ModelAndView.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("Erreur lors du traitement de la requête : " + e.getMessage());
        }
    }

    private MapFile toMapFile(Part part) throws IOException {
        String fileName = part.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        String extension = dot >= 0 ? fileName.substring(dot + 1) : "";
        try (InputStream in = part.getInputStream()) {
            return new MapFile(fileName, in.readAllBytes(), extension);
        }
    }

    private boolean isMultipart(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private MapFile readFile(HttpServletRequest req, String name) throws IOException, ServletException {
        if (!isMultipart(req)) {
            return null;
        }
        Part part = req.getPart(name);
        return part == null ? null : toMapFile(part);
    }

    private MapFile[] readFiles(HttpServletRequest req, String name) throws IOException, ServletException {
        List<MapFile> files = new ArrayList<>();
        if (isMultipart(req)) {
            for (Part part : req.getParts()) {
                if (part.getName().equals(name)) {
                    MapFile file = toMapFile(part);
                    if (file != null) {
                        files.add(file);
                    }
                }
            }
        }
        return files.toArray(new MapFile[0]);
    }

    private Object[] getArgs(ApplicationContext springContext, Parameter[] parameters, HttpServletRequest req) throws Exception {
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();

            if (parameterType == ApplicationContext.class) {
                args[i] = springContext;
            } else if (parameterType == MapSession.class) {
                SessionUtil.setMapSession(req, SessionUtil.getMapSessionContext(req));
                args[i] = SessionUtil.getMapSessionContext(req);
            } else if (parameterType == MapFile.class) {
                args[i] = readFile(req, parameter.getName());
            } else if (parameterType == MapFile[].class) {
                args[i] = readFiles(req, parameter.getName());
            } else if (parameter.isAnnotationPresent(ObjectArg.class)) {
                args[i] = BindingUtil.buildObject(parameter.getName(), parameterType, req);
            } else {
                String value = req.getParameter(parameter.getName());
                args[i] = BindingUtil.convertParameter(value, parameterType);
            }
        }

        return args;
    }
}