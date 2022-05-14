package ru.job4j.controller.tools;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class FieldDataSetter {
    public static <T> T setByReflection(T oldObj, T newObj) throws InvocationTargetException, IllegalAccessException {
        var methods = oldObj.getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method : methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                namePerMethod.put(name, method);
            }
        }
        for (var name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                var getMethod = namePerMethod.get(name);
                var setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Impossible invoke set method from object : " + oldObj + ", Check set and get pairs.");
                }
                var newValue = getMethod.invoke(newObj);
                if (newValue != null) {
                    setMethod.invoke(oldObj, newValue);
                }
            }
        }
        return oldObj;
    }
}
