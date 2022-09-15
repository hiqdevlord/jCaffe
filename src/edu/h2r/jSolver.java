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
        internalPtr = createSolver(solverFile);
        // Parse the network definition file name from the solver file
        String basePath = solverFile.split("/(?=[^/]+$)")[0];
        String networkFile = null;
        inputScale = -1f;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(solverFile)));
            String line;
            while ((line = br.readLine()) != null)
                if (line.trim().startsWith("net:")) {
                    String tmp = line.trim().substring(line.trim().indexOf("net:") + 4).trim();
                    networkFile = tmp.substring(1, tmp.length() - 1);
                    if (!networkFile.startsWith("/"))
                        networkFile = basePath + "/" + networkFile;
                    break;
                }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse the input scale from the network definition file
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(networkFile)));
            String line;
            while ((line = br.readLine()) != null)
                if (line.trim().startsWith("scale:")) {
                    String tmp = line.trim().substring(line.trim().indexOf("scale:") + 6).trim();
                    inputScale = Float.valueOf(tmp);
                    break;
                }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputScale == -1)
            throw new IllegalArgumentException("Couldn't parse the 'scale' parameter from file " + networkFile +
                    " (parsed from file " + solverFile + ")");

        net = new jNet(getNetPointer(), inputScale);
    }

    /**
     * Returns the underlying neural network {@link jNet} object.
     */
    public jNet getNet() {
        return net;
    }

    public native void train();

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

    private native void _dispose();

    private native long getNetPointer();

    private native long createSolver(String solverFile);

    static {
        File jar = new File(jNet.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        System.load(jar.getParentFile().toURI().resolve("libcaffe_jni.so").getPath());
    }
}
