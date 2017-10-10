package com.ribose.jenkins.plugin.awscodecommittrigger.credentials;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.ribose.jenkins.plugin.awscodecommittrigger.Context;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.UUID;


public class StandardAwsCredentials extends BaseStandardCredentials implements AwsCredentials {

    private static final Log log = Log.get(StandardAwsCredentials.class);

    private String displayName;
    private String accessKey;
    private Secret secretKey;

    @DataBoundConstructor
    public StandardAwsCredentials(CredentialsScope scope, String id, String description, String displayName, String accessKey, String secretKey) {
        super(scope, id, description);

        this.displayName = displayName;
        this.accessKey = accessKey;
        this.secretKey = Secret.fromString(secretKey);
    }

    public StandardAwsCredentials(String description, String accessKey, Secret secretKey) {
        this(CredentialsScope.GLOBAL, UUID.randomUUID().toString(), description, null, accessKey, secretKey.getPlainText());
    }

    public String getAccessKey() {
        return accessKey;
    }

    public Secret getSecretKey() {
        return secretKey;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(Secret secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String getAWSAccessKeyId() {
        return this.accessKey;
    }

    @Override
    public String getAWSSecretKey() {
        return this.secretKey.getPlainText();
    }

    public AWSCredentials getCredentials() {
        return new BasicAWSCredentials(getAWSAccessKeyId(), getAWSSecretKey());
    }

    @Override
    public void refresh() {
        log.debug("no-op method");
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        private final SQSFactory sqsFactory;

        public DescriptorImpl() {
            this.sqsFactory = Context.injector().getBinding(SQSFactory.class).getProvider().get();
        }

        @Override
        public String getDisplayName() {
            return "Standard Aws Credentials";
        }
    }
}
