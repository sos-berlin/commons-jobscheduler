package com.sos.JSHelper.System;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/** source code from http://kra.lc/blog/2014/02/powershell-java-bridge/ */
public class PowerShell {

    private static final Logger LOGGER = Logger.getLogger(PowerShell.class);
    private static final int GARBAGE_INTERVAL = 60 * 1000;
    private static File powershell = null;
    private final Process process;
    private final PipedOutputStream outputPipe;
    private Thread inputThread;
    private Thread shutdownThread;
    private Timer garbageTimer;
    private File source;
    private PrintWriter sourceWriter;
    private PipedInputStream inputPipe;

    static {
        try {
            powershell = File.createTempFile(PowerShell.class.getSimpleName(), ".ps1");
            powershell.deleteOnExit();
            Files.copy(PowerShell.class.getResourceAsStream("PowerShell.ps1"), powershell.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (NullPointerException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public PowerShell() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        process = runtime.exec("PowerShell -NoLogo -File " + powershell.getAbsolutePath() + " -Source "
                + (source = File.createTempFile(PowerShell.class.getSimpleName() + "Source", ".ps1")));
        source.deleteOnExit();
        sourceWriter = new PrintWriter(source);
        runtime.addShutdownHook(shutdownThread = new Thread() {

            @Override
            public void run() {
                detatch();
            }
        });
        outputPipe = new PipedOutputStream(inputPipe = new PipedInputStream());
        inputThread = new Thread(PowerShell.class.getSimpleName() + "Thread") {

            @Override
            public void run() {
                InputStream stream = null;
                try {
                    stream = process.getInputStream();
                    int length;
                    byte[] buffer = new byte[128];
                    while ((length = stream.read(buffer)) != -1) {
                        outputPipe.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    //
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            //
                        }
                    }
                    detatch();
                }
            }
        };
        inputThread.start();
        garbageTimer = new Timer(PowerShell.class.getSimpleName() + "GarbageTimer", true);
        garbageTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (isDetatched()) {
                    this.cancel();
                } else if (source.length() > 131072) {
                    reattach();
                }
            }
        }, GARBAGE_INTERVAL, GARBAGE_INTERVAL);
    }

    public InputStream getInputStream() {
        return inputPipe;
    }

    public synchronized void execute(String command) throws IOException {
        if (isDetatched()) {
            return;
        }
        sourceWriter.println(command);
        if (sourceWriter.checkError()) {
            throw new IOException();
        }
    }

    protected synchronized void reattach() {
        try {
            File newSource = File.createTempFile(PowerShell.class.getSimpleName() + "Source", ".ps1");
            sourceWriter.println(". JavaInitialize(\"" + newSource.getAbsolutePath() + "\")");
            sourceWriter.close();
            sourceWriter = new PrintWriter(source = newSource);
        } catch (IOException e) {
            //
        }
    }

    public void detatch() {
        garbageTimer.cancel();
        Runtime.getRuntime().removeShutdownHook(shutdownThread);
        sourceWriter.close();
        try {
            process.destroy();
            process.waitFor();
        } catch (InterruptedException e) {
            //
        }
        source.delete();
        try {
            outputPipe.close();
        } catch (IOException e) {
            //
        }
    }

    public boolean isDetatched() {
        return !inputThread.isAlive() || !source.exists();
    }

    public static void main(String[] args) throws IOException {
        PowerShell powershell = new PowerShell();
        for (;;) {
            try {
                powershell.execute("[console]::beep(500,300)");
                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}