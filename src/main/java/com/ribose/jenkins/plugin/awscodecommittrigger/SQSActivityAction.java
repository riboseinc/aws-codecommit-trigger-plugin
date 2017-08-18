package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Action;
import hudson.model.Job;
import hudson.util.FormValidation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.http.HttpStatus;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RespondSuccess;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class SQSActivityAction implements Action {

    private static final Log log = Log.get(SQSActivityAction.class);
    private static final FastDateFormat df = FastDateFormat.getInstance("yyyyMMdd");

    private final transient Job job;
    private final transient File activityDir;

    public SQSActivityAction(Job job) {
        this.job = job;
        this.activityDir = new File(this.job.getRootDir(), ".activity");
        if (!this.activityDir.exists() && !this.activityDir.mkdirs()) {
            log.error("Unable to create trigger activity dir %s", this.activityDir.getPath());
        }

        log.debug("Activity dir %s is writeable? %s", this.activityDir.getPath(), this.activityDir.canWrite());
    }

    @Override
    public String getIconFileName() {
        return "clipboard.png";
    }

    @Override
    public String getDisplayName() {
        return "SQS Activity";
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String getUrlName() {
        return getDisplayName().toLowerCase().replace(" ", "-");
    }

    public File getActivityDir() {
        return activityDir;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public List<String> getLogNames() {
        List<String> names = new ArrayList<>();
        File[] files = this.activityDir.listFiles();
        Arrays.sort(files, NameFileComparator.NAME_REVERSE);
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }

    @RespondSuccess
    public void doDownload() throws IOException, ServletException {
        StaplerRequest request = Stapler.getCurrentRequest();
        StaplerResponse response = Stapler.getCurrentResponse();

        String name = request.getRestOfPath();
        File file = new File(this.activityDir.getPath() + "/" + name);
        if (file.exists()) {
            FileInputStream is = FileUtils.openInputStream(file);
            response.serveFile(request, is, 0L, 60000L, file.length(), name);
        } else {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            response.getOutputStream().println("sorry, we not found it " + name.replace("/", ""));
        }
    }

    public Job getJob() {
        return job;
    }

    public FormValidation doClear() {
        try {
            FileUtils.cleanDirectory(this.activityDir);
        } catch (IOException e) {
            return FormValidation.error(e, "Unable clear Activity");
        }
        return FormValidation.ok("Done. Please refresh the page.");
    }

    public File getActivityLogFile() {
        String date = df.format(new Date());
        String logPath = String.format("%s/activities-on-%s.log", this.getActivityDir().getPath(), date);
        return new File(logPath);
    }
}
