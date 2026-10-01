package mg.itu.myframework.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.itu.myframework.annotation.Controller;
import mg.itu.myframework.model.MethodClassMapping;
import mg.itu.myframework.model.UrlMethod;
import mg.itu.myframework.util.ClassUtil;

@WebListener
public class AppStartUpListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("-----------------------------------------");
            ServletContext context = sce.getServletContext();
            Object springContext = context.getAttribute("springContext");

            String packageName = context.getInitParameter("packageNames");
            String prefix = context.getInitParameter("prefix");
            String suffix = context.getInitParameter("suffix");

            if (packageName == null || packageName.trim().isEmpty()) {
                throw new RuntimeException(
                        "Le context-param 'packageNames' est introuvable dans web.xml.");
            }

            List<String> packageNames = List.of(packageName.split(";"));
            List<String> listController = new ArrayList<>();
            Map<UrlMethod, MethodClassMapping> listUrlMapping = new HashMap<>();

            List<Class<?>> controllers = ClassUtil.getClassesWithAnnotation(
                    packageNames,
                    listUrlMapping,
                    Controller.class);

            for (Class<?> controller : controllers) {
                listController.add(controller.getName());
                System.out.println("Controller trouvé : " + controller.getName());
            }

            context.setAttribute("listController", listController);
            context.setAttribute("listUrlMapping", listUrlMapping);
            context.setAttribute("prefix", prefix);
            context.setAttribute("suffix", suffix);
            context.setAttribute("springContext", springContext);

            System.out.println("Framework initialisé.");
            System.out.println("-----------------------------------------");

        } catch (Exception e) {
            System.out.println("-----------------------------------------");
            throw new RuntimeException(e);

        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // ContextLoaderListener ferme déjà le contexte Spring automatiquement.
        System.out.println("Application arrêtée !");
    }
}