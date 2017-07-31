package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import hudson.model.Action;
import hudson.model.Job;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SQSActivityAction implements Action {

    private static final Log log = Log.get(SQSActivityAction.class);

    private final transient Job job;
    private final transient File activityDir;

//    private static final String SQS_LOG_NAME = "sqs-activity.log";
    private static final Long DEFAULT_BIG_SIZE = 10L;

    public SQSActivityAction(Job job) {
        this.job = job;
        this.activityDir = new File(this.job.getRootDir(), ".activity");
        if (!this.activityDir.exists() && !this.activityDir.mkdirs()) {
            log.error("Unable to create trigger activity dir %s", this.activityDir.getPath());
        }

        log.info("Activity dir %s is writeable? %s", this.activityDir.getPath(), this.activityDir.canWrite());
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
    public String getUrlName() {
        return "SQSActivity";
    }

    public File getActivityDir() {
        return activityDir;
    }

    public List<String> getLogNames() {
        List<String> names = new ArrayList<>();
        File[] files = this.activityDir.listFiles();
        Arrays.sort(files, NameFileComparator.NAME_REVERSE);
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }

//    public String getActivityDirPath() throws IOException {
//        return this.activityDir.getCanonicalPath();
//    }

//    public void doRaw() throws ServletException, IOException {
//        StaplerRequest request = Stapler.getCurrentRequest();
//        StaplerResponse response = Stapler.getCurrentResponse();
//        FileInputStream is = FileUtils.openInputStream(this.activityDir);
//        response.serveFile(request, is, 0L, 60000L, this.activityDir.length(), SQS_LOG_NAME);
//    }

    public boolean isBig(File log) {
        long sizeInMB = log.length() / 1048576L; // 1048576 B = 1 MB
        return sizeInMB > DEFAULT_BIG_SIZE;
    }

//    public String getLogSize() {
//        return FileUtils.byteCountToDisplaySize(this.activityDir.length());
//    }

    public String readLog(String name) throws IOException {
        String path = this.activityDir.getPath() + "/" + name;
        return FileUtils.readFileToString(new File(path), "UTF-8");
    }

//    public FormValidation doClear() {
//        try {
//            FileUtils.write(new File(this.sqsLogPath), "");
//        } catch (IOException e) {
//            return FormValidation.error(e, "Unable clear Activity");
//        }
//        return FormValidation.ok("Done. Please refresh the page.");
//    }

//    public File getSqsLogFile() {
//        return activityDir;
//    }
}
