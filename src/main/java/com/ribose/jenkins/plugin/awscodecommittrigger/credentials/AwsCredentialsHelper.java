package com.ribose.jenkins.plugin.awscodecommittrigger.credentials;

import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.security.ACL;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;


public class AwsCredentialsHelper {

    private AwsCredentialsHelper() {
    }

    @CheckForNull
    public static AmazonWebServicesCredentials getCredentials(@Nullable String credentialsId) {
        if (StringUtils.isBlank(credentialsId)) {
            return null;
        }

//        return AWSCredentialsHelper.getCredentials(credentialsId, null);

//        return (AmazonWebServicesCredentials) CredentialsMatchers.firstOrNull(
//            CredentialsProvider.lookupCredentials(AmazonWebServicesCredentials.class, context,
//                ACL.SYSTEM, Collections.EMPTY_LIST),
//            CredentialsMatchers.withId(credentialsId));

        return CredentialsMatchers.firstOrNull(
            CredentialsProvider.lookupCredentials(AmazonWebServicesCredentials.class, (Item) null, ACL.SYSTEM, null, null),
            CredentialsMatchers.withId(credentialsId)
        );
    }

//    @CheckForNull
//    public static AwsCredentials getCredentials(@NotNull String accessKey, @NotNull Secret secretKey) {
//        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey.getPlainText())) {
//            return null;
//        }
//
//        return CredentialsMatchers.firstOrNull(
//            CredentialsProvider.lookupCredentials(AwsCredentials.class, (Item) null, ACL.SYSTEM, null, null),
//            CredentialsMatchers.allOf(
//                CredentialsMatchers.withProperty("accessKey", accessKey),
//                CredentialsMatchers.withProperty("secretKey", secretKey)
//            )
//        );
//    }
}
