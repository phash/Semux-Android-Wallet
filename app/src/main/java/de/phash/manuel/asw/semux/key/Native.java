/**
 * Copyright (c) 2017-2018 The Semux Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package de.phash.manuel.asw.semux.key;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Semux crypto native implementation
 */
public class Native {
    protected static File nativeDir;
    protected static boolean enabled = false;
    private static String libraryPath = null;

    // initialize library when the class loads
    static {
        init();
    }

    /**
     * Initializes the native libraries
     */
    public static void init() {

        enabled = loadLibrary("cpplibsodium.so.23") && loadLibrary("/native/linux64/libcrypto.so");

        //enabled = true;
    }

    /**
     * Loads a library file from bundled resource.
     *
     * @param resource
     * @return
     */
    protected static boolean loadLibrary(String resource) {
        try {
            if (nativeDir == null) {
                nativeDir = Files.createTempDirectory("native").toFile();
                nativeDir.deleteOnExit();
            }

            String name = resource.contains("/") ? resource.substring(resource.lastIndexOf('/') + 1) : resource;
            File file = new File(nativeDir, name);

            if (!file.exists()) {
                InputStream in = Native.class.getResourceAsStream(resource); // null pointer exception
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                for (int c; (c = in.read()) != -1; ) {
                    out.write(c);
                }
                out.close();
                in.close();
            }

            System.load(file.getAbsolutePath());
            return true;
        } catch (Exception | UnsatisfiedLinkError e) {
            return false;
        }
    }

    /**
     * Returns whether the native library is enabled.
     *
     * @return
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Disables native implementation.
     */
    public static void disable() {
        enabled = false;
    }

    /**
     * Enables native implementation.
     */
    public static void enable() {
        init();
    }


}
