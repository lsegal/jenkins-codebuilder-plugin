package dev.lsegal.jenkins.codebuilder;

import java.io.IOException;
import java.util.Arrays;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.model.Jenkins;

public final class CodeBuilderLogger extends BuildWrapper {
  @DataBoundConstructor
  public CodeBuilderLogger() {
    super();
  }

  @Extension
  public static class DescriptorImpl extends BuildWrapperDescriptor {
    @Override
    public String getDisplayName() {
      return Messages.loggerName();
    }

    @Override
    public boolean isApplicable(final AbstractProject<?, ?> item) {
      return true;
    }
  }

  @Override
  public Environment setUp(@SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher, BuildListener listener)
      throws IOException, InterruptedException {
    Computer cpu = Arrays.asList(Jenkins.get().getComputers()).stream()
        .filter(c -> c.getChannel() == launcher.getChannel()).findFirst().get();
    if (cpu instanceof CodeBuilderComputer) {
      CodeBuilderComputer cbCpu = (CodeBuilderComputer) cpu;
      listener.getLogger().print("[CodeBuilder]: " + Messages.loggerStarted() + ": ");
      listener.hyperlink(cbCpu.getBuildUrl(), cbCpu.getBuildId());
      listener.getLogger().println();
    }
    return new Environment() {
    };
  }
}
