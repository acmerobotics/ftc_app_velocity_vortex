package com.acmerobotics.library.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Ryan
 */

public class RobotDebugClient {

    public class ReadThread extends Thread {
        @Override
        public void run() {
            while (running) {
                try {
                    String json = inputStream.readLine();
                    if (json == null) continue;
                    JSONObject obj = new JSONObject(json);
                    Log.i("RobotDebugClient", "Read JSON object with " + obj.length() + " size");
                } catch (IOException e) {
                    Log.e("RobotDebugClient", "Failed to read line: " + e.getMessage());
                    running = false;
                    break;
                } catch (JSONException e) {
                    Log.e("RobotDebugClient", "Failed to parse packet data: " + e.getMessage());
                }
            }
        }
    }

    public class WriteThread extends Thread {
        @Override
        public void run() {
            while (running) {
                try {
                    JSONObject obj = writeQueue.poll(250, TimeUnit.MILLISECONDS);
                    if (obj == null) continue;
                    String jsonString = obj.toString() + "\n";
                    outputStream.write(jsonString.getBytes());
                    outputStream.flush();
                    Log.i("RobotDebugClient", "Wrote object to output");
                } catch (InterruptedException e) {
                    Log.e("RobotDebugClient", "Deque poll failed: " + e.getMessage());
                } catch (IOException e) {
                    Log.e("RobotDebugClient", "Packet write failed: " + e.getMessage());
                    running = false;
                    break;
                }
            }
        }
    }

    private Socket sock;
    private boolean running;
    private ReadThread readThread;
    private WriteThread writeThread;
    private OutputStream outputStream;
    private BufferedReader inputStream;
    private BlockingQueue<JSONObject> writeQueue;

    public RobotDebugClient(Socket socket) {
        this.sock = socket;
        this.running = true;
        this.writeQueue = new ArrayBlockingQueue<>(30);

        try {
            outputStream = sock.getOutputStream();
            inputStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            Log.e("RobotDebugClient", e.getMessage());
        }

        this.readThread = new ReadThread();
        this.readThread.start();

        this.writeThread = new WriteThread();
        this.writeThread.start();
    }

    public synchronized void close() {
        this.running = false;
        if (readThread != null) {
            try {
                readThread.join();
            } catch (InterruptedException e) {
                Log.e("RobotDebugClient", e.getMessage());
            }
        }
        if (writeThread != null) {
            try {
                writeThread.join();
            } catch (InterruptedException e) {
                Log.e("RobotDebugClient", e.getMessage());
            }
        }
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
                Log.e("RobotDebugClient", e.getMessage());
            }
        }
    }

    public synchronized void send(JSONObject json) {
        writeQueue.offer(json);
    }

    public boolean isRunning() {
        return running;
    }

}
