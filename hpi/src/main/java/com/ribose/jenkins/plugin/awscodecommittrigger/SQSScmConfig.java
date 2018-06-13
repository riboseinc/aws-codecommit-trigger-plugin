package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.sqsscmconfig.Messages;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.ScmFactory;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


@ExportedBean
public class SQSScmConfig extends AbstractDescribableImpl<SQSScmConfig> {

    public enum Type {IR, ER}

    private String subscribedBranches;
    private Type type;
    private String url;

    private transient List<BranchSpec> branchSpecs;

    @Inject
    private transient ScmFactory scmFactory;

    @DataBoundConstructor
    public SQSScmConfig(Type type, String url, String subscribedBranches) {
        this.subscribedBranches = subscribedBranches;
        this.type = type;

        if (this.type == Type.IR) {
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

    public ScmFactory getScmFactory() {
        if (scmFactory == null) {
            scmFactory = Context.injector().getBinding(ScmFactory.class).getProvider().get();
        }
        return scmFactory;
    }

    public List<BranchSpec> getBranchSpecs() {
        if (CollectionUtils.isEmpty(branchSpecs)) {
            branchSpecs = new ArrayList<>();
            List<String> branches = com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.parseCsvString(subscribedBranches);
            for (String branch : branches) {
                branchSpecs.add(new BranchSpec(branch));
            }
        }
        return branchSpecs;
    }

    public GitSCM toGitSCM() {
        return getScmFactory().createGit(this.url, getBranchSpecs());
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SQSScmConfig> {

        @Override
        public SQSScmConfig newInstance(StaplerRequest req, @Nonnull JSONObject jsonObject) throws FormException {
            Object typeObject = jsonObject.get("type");
            if (typeObject.getClass().isAssignableFrom(JSONObject.class)) {
                JSONObject jsonType = jsonObject.getJSONObject("type");
                jsonObject.put("type", jsonType.getString("value"));
            }
            return super.newInstance(req, jsonObject);
        }

        public FormValidation doCheckUrl(@QueryParameter final String url) {
            if (StringUtils.isBlank(url)) {
                return FormValidation.warning(Messages.warningBlankUrl());
            }

            return com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.isCodeCommitRepo(url)
                ? FormValidation.ok()
                : FormValidation.error(Messages.errorCodeCommitUrlInvalid());
        }
    }
}
