package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.pipeline;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;


@Ignore
@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/29")
public class JenkinsIT extends AbstractJenkinsIT {

    @Test
    public void shouldPassIt() throws IOException {
//        WorkflowJob project = this.jenkinsRule.getInstance().createProject(WorkflowJob.class, "workflow");
//        String script = IOUtils.toString(Utils.getResource(JenkinsIT.class, "Jenkinsfile"));
//        TestFlowExecutionOwner owner = new TestFlowExecutionOwner();
//        CpsFlowExecution ex = new CpsFlowExecution(script, true, owner);
//        owner.setCps(ex);
//        ex.start();
    }

//    public static class TestFlowExecutionOwner extends FlowExecutionOwner {
//
//        private FlowExecution cps;
//
////        public TestFlowExecutionOwner(FlowExecution cps) {
////            this.cps = cps;
////        }
//
//
//        public void setCps(FlowExecution cps) {
//            this.cps = cps;
//        }
//
//        @Nonnull
//        @Override
//        public FlowExecution get() throws IOException {
//            return this.cps;
//        }
//
//        @Override
//        public File getRootDir() throws IOException {
//            return new File("./");
//        }
//
//        @Override
//        public Queue.Executable getExecutable() throws IOException {
//            return null;
//        }
//
//        @Override
//        public String getUrl() throws IOException {
//            return "";
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            return o instanceof TestFlowExecutionOwner;
//        }
//
//        @Override
//        public int hashCode() {
//            return 0;
//        }
//    }
}
