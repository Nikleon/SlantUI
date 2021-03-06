package com.sprutuswesticus.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
        final int numCxns = 3;
        return () -> {
            try {
                // Establish connection
                ServerSocket ss = new ServerSocket(getPort());
                Socket[] partnerCxns = new Socket[numCxns];
                ObjectOutputStream[] toPartners = new ObjectOutputStream[numCxns];
                ObjectInputStream[] fromPartners = new ObjectInputStream[numCxns];
                for (int i = 0; i < numCxns; i++) {
                    partnerCxns[i] = ss.accept();
                    toPartners[i] = new ObjectOutputStream(partnerCxns[i].getOutputStream());
                    fromPartners[i] = new ObjectInputStream(partnerCxns[i].getInputStream());

                    // Read partner user
                    String partnerUser = (String) fromPartners[i].readObject();
                    System.out.format("Partner %s has joined the session.\n", partnerUser);

                    // Send board
                    toPartners[i].writeObject(board);
                }

                boolean stopped = false;
                while (!stopped) {
                    for (ObjectInputStream fromPartner : fromPartners) {
                        Runnable subloop = () -> {
                            try {
                                // Read all partner updates
                                while (true) {
                                    Update partnerUpdate = (Update) fromPartner.readObject();
                                    pendingUpdates.add(partnerUpdate);
                                }
                            } catch (SocketException e) {
                                this.stop();
                            } catch (ClassNotFoundException | IOException e) {
                                e.printStackTrace(System.err);
                            }
                        };
                        Session.startWorkerThread(subloop);
                    }

                    // Apply and send all pending updates
                    while (true) {
                        if (pendingUpdates.isEmpty()) {
                            continue;
                        }
                        final Update confirmedUpdate = pendingUpdates.poll();
                        for (ObjectOutputStream toPartner : toPartners) {
                            toPartner.writeObject(confirmedUpdate);
                        }

                        Platform.runLater(() -> {
                            updateCallback.accept(confirmedUpdate, board);
                        });
                    }
                }

                for (int i = 0; i < numCxns; i++) {
                    partnerCxns[i].close();
                }
                ss.close();
            } catch (SocketException e) {
                this.stop();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace(System.err);
            }
        };
    }

    @Override
    public void enqueueUpdate(Update update) {
        pendingUpdates.add(update);
    }

}
