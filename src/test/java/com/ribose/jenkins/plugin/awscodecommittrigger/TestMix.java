package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.vdurmont.semver4j.Semver;
import org.junit.Test;

public class TestMix {

    @Test
    public void testSemver() {
        String version = "3.0.0-SNAPSHOT";
        String compVersion = "3.0.0";
        Semver semver = new Semver(compVersion);
        Semver vSemver = new Semver(version).withClearedSuffixAndBuild();
        System.out.println(semver.isGreaterThanOrEqualTo(vSemver));
    }
}
