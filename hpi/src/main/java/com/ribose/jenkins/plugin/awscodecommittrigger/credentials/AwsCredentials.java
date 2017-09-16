package com.ribose.jenkins.plugin.awscodecommittrigger.credentials;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;


@NameWith(value = AwsCredentials.NameProvider.class, priority = 1)
public interface AwsCredentials extends StandardCredentials, AWSCredentials, AWSCredentialsProvider {

    String getDisplayName();

    class NameProvider extends CredentialsNameProvider<AwsCredentials> {

        @NonNull
        @Override
        public String getName(@NonNull AwsCredentials credentials) {
            String displayName = credentials.getDisplayName();
            if (StringUtils.isNotBlank(displayName)) {
                return displayName;
            }

            String desc = credentials.getDescription();
            if (!StringUtils.isBlank(desc)) {
                desc = String.format("(%s...)",  StringUtils.truncate(desc.trim(), 10));//TODO fix
            }

            return String.format("%sxxx %s", credentials.getAWSAccessKeyId().substring(0, 5), desc);
        }
    }
}
