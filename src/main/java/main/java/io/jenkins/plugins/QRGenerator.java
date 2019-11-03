package main.java.io.jenkins.plugins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import io.nayuki.qrcodegen.*;

public class QRGenerator extends Builder implements SimpleBuildStep {

    private String path;
    private String data;

    @DataBoundConstructor
    public QRGenerator(@Nonnull String path,@Nonnull String data){
        this.path = path;
        this.data = data;
    }

    public String getPath(){
        return path;
    }
    public String getData(){
        return data;
    }

    @DataBoundSetter
    public void setPath(String path){
        this.path = path;
    }

    @DataBoundSetter
    public void setData(String data){
        this.data = data;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        EnvVars env = run.getEnvironment(taskListener);
        final String path = env.expand(this.path);
        final String data = env.expand(this.data);

        taskListener.getLogger().println("Generating QR code for " + data);
        final File absolutePath = new File(run.getRootDir(),path);
        QrCode qr0 = QrCode.encodeText(data, QrCode.Ecc.MEDIUM);
        BufferedImage img = qr0.toImage(4, 10);
        ImageIO.write(img, "png", absolutePath);
        FilePath qrfp_source = new FilePath(absolutePath);
        FilePath qrfp_dest = filePath.child(path);
        qrfp_dest.copyFrom(qrfp_source);
        absolutePath.delete();
        taskListener.getLogger().println("QR Code saved at " + qrfp_dest);
    }

    @Symbol("generateqrcode")
    @Extension
    public static final class DiscriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}

