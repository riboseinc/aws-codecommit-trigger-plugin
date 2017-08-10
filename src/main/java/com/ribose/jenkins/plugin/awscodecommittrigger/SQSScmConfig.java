package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.sqstrigger.Messages;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

@ExportedBean
public class SQSScmConfig extends AbstractDescribableImpl<SQSScmConfig> implements Serializable {

    public enum Type {JOB_SCM, URL}

    private String subscribedBranches;
    private Type type;
    private String url;

    @DataBoundConstructor
    public SQSScmConfig(String subscribedBranches, Type type, String url) {
        this.subscribedBranches = subscribedBranches;
        this.type = type;

        if (this.type == Type.JOB_SCM) {
            url = "";
        }
        this.url = url;
    }

    public String getSubscribedBranches() {
        return subscribedBranches;
    }

    public Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SQSScmConfig> {

        //TODO validate codecommit url pattern
        public FormValidation doCheckSubscribedBranches(@QueryParameter final String subscribedBranches) {
            if (StringUtils.isBlank(subscribedBranches)) {
                return FormValidation.warning(Messages.warningSubscribedBranches());
            }
            return FormValidation.ok();
        }
    }
}
