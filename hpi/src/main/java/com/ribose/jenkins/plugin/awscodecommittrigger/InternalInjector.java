package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import jenkins.ProxyInjector;

public class InternalInjector extends ProxyInjector {

    private Module module = new Context();
    private Injector injector;

    @Override
    protected Injector resolve() {
        if (injector == null) {
            injector = Guice.createInjector(module);
        }
        return injector;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
        this.injector = null;
    }
}
