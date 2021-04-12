package com.sprutuswesticus.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import com.sprutuswesticus.Board;
import com.sprutuswesticus.Update;

import javafx.application.Platform;

public class ClientSession extends Session {

    private LinkedBlockingQueue<Update> unsentUpdates;

    public ClientSession(NetworkConfig config, BiConsumer<Update, Board> updateCallback) {
        this(config.getIp(), config.getPort(), config.getUser(), updateCallback);
    }

    public ClientSession(String ip, int port, String user, BiConsumer<Update, Board> updateCallback) {
        super(ip, port, user, updateCallback);

        unsentUpdates = new LinkedBlockingQueue<>();

        Runnable clientWorker = makeWorker();
        Session.startWorkerThread(clientWorker);
    }

    /**
     * Worker sends locally enqueued updates and passes all confirmed updates to app
     * thread for processing
     */
    private Runnable makeWorker() {
        return () -> {
            try {
                // Establish connection
                Socket socket = new Socket(getIp(), getPort());
                ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());

                // Send user
                toServer.writeObject(getUser());

                // Read board
                final Board newBoard = (Board) fromServer.readObject();
                Platform.runLater(() -> {
                    board = newBoard;
                    updateCallback.accept(null, board);
                });

                boolean stopped = false;
                while (!stopped) {
                    // Send all locally enqueued updates
                    while (!unsentUpdates.isEmpty()) {
                        toServer.writeObject(unsentUpdates.poll());
                    }

                    // Apply all remotely confirmed updates
                    while (true) {
                        final Update confirmedUpdate = (Update) fromServer.readObject();
                        if (confirmedUpdate == null) {
                            break;
                        }
                        Platform.runLater(() -> {
                            updateCallback.accept(confirmedUpdate, board);
                        });
                    }

                    // TODO: stop condition
                }

                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace(System.err);
            }
        };
    }

    @Override
    public void enqueueUpdate(Update update) {
        unsentUpdates.add(update);
    }

}
