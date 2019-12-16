package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractFreestyleTestProject;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Parameterized.class)
public abstract class AbstractFreestyleParamsTestProject extends AbstractFreestyleTestProject {

    private static Logger log = LoggerFactory.getLogger(AbstractFreestyleParamsTestProject.class);

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public ProjectFixture fixture;

    @Test
    public void shouldPassIt() throws Exception {
        log.info("Running test fixture: {}", this.fixture.getName());
        this.mockAwsSqs.send(this.fixture.getSendBranches());
        this.submitAndAssertFixture(this.fixture);
    }
}
