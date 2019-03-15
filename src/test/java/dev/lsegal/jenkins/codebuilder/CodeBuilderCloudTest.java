package dev.lsegal.jenkins.codebuilder;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProvisioner.PlannedNode;

public class CodeBuilderCloudTest {
  @Rule
  public JenkinsRule j = new JenkinsRule();

  @Test
  public void canProvision_with_no_label() throws Exception {
    CodeBuilderCloud c = new CodeBuilderCloud(null, "", "project", "region");

    Assert.assertTrue(c.canProvision(new LabelAtom("codebuilder")));
    Assert.assertTrue(c.canProvision(new LabelAtom("")));
    Assert.assertTrue(c.canProvision(null));
  }

  @Test
  public void canProvision_with_label() throws Exception {
    CodeBuilderCloud c = new CodeBuilderCloud(null, "", "project", "region");
    c.setLabel("codebuilder");

    Assert.assertTrue(c.canProvision(new LabelAtom("codebuilder")));
  }

  @Test
  public void canProvision_returns_false_with_label_mismatch() throws Exception {
    CodeBuilderCloud c = new CodeBuilderCloud(null, "", "project", "region");
    c.setLabel("not_codebuilder");

    Assert.assertFalse(c.canProvision(new LabelAtom("codebuilder")));
  }

  @Test
  public void provisions() throws Exception {
    CodeBuilderCloud c = new CodeBuilderCloud(null, "", "project", "region");
    Collection<PlannedNode> plannedNodes = c.provision(null, 1);

    Assert.assertEquals(1, plannedNodes.size());
  }
}
