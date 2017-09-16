package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractFreestyleIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractFreestyleParamsIT extends AbstractFreestyleIT {
    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public ProjectFixture fixture;

    @Test
    public void shouldPassIt() throws Exception {
        this.mockAwsSqs.send(this.fixture.getSendBranches());
        this.submitAndAssertFixture(this.fixture);
    }
}
