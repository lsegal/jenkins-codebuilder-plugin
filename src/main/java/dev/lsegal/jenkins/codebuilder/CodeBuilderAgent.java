package dev.lsegal.jenkins.codebuilder;

import java.io.IOException;
import java.util.Collections;

import javax.annotation.Nonnull;

import com.amazonaws.services.codebuild.model.ResourceNotFoundException;
import com.amazonaws.services.codebuild.model.StopBuildRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.ComputerLauncher;

class CodeBuilderAgent extends AbstractCloudSlave {
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeBuilderAgent.class);
  private static final long serialVersionUID = -6722929807051421839L;
  private final CodeBuilderCloud cloud;

  /**
   * Creates a new CodeBuilderAgent node that provisions a
   * {@link CodeBuilderComputer}.
   */
  public CodeBuilderAgent(@Nonnull CodeBuilderCloud cloud, @Nonnull String name, @Nonnull ComputerLauncher launcher)
      throws Descriptor.FormException, IOException {
    super(name, "AWS CodeBuild Agent", "/build", 1, Mode.NORMAL, cloud.getLabel(), launcher,
        new CloudRetentionStrategy(cloud.getAgentTimeout() / 60 + 1), Collections.emptyList());
    this.cloud = cloud;
  }

  /**
   * Get the cloud instance associated with this builder
   */
  public CodeBuilderCloud getCloud() {
    return cloud;
  }

  @Override
  public AbstractCloudComputer<CodeBuilderAgent> createComputer() {
    return new CodeBuilderComputer(this);
  }

  @Override
  protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
    listener.getLogger().println("[CodeBuilder]: Terminating agent: " + getDisplayName());

    if (getLauncher() instanceof CodeBuilderLauncher) {
      String buildId = ((CodeBuilderComputer) getComputer()).getBuildId();
      if (StringUtils.isBlank(buildId)) {
        return;
      }

      try {
        LOGGER.info("[CodeBuilder]: Stopping build ID: {}", buildId);
        cloud.getClient().stopBuild(new StopBuildRequest().withId(buildId));
      } catch (ResourceNotFoundException e) {
        // this is fine. really.
      } catch (Exception e) {
        LOGGER.error("[CodeBuilder]: Failed to stop build ID: {}", buildId, e);
      }
    }
  }
}
