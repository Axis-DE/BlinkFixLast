package axis.shiyan.wei.bluearchive.blinkfix.utils;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Video-player in LWJGL.
 *
 * @version 1.0.0
 *
 * @author Yuxing
 * @author HyperTap
 */
public class VideoPlayer {
    private FFmpegFrameGrabber frameGrabber;
    private TextureBinder textureBinder;

    private int frameLength;
    private int count; // frames counter
    private double frameRate; // 添加帧率字段

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    public final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private static final Logger logger = Logger.getLogger("VideoPlayer");

    /**
     * Start video-player object.
     *
     * @param videoFile your video file object
     */

    public void init(File videoFile) throws FFmpegFrameGrabber.Exception {
        frameGrabber = FFmpegFrameGrabber.createDefault(videoFile);
        frameGrabber.setPixelFormat(avutil.AV_PIX_FMT_RGB24);
        avutil.av_log_set_level(avutil.AV_LOG_QUIET); // Log level -> quiet

        textureBinder = new TextureBinder();

        count = 0;

        stopped.set(false);
        frameGrabber.start();
        frameLength = frameGrabber.getLengthInFrames();

        frameRate = frameGrabber.getFrameRate(); // 保存帧率

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledFuture = scheduler.scheduleAtFixedRate(this::doGetBuffer, 0, (long) (1000 / frameRate), TimeUnit.MILLISECONDS);
    }

    private void doGetBuffer() {
        if (paused.get() || stopped.get()) return;

        try {
            // 修复：添加更精确的结束检测
            Frame frame = frameGrabber.grabImage();
            if (frame != null) {
                if (frame.image != null) {
                    textureBinder.setBuffer((ByteBuffer) frame.image[0], frame.imageWidth, frame.imageHeight);

                    // 增加帧计数
                    count++;

                    // 检查是否到达视频末尾
                    if (count >= frameLength) {
                        resetVideo(); // 重置视频
                    }
                }
            }
        } catch (FFmpegFrameGrabber.Exception e) {
            logger.log(Level.WARNING, e.getMessage());

            // 如果发生异常，尝试重置视频
            if (!stopped.get()) {
                resetVideo();
            }
        }
    }

    // 新增方法：重置视频播放
    private void resetVideo() {
        try {
            count = 0;
            frameGrabber.setFrameNumber(0); // 重置到第一帧
            logger.info("Video reset to beginning for replay");
        } catch (FFmpegFrameGrabber.Exception e) {
            logger.log(Level.SEVERE, "Failed to reset video: " + e.getMessage());
        }
    }

    /**
     * Binding texture and play video frame.
     *
     * @param left rect left
     * @param top rect top
     * @param right rect right
     * @param bottom rect bottom
     */
    public void render(int left, int top, int right, int bottom) throws FrameGrabber.Exception {
        if (stopped.get() || paused.get()) return;


        int videoWidth = frameGrabber.getImageWidth();
        int videoHeight = frameGrabber.getImageHeight();
        float videoAspect = (float)videoWidth / videoHeight;


        int displayWidth = right - left;
        int displayHeight = bottom - top;
        float displayAspect = (float)displayWidth / displayHeight;


        float texLeft = 0f;
        float texRight = 1f;
        float texTop = 0f;
        float texBottom = 1f;

        if (videoAspect > displayAspect) {
            float scale = (float)displayHeight / videoHeight;
            float scaledWidth = videoWidth * scale;
            float overflow = (scaledWidth - displayWidth) / scale;
            texLeft = overflow / (2 * videoWidth);
            texRight = 1f - texLeft;
        } else {
            float scale = (float)displayWidth / videoWidth;
            float scaledHeight = videoHeight * scale;
            float overflow = (scaledHeight - displayHeight) / scale;
            texTop = overflow / (2 * videoHeight);
            texBottom = 1f - texTop;
        }

        textureBinder.bindTexture();

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        //填充整个显示区域
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(texLeft, texBottom);
        GL11.glVertex3f(left, bottom, 0);
        GL11.glTexCoord2f(texRight, texBottom);
        GL11.glVertex3f(right, bottom, 0);
        GL11.glTexCoord2f(texRight, texTop);
        GL11.glVertex3f(right, top, 0);
        GL11.glTexCoord2f(texLeft, texTop);
        GL11.glVertex3f(left, top, 0);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Stop play video frame.
     */
    public void stop() throws FFmpegFrameGrabber.Exception {
        if (stopped.get()) return;

        stopped.set(true);
        paused.set(false);

        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        textureBinder = null;

        count = 0;

        if (frameGrabber != null) {
            frameGrabber.stop();
            frameGrabber.release();
            frameGrabber = null;
        }
    }
}