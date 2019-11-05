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
        final String expandedPath = env.expand(this.path);
        final String expandedData = env.expand(this.data);
        final String workspace = env.expand("${WORKSPACE}");

        taskListener.getLogger().println("Generating QR code for " + expandedData);
        String absolutePathStr = (expandedPath.contains(workspace) || new File(expandedPath).isAbsolute()) ? expandedPath : workspace + File.separator + expandedPath;
        absolutePathStr = absolutePathStr.replace("\\", File.separator);
        absolutePathStr = absolutePathStr.replace("/", File.separator);

        taskListener.getLogger().println("Saving to "+ absolutePathStr);

        File tmpImg = File.createTempFile("img","png");

        FilePath qrfpSource = new FilePath(tmpImg);

        QrCode qr0 = QrCode.encodeText(expandedData, QrCode.Ecc.MEDIUM);
        BufferedImage img = qr0.toImage(4, 10);
        ImageIO.setUseCache(false);
        ImageIO.write(img, "png", tmpImg);
        FilePath qrfpDest = filePath.child(expandedPath);
        qrfpDest.copyFrom(qrfpSource);
        tmpImg.delete();
        taskListener.getLogger().println("QR Code saved at " + qrfpDest);
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

