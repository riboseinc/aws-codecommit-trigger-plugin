package com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSScmConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ScmConfigFactory {

    private static final ScmConfigFactory instance = new ScmConfigFactory();

    public static ScmConfigFactory get() {
        return instance;
    }

    private ScmConfigFactory() {
    }

    public List<SQSScmConfig> createIR() {
        return Collections.singletonList(new SQSScmConfig(SQSScmConfig.Type.IR, null, null));
    }

    public List<SQSScmConfig> createERs(String... args) {
        List<SQSScmConfig> scms = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2) {
            String url = args[i];
            String subscribedBranches = args[i + 1];
            scms.add(new SQSScmConfig(SQSScmConfig.Type.ER, url, subscribedBranches));
        }
        return Collections.unmodifiableList(scms);
    }
}
