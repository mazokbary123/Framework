package mg.itu.myframework.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;

public class BindingUtil {
    private static String sep = ".";

    public static Map<String, Class<?>> getObjectParameters(String parameterName, Class<?> classe, String separation) {
        return getAttributTypes(parameterName, classe, separation);
    }

    public static Map<String, Class<?>> getObjectParameters(String parameterName, Class<?> classe) {
        return getObjectParameters(parameterName, classe, sep);
    }

    public static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || type.equals(String.class)
                || Number.class.isAssignableFrom(type)
                || type.equals(Boolean.class)
                || type.equals(Character.class)
                || java.util.Date.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type)
                || type.equals(java.util.UUID.class);
    }

    public static Map<String, Class<?>> getAttributTypes(String prefix, Class<?> classe, String separation) {
        Map<String, Class<?>> parameters = new HashMap<>();
        if (classe == null || classe == Object.class) {
            return parameters;
        }

        for (Field field : classe.getDeclaredFields()) {
            String name = prefix + separation + field.getName();
            Class<?> type = field.getType();

            if (isSimpleType(type)) {
                parameters.put(name, type);
            } else {
                parameters.putAll(
                    getAttributTypes(name, type, separation)
                );
            }
        }

        // Récupérer les attributs des classes parentes
        parameters.putAll(
            getAttributTypes(prefix, classe.getSuperclass(), separation)
        );

        return parameters;
    }

    public static void printObjectParameters(
            String parameterName, Class<?> classe) {

        Map<String, Class<?>> parameters =
                getObjectParameters(parameterName, classe);

        for (Map.Entry<String, Class<?>> entry : parameters.entrySet()) {
            System.out.println(
                entry.getKey() + " (" +
                entry.getValue().getSimpleName() + ")"
            );
        }
    }

    public static Object buildObject(String parameterName, Class<?> classe, HttpServletRequest req) throws Exception{
        return buildObject(parameterName, classe, req , sep);
    }

    public  static  Object buildObject(String parameterName, Class<?> classe, HttpServletRequest req , String separation) throws Exception {
        Object object = classe.getDeclaredConstructor().newInstance();
        Map<String, Class<?>> parameters = getObjectParameters(parameterName, classe);

        for (Map.Entry<String, Class<?>> entry : parameters.entrySet()) {
            String name = entry.getKey();
            Class<?> type = entry.getValue();
            String value = req.getParameter(name);

            if (value == null || value.isEmpty()) {
                continue;
            }

            String[] attributes = name.substring(parameterName.length() + 1).split(java.util.regex.Pattern.quote(separation));
            Object currentObject = object;
            Class<?> currentClass = classe;

            // Parcourir les objets imbriqués
            for (int i = 0; i < attributes.length - 1; i++) {
                Field field = findField(currentClass, attributes[i]);
                field.setAccessible(true);
                Object nestedObject = field.get(currentObject);

                if (nestedObject == null) {
                    nestedObject = field.getType().getDeclaredConstructor().newInstance();
                    field.set(currentObject, nestedObject);
                }

                currentObject = nestedObject;
                currentClass = field.getType();
            }

            // Affecter la valeur au dernier attribut
            String lastAttribute = attributes[attributes.length - 1];
            Field field = findField(currentClass, lastAttribute);
            field.setAccessible(true);

            field.set(currentObject, convertParameter(value, type));
        }

        return object;
    }

    private static Field findField(Class<?> classe, String name) throws NoSuchFieldException {
        Class<?> current = classe;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(name);
    }

    public  static  Object convertParameter(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(value);
        } else if (targetType == char.class || targetType == Character.class) {
            return value.charAt(0);
        }
        else if (targetType == java.util.Date.class) {
            try {
                return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


}
