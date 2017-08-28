package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.ribose.jenkins.plugin.awscodecommittrigger.InternalInjector;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.ScmFactory;
import jenkins.model.Jenkins;

public class MockContext extends com.google.inject.AbstractModule {

    @Override
    protected void configure() {
//        super.configure();

        this.bind(ScmFactory.class)
            .to(MockScmFactory.class)
            .in(com.google.inject.Singleton.class);

        this.bind(SQSFactory.class)
            .to(MockSQSFactory.class)
            .in(com.google.inject.Singleton.class);
    }

    public static InternalInjector getInjector() {
        InternalInjector inject = Jenkins.getInstance().lookup.get(InternalInjector.class);
        Module module = Modules.override(inject.getModule()).with(new MockContext());
        inject.setModule(module);
        return inject;
    }
}
