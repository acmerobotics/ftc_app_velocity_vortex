package com.acmerobotics.library.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ryan
 */

public class RobotDebugServer {

    private static RobotDebugServer server;

    public static RobotDebugServer getInstance() {
        return server;
    }

    private class ConnectionThread extends Thread {

        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                try {
                    Socket s = sock.accept();
                    Log.i("RobotDebugServer", "Accepted connection from " + s.getInetAddress().getCanonicalHostName());
                    synchronized (clientListLock) {
                        clients.add(new RobotDebugClient(s));
                    }
                    Log.i("RobotDebugServer", clients.size() + " clients currently open");
                } catch (SocketTimeoutException ste) {
                     // do nothing
                } catch (IOException e) {
                    Log.e("RobotDebugServer", "Failed to accept connection: " + e.getMessage());
                }
            }
        }

        public void terminate() {
            running = false;
        }
    }

    private class CleanupThread extends Thread {

        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                synchronized (clientListLock) {
                    for (int i = 0; i < clients.size();) {
                        if (!clients.get(i).isRunning()) {
                            clients.remove(i);
                            Log.i("RobotDebugServer", "Removed client (" + clients.size() + " left)");
                        } else {
                            i++;
                        }
                    }
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e("RobotDebugServer", e.getMessage());
                }
            }
        }

        public void terminate() {
            running = false;
        }
    }

    private int port;
    private ServerSocket sock;
    private List<RobotDebugClient> clients;
    private Lock clientListLock;
    private ConnectionThread connThread;
    private CleanupThread cleanupThread;

    public RobotDebugServer(int port) {
        this.port = port;
        this.clientListLock = new ReentrantLock();
        this.clients = new ArrayList<RobotDebugClient>();
        try {
            this.sock = new ServerSocket(port);
            this.sock.setSoTimeout(100);
            Log.i("RobotDebugServer", "Socket created successfully");
        } catch (IOException e) {
            Log.e("RobotDebugServer", "Failed to construct socket server: " + e.getMessage());
        }
        server = this;
    }

    public synchronized void listen() {
        if (connThread == null) {
            connThread = new ConnectionThread();
            connThread.start();
        }
        if (cleanupThread == null) {
            cleanupThread = new CleanupThread();
            cleanupThread.start();
        }
    }

    public void close() {
        if (connThread != null) {
            connThread.terminate();
            try {
                connThread.join();
            } catch (InterruptedException e) {
                Log.e("RobotDebugServer", e.getMessage());
            }
        }
        if (cleanupThread != null) {
            cleanupThread.terminate();
            try {
                cleanupThread.join();
            } catch (InterruptedException e) {
                Log.e("RobotDebugServer", e.getMessage());
            }
        }
        try {
            sock.close();
        } catch (IOException e) {
            Log.e("RobotDebugServer", e.getMessage());
        }
        synchronized (clientListLock) {
            for (RobotDebugClient client : clients) {
                client.close();
            }
        }
    }

    public synchronized void send(JSONObject json) {
        synchronized (clientListLock) {
            for (RobotDebugClient client : clients) {
                client.send(json);
            }
        }
    }
}
