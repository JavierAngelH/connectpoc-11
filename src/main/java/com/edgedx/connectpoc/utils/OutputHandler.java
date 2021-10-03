package com.edgedx.connectpoc.utils;

import java.io.*;

public class OutputHandler extends Thread {
    private final StringBuilder buf = new StringBuilder();
    private final BufferedReader in;

    public OutputHandler(InputStream in, String encoding)
            throws UnsupportedEncodingException {
        this.in = new BufferedReader(new InputStreamReader(
                in, encoding == null ? "UTF-8" : encoding));
        setDaemon(true);
        start();
    }

    public String getText() {
        synchronized (buf) {
            return buf.toString();
        }
    }

    @Override
    public void run() {
        // Reading process output
        try {
            String s = in.readLine();
            while (s != null) {
                synchronized (buf) {
                    buf.append(s);
                    buf.append('\n');
                }
                s = in.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
