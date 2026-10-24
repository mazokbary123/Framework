package mg.itu.myframework.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapSession {
    private Map<String, Object> attributes;

    public MapSession() {
        attributes = new HashMap<>();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void add(String key, Object value) {
        setAttribute(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Object get(String key) {
        return getAttribute(key);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void remove(String key) {
        removeAttribute(key);
    }

    public void clearAttributes() {
        attributes.clear();
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    public Set<String> getKeys() {
        return getAttributeKeys();
    }

}
