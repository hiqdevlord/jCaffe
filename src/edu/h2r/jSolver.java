package edu.h2r;

import java.io.*;

/**
 * Created by gabe on 4/17/15.
 */
public class jSolver {

    private long internalPtr;
    private String solverFile;
    private float inputScale;
    private jNet net;

    public jSolver(String solverFile) {
        this.solverFile = solverFile;
        // Note: this function sets the field inputScale.
        internalPtr = createSolver(solverFile);
        net = new jNet(getNetPointer(), inputScale);
    }

    /**
     * Returns the underlying neural network {@link jNet} object.
     */
    public jNet getNet() {
        return net;
    }

    /**
     * Resets the underlying Caffe neural network.
     */
    public void reset() {
        net.dispose();
        this.dispose();
        internalPtr = createSolver(solverFile);
        net = new jNet(getNetPointer(), inputScale);
    }

    /**
     * Deletes the underlying Caffe neural network.
     */
    public void dispose() {
        net.dispose();
        _dispose();
    }
    
    /**
     * Trains the underlying Caffe neural network by doing a forward and backward pass on it.
     */
    public native void train();

    /**
     * Trains the underlying Caffe neural network for one step only, by doing a forward and backward pass on it.
     */
    public native void trainOneStep();

    /**
     * Sets Caffe log level.
     * @param log_level log level (levels INFO, WARNING, ERROR, and FATAL are 0, 1, 2, and 3, respectively)
     */
    public native void setLogLevel(int log_level);

    private native void _dispose();

    private native long getNetPointer();

    private native long createSolver(String solverFile);

    static {
        File jar = new File(jNet.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        System.load(jar.getParentFile().toURI().resolve("libcaffe_jni.so").getPath());
    }
}
