package com.ribose.jenkins.plugin.awscodecommittrigger;

import org.apache.commons.lang.StringUtils;
import javax.annotation.Nullable;
import com.vdurmont.semver4j.Semver;


public class PluginInfo {
    public static final String version = "${project.version}";
    public static final String compatibleSinceVersion = "${hpi.compatibleSinceVersion}";

    public static boolean checkPluginCompatibility(@Nullable String version) {
        if (StringUtils.isBlank(version)) {
            return false;
        }

        Semver since = new Semver(compatibleSinceVersion);
        Semver current = new Semver(version).withClearedSuffixAndBuild();
        return current.isGreaterThanOrEqualTo(since);
    }
}
