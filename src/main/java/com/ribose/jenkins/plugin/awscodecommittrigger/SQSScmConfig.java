package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.sqsscmconfig.Messages;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@ExportedBean
public class SQSScmConfig extends AbstractDescribableImpl<SQSScmConfig> {

    public enum Type {IR, ER}

    private String subscribedBranches;
    private Type type;
    private String url;

    private transient List<BranchSpec> branchSpecs;

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
        return new GitSCM(
            GitSCM.createRepoList(this.url, null),
            getBranchSpecs(),
            false,
            Collections.<SubmoduleConfig>emptyList(),
            null,
            null,
            Collections.<GitSCMExtension>emptyList()
        );
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SQSScmConfig> {

        @Override
        public SQSScmConfig newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            JSONObject json = jsonObject.getJSONObject("type");
            json.put("type", json.getString("value"));
            json.remove("value");
            return super.newInstance(req, json);//req.bindJSON(SQSScmConfig.class, json);
        }

        public FormValidation doCheckUrl(@QueryParameter final String url) {
            if (StringUtils.isBlank(url)) {
                return FormValidation.warning(Messages.warningBlankUrl());
            }
            return com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.isCodeCommitRepo(url)
                ? FormValidation.ok()
                : FormValidation.error(Messages.errorCodeCommitUrlInvalid());
        }

//        public String getSubscribeBranchPage() {
//            return getViewPage(clazz, "subscribedBranches.jelly");
//        }
    }
}
