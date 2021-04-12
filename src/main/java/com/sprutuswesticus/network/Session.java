package com.sprutuswesticus.network;

import java.util.function.BiConsumer;

import com.sprutuswesticus.App;
import com.sprutuswesticus.Board;
import com.sprutuswesticus.Update;

public abstract class Session {
    public static void startWorkerThread(Runnable worker) {
        Thread workerThread = new Thread(worker);
        workerThread.start();
        App.WORKER_THREADS.add(workerThread);
    }

    private final String ip;
    private final int port;
    private final String user;

    protected Board board = null;
    protected Thread workerThread = null;
    protected BiConsumer<Update, Board> updateCallback;

    public Session(String ip, int port, String user, BiConsumer<Update, Board> updateCallback) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.updateCallback = updateCallback;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    // Client-side: send Update to server
    // Server-side: add to threadsafe queue
    public abstract void enqueueUpdate(Update update);
}
