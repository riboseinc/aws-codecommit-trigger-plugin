package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.SQSScmConfig.Messages;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@ExportedBean
public class SQSScmConfig extends AbstractDescribableImpl<SQSScmConfig> implements Serializable {

    public enum Type {JOB_SCM, URL}

    private String subscribedBranches;
    private Type type;
    private String url;

    private transient List<BranchSpec> branchSpecs;

    @DataBoundConstructor
    public SQSScmConfig(Type type, String url, String subscribedBranches) {
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
//            Collections.singletonList(new BranchSpec("")),
            getBranchSpecs(),
            false,
            Collections.<SubmoduleConfig>emptyList(),
            null,
            null,
            Collections.<GitSCMExtension>emptyList()
        );
    }

//    public static void yield(SQSJob sqsJob, Predicate predicate) {
//        List<SCM> scms = sqsJob.getScmList();
//        for (SCM scm : scms) {
//            if (scm instanceof GitSCM) {//TODO refactor to visitor
//                GitSCM git = (GitSCM) scm;
//                List<RemoteConfig> repos = git.getRepositories();
//                for (RemoteConfig repo : repos) {
//                    for (URIish urIish : repo.getURIs()) {
//                        urls.add(urIish.toString());
//                    }
//                }
//
//                for (BranchSpec branchSpec : git.getBranches()) {
//                    branches.add(branchSpec.getName());
//                }
//            }
//        }
//    }


//    public static SQSScmConfig fromSCM(SCM scm) {
//        if (scm instanceof GitSCM) {
//            GitSCM git = (GitSCM) scm;
//            return new SQSScmConfig(git.getUserRemoteConfigs())
//        }
//    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SQSScmConfig> {

//        public FormValidation doCheckSubscribedBranches(@QueryParameter final String subscribedBranches) {
//            if (StringUtils.isBlank(subscribedBranches)) {
//                return FormValidation.warning(Messages.warningSubscribedBranches());
//            }
//            return FormValidation.ok();
//        }

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
            return com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.isCodeCommitRepo(url)
                ? FormValidation.ok()
                : FormValidation.error(Messages.errorCodeCommitUrlInvalid());
        }

//        public String getSubscribeBranchPage() {
//            return getViewPage(clazz, "subscribedBranches.jelly");
//        }
    }
}
