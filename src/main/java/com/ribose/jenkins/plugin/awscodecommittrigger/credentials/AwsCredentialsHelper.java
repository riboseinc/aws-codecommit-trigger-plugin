package com.ribose.jenkins.plugin.awscodecommittrigger.credentials;

import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.security.ACL;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;


public final class AwsCredentialsHelper {

    private AwsCredentialsHelper() {
    }

    @CheckForNull
    public static AmazonWebServicesCredentials getCredentials(@Nullable String credentialsId) {
        return AwsCredentialsHelper.getCredentials(AmazonWebServicesCredentials.class, credentialsId);
    }

    @CheckForNull
    public static <T extends Credentials> T getCredentials(Class<T> clz, @Nullable String credentialsId) {
        if (StringUtils.isBlank(credentialsId)) {
            return null;
        }

        return CredentialsMatchers.firstOrNull(
            CredentialsProvider.lookupCredentials(clz, (Item) null, ACL.SYSTEM, null, null),
            CredentialsMatchers.withId(credentialsId)
        );
    }
}
