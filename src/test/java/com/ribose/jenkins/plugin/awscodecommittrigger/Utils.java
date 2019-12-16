package com.ribose.jenkins.plugin.awscodecommittrigger;

import java.net.URL;

public class Utils {

    public static URL getResource(Class clazz, String name) {
        return getResource(clazz, name, false);
    }

    public static URL getResource(Class clazz, String name, boolean includeClassName) {
        return clazz.getResource((includeClassName ? clazz.getSimpleName() + "/" : "") + name);
    }
}
