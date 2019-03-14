package dev.lsegal.jenkins.codebuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.slaves.AbstractCloudComputer;
import jenkins.model.Jenkins;

public class CodeBuilderComputer extends AbstractCloudComputer<CodeBuilderAgent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeBuilderComputer.class);
  private String buildId;

  @Nonnull
  private final CodeBuilderCloud cloud;

  public CodeBuilderComputer(CodeBuilderAgent agent) {
    super(agent);
    this.cloud = agent.getCloud();
  }

  public String getBuildId() {
    return buildId;
  }

  /* package */ void setBuildId(String buildId) {
    this.buildId = buildId;
  }

  public String getBuildUrl() {
    try {
      return String.format("https://%s.console.aws.amazon.com/codesuite/codebuild/projects/%s/build/%s",
          cloud.getRegion(), cloud.getProjectName(), URLEncoder.encode(buildId, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return buildId;
    }
  }

  @Override
  public void taskAccepted(Executor executor, Queue.Task task) {
    super.taskAccepted(executor, task);
    LOGGER.info("[CodeBuilder]: [{}]: Task in job '{}' accepted", this, task.getFullDisplayName());
  }

  @Override
  public void taskCompleted(Executor executor, Queue.Task task, long durationMS) {
    super.taskCompleted(executor, task, durationMS);
    LOGGER.info("[CodeBuilder]: [{}]: Task in job '{}' completed in {}ms", this, task.getFullDisplayName(), durationMS);
    gracefulShutdown();
  }

  @Override
  public void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {
    super.taskCompletedWithProblems(executor, task, durationMS, problems);
    LOGGER.error("[CodeBuilder]: [{}]: Task in job '{}' completed with problems in {}ms", this,
        task.getFullDisplayName(), durationMS, problems);
    gracefulShutdown();
  }

  @Override
  public String toString() {
    return String.format("name: %s buildID: %s", getName(), getBuildId());
  }

  private void gracefulShutdown() {
    setAcceptingTasks(false);

    Computer.threadPoolForRemoting.submit(() -> {
      LOGGER.info("[CodeBuilder]: [{}]: Terminating agent after task.", this);
      try {
        Thread.sleep(500);
        Jenkins.getInstance().removeNode(getNode());
      } catch (Exception e) {
        LOGGER.info("[CodeBuilder]: [{}]: Termination error: {}", this, e.getClass());
      }
      return null;
    });
  }
}
