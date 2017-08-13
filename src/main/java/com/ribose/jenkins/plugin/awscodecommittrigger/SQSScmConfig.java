package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.SQSScmConfig.Messages;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
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

    public String getSwubscribedBranches() {
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

        public FormValidation doCheckSubscribedBranches(@QueryParameter final String subscribedBranches) {
            if (StringUtils.isBlank(subscribedBranches)) {
                return FormValidation.warning(Messages.warningSubscribedBranches());
            }
            return FormValidation.ok();
        }

        @Override
        public SQSScmConfig newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            JSONObject json = jsonObject.getJSONObject("type");
            json.put("type", json.getString("value"));
            json.remove("value");
            return req.bindJSON(SQSScmConfig.class, json);
        }

        public FormValidation doCheckUrl(@QueryParameter final String url) {
            if (StringUtils.isBlank(url)) {
                return FormValidation.warning(Messages.warningBlankUrl());
            }

            String repoName = com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.getCodeCommitRepoName(url);
            if (StringUtils.isEmpty(repoName)) {
                return FormValidation.error(Messages.errorCodeCommitUrlInvalid());
            }
            return FormValidation.ok();
        }
    }
}
