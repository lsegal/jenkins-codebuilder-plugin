package dev.lsegal.jenkins.codebuilder;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.codebuild.model.StartBuildRequest;
import com.amazonaws.services.codebuild.model.StartBuildResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.TaskListener;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.SlaveComputer;
import hudson.util.StreamTaskListener;
import jenkins.model.Jenkins;

public class CodeBuilderLauncher extends JNLPLauncher {
  private static final int sleepMs = 500;
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeBuilderLauncher.class);

  private final CodeBuilderCloud cloud;
  private boolean launched;

  public CodeBuilderLauncher(CodeBuilderCloud cloud) {
    super(true);
    this.cloud = cloud;
  }

  @Override
  public boolean isLaunchSupported() {
    return !launched;
  }

  @Override
  public void launch(SlaveComputer computer, TaskListener listener) {
    this.launched = false;
    LOGGER.info("[CodeBuilder]: Launching {} with {}", computer, listener);
    CodeBuilderComputer cbcpu = (CodeBuilderComputer) computer;
    StartBuildRequest req = new StartBuildRequest().withProjectName(cloud.getProjectName())
        .withImageOverride(cloud.getJnlpImage()).withPrivilegedModeOverride(true)
        .withComputeTypeOverride(cloud.getComputeType()).withBuildspecOverride(buildspec(computer));

    try {
      StartBuildResult res = cloud.getClient().startBuild(req);
      String buildId = res.getBuild().getId();
      cbcpu.setBuildId(buildId);

      LOGGER.info("[CodeBuilder]: Waiting for agent '{}' to connect to build ID: {}...", computer, buildId);
      for (int i = 0; i < cloud.getAgentTimeout() * (1000 / sleepMs); i++) {
        if (computer.isOnline() && computer.isAcceptingTasks()) {
          LOGGER.info("[CodeBuilder]: Agent '{}' connected to build ID: {}.", computer, buildId);
          launched = true;
          return;
        }
        Thread.sleep(sleepMs);
      }
      throw new TimeoutException(
          "Timed out while waiting for agent " + computer.getNode() + " to start for build ID: " + buildId);

    } catch (Exception e) {
      cbcpu.setBuildId(null);
      LOGGER.error("[CodeBuilder]: Exception while starting build: {}", e.getMessage(), e);
      listener.fatalError("Exception while starting build: %s", e.getMessage());

      if (computer.getNode() instanceof CodeBuilderAgent) {
        try {
          Jenkins.get().removeNode(computer.getNode());
        } catch (IOException e1) {
          LOGGER.error("Failed to terminate agent: {}", computer.getNode().getDisplayName(), e);
        }
      }
    }
  }

  @Override
  public void beforeDisconnect(SlaveComputer computer, StreamTaskListener listener) {
    ((CodeBuilderComputer) computer).setBuildId(null);
  }

  private String buildspec(SlaveComputer computer) {
    String cmd = String.format("jenkins-agent -noreconnect -workDir \"$CODEBUILD_SRC_DIR\" -url \"%s\" \"%s\" \"%s\"",
        cloud.getJenkinsUrl(), computer.getJnlpMac(), computer.getNode().getDisplayName());
    StringBuilder builder = new StringBuilder();
    builder.append("version: 0.2\n");
    builder.append("phases:\n");
    builder.append("  pre_build:\n");
    builder.append("    commands:\n");
    builder.append(
        "      - nohup /usr/local/bin/dockerd --host=unix:///var/run/docker.sock --host=tcp://127.0.0.1:2375 --storage-driver=overlay2 >/dev/null &\n");
    builder.append("      - until docker info; do echo .; sleep 1; done\n");
    builder.append("  build:\n");
    builder.append("    commands:\n");
    builder.append("      - " + cmd + " || exit 0\n");

    return builder.toString();
  }
}
