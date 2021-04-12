package com.sprutuswesticus.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import com.sprutuswesticus.Board;
import com.sprutuswesticus.Update;

import javafx.application.Platform;

public class ServerSession extends Session {

    private LinkedBlockingQueue<Update> pendingUpdates;

    public ServerSession(NetworkConfig config, BiConsumer<Update, Board> updateCallback, Board board) {
        this(config.getIp(), config.getPort(), config.getUser(), updateCallback, board);
    }

    public ServerSession(String ip, int port, String user, BiConsumer<Update, Board> updateCallback, Board board) {
        super(ip, port, user, updateCallback); // TODO: ignores ip
        super.board = board;

        pendingUpdates = new LinkedBlockingQueue<>();

        Runnable serverWorker = makeWorker();
        Session.startWorkerThread(serverWorker);
    }

    /**
     * Worker reads partner's updates, sends all updates (including host's) to
     * partner, and passes all updates to app thread for processing
     */
    private Runnable makeWorker() {
        return () -> {
            try {
                // Establish connection
                ServerSocket ss = new ServerSocket(getPort());
                Socket partnerCxn = ss.accept();
                ObjectInputStream fromPartner = new ObjectInputStream(partnerCxn.getInputStream());
                ObjectOutputStream toPartner = new ObjectOutputStream(partnerCxn.getOutputStream());

                // Read partner user
                String partnerUser = (String) fromPartner.readObject();
                System.out.format("Partner %s has joined the session.\n", partnerUser);

                // Send board
                toPartner.writeObject(board);

                boolean stopped = false;
                while (!stopped) {
                    // Read all partner updates
                    Update partnerUpdate;
                    while ((partnerUpdate = (Update) fromPartner.readObject()) != null) {
                        pendingUpdates.add(partnerUpdate);
                    }

                    // Apply and send all pending updates
                    while (!pendingUpdates.isEmpty()) {
                        final Update confirmedUpdate = pendingUpdates.poll();
                        toPartner.writeObject(confirmedUpdate);

                        Platform.runLater(() -> {
                            updateCallback.accept(confirmedUpdate, board);
                        });
                    }
                }

                partnerCxn.close();
                ss.close();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println(e.getStackTrace());
            }
        };
    }

    @Override
    public void enqueueUpdate(Update update) {
        pendingUpdates.add(update);
    }

}
