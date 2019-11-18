package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.MarkIndex;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static URL getResource(Class clazz, String name) {
        return getResource(clazz, name, false);
    }

    public static URL getResource(Class clazz, String name, boolean includeClassName) {
        return clazz.getResource((includeClassName ? clazz.getSimpleName() + "/" : "") + name);
    }
}
