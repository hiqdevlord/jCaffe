package edu.h2r;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

/**
 * TODO: 1) Add method to query number of nodes in layer
 * TODO: 2) Add c++ error handling
 * TODO: 3) Optimize image operations
 * TODO: 4) Have an internal thread create & destroy c++ stuff
 *
 */
public class JNet implements Disposable {

    private final float inputScale;
    private long internalPtr;

    public JNet(String param_file, String pretrained_param_file, float inputScale) {
        internalPtr = createNet(param_file, pretrained_param_file);
        this.inputScale = inputScale;
    }

    public float[] forward(BufferedImage image) throws IllegalArgumentException {
        if (image.getWidth() != getInputWidth() || image.getHeight() != getInputHeight())
            throw new IllegalArgumentException("Image does not have correct dimensions");

        String[] layerNames = getLayerNames();
        return forwardTo(imageToArray(image), layerNames[layerNames.length - 1]);
    }

    public float[] forwardTo(BufferedImage image, String toLayerName) throws IllegalArgumentException {
        if (image.getWidth() != getInputWidth() || image.getHeight() != getInputHeight())
            throw new IllegalArgumentException("Image does not have correct dimensions");

        if (!hasLayer(toLayerName))
            throw new IllegalArgumentException("Layer does not exist");

        return forwardTo(imageToArray(image), toLayerName);
    }

    public static float[] imageToArray(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        float[] pixels = new float[ h * w];
        image.getData().getPixels(0, 0, w, h, pixels);
        return pixels;
    }

    public static BufferedImage imageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        raster.setPixels(0, 0, width, height, pixels);
        return image;
    }

    public static int[] grayToRGB(float[] grayPixels) {
        int[] rgbPixels = new int[grayPixels.length];
        for (int i = 0; i < grayPixels.length; i++) {
            int grayPixel = (int) (grayPixels[i] * 255f);
            rgbPixels[i] = -16777216;  // alpha channel
            rgbPixels[i] += (grayPixel & 0xFF);
            rgbPixels[i] += (grayPixel & 0xFF) << 8;
            rgbPixels[i] += (grayPixel & 0xFF) << 16;
        }
        return rgbPixels;
    }

    public static int[] grayToRGB(int[] grayPixels) {
        int[] rgbPixels = new int[grayPixels.length];
        for (int i = 0; i < grayPixels.length; i++) {
            rgbPixels[i] = -16777216;  // alpha channel
            rgbPixels[i] += (grayPixels[i] & 0xFF);
            rgbPixels[i] += (grayPixels[i] & 0xFF) << 8;
            rgbPixels[i] += (grayPixels[i] & 0xFF) << 16;
        }
        return rgbPixels;
    }

    // Methods implemented in C++

    public native long createNet(String param_file, String pretrained_param_file);

    public native void dispose();

    public native float[] forwardTo(float[] input, String toLayerName);

    public native String[] getLayerNames();

    public native int getNodeCount(String layerName);  // TODO: What's this for?

    private native boolean hasLayer(String layerName);

    private native int getInputHeight();

    private native int getInputWidth();


    static {
        File jar = new File(JNet.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File lib = new File(jar.getParentFile().toURI().resolve("libcaffe.so").getPath());
        System.out.println(lib.getPath() + " exists: " + lib.exists());
        System.load(jar.getParentFile().toURI().resolve("libcaffe.so").getPath());
    }
}
