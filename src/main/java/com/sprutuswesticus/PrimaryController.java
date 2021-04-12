package com.sprutuswesticus;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.sprutuswesticus.network.ClientSession;
import com.sprutuswesticus.network.NetworkConfig;
import com.sprutuswesticus.network.ServerSession;
import com.sprutuswesticus.network.Session;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Window;

public class PrimaryController {

    private Session session;
    private Board board;
    private Canvas canvas;

    @FXML
    private BorderPane root;

    @FXML
    private MenuItem gameLoadSpecBtn;

    @FXML
    private MenuItem networkHostBtn;

    @FXML
    private MenuItem networkJoinBtn;

    @FXML
    private MenuItem networkLeaveBtn;

    public PrimaryController() {
        session = null;
        board = new Board();
    }

    @FXML
    private void initialize() {
        gameLoadSpecBtn.setOnAction(evt -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New game from spec...");
            dialog.setHeaderText("");
            dialog.setContentText("Spec:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(this::loadBoardFromSpec);
        });

        networkHostBtn.setOnAction(evt -> {
            HostDialog dialog = new HostDialog();
            Optional<NetworkConfig> result = dialog.showAndWait();
            result.filter(networkConfig -> networkConfig.valid).ifPresent(networkConfig -> {
                BiConsumer<Update, Board> updateCallback = (u, b) -> {
                    b.alter(u);
                    b.draw(canvas.getGraphicsContext2D());
                };
                session = new ServerSession(networkConfig, updateCallback, board);
            });
        });

        networkJoinBtn.setOnAction(evt -> {
            JoinDialog dialog = new JoinDialog();
            Optional<NetworkConfig> result = dialog.showAndWait();
            result.filter(networkConfig -> networkConfig.valid).ifPresent(networkConfig -> {
                BiConsumer<Update, Board> updateCallback = (u, b) -> {
                    if (canvas == null) {
                        createCanvas(b.getWidth(), b.getHeight());
                    }
                    b.alter(u);
                    b.draw(canvas.getGraphicsContext2D());
                };
                session = new ClientSession(networkConfig, updateCallback);
            });
        });
    }

    private void loadBoardFromSpec(String specStr) {
        board = new Board(specStr);

        // TODO: dynamically size canvas
        createCanvas(board.getWidth(), board.getHeight());
        board.draw(canvas.getGraphicsContext2D());
    }

    private void createCanvas(double w, double h) {
        double MAX_CELL = 100;
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double MAX_WIDTH = screenBounds.getWidth() - 200;
        double MAX_HEIGHT = screenBounds.getHeight() - 200;

        double max_w_cell = MAX_WIDTH / w;
        double max_h_cell = MAX_HEIGHT / h;

        double w_final, h_final;
        if (MAX_CELL < max_w_cell && MAX_CELL < max_h_cell) {
            w_final = MAX_CELL * w;
            h_final = MAX_CELL * h;
        } else if (max_w_cell < max_h_cell) {
            w_final = MAX_WIDTH;
            h_final = MAX_WIDTH / w * h;
        } else {
            w_final = MAX_HEIGHT / h * w;
            h_final = MAX_HEIGHT;
        }
        canvas = new Canvas(w_final, h_final);
        root.setPrefSize(w_final, h_final + ((MenuBar) root.getTop()).getHeight());
        root.setCenter(canvas);

        Window window = root.getScene().getWindow();
        window.setWidth(w_final + 40);
        window.setHeight(h_final + ((MenuBar) root.getTop()).getHeight() + 60);
        window.centerOnScreen();
    }

    class HostDialog extends Dialog<NetworkConfig> {
        public HostDialog() {
            setTitle("Host session based on current game...");
            setHeaderText("");

            TextField portField = new TextField();
            HBox portRow = new HBox(5, new Label("Port:"), portField);
            TextField userField = new TextField();
            HBox userRow = new HBox(5, new Label("User:"), userField);
            getDialogPane().setContent(new VBox(10, portRow, userRow));
            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

            setResultConverter(btnType -> {
                return btnType == ButtonType.CANCEL ? new NetworkConfig()
                        : new NetworkConfig("localhost", Integer.parseInt(portField.getText()), userField.getText());
            });
        }
    }

    class JoinDialog extends Dialog<NetworkConfig> {
        public JoinDialog() {
            setTitle("Join remote session...");
            setHeaderText("");

            TextField ipField = new TextField();
            HBox ipRow = new HBox(5, new Label("IP:"), ipField);
            TextField portField = new TextField();
            HBox portRow = new HBox(5, new Label("Port:"), portField);
            TextField userField = new TextField();
            HBox userRow = new HBox(5, new Label("User:"), userField);
            getDialogPane().setContent(new VBox(10, ipRow, portRow, userRow));
            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

            setResultConverter(btnType -> {
                return btnType == ButtonType.CANCEL ? new NetworkConfig()
                        : new NetworkConfig(ipField.getText(), Integer.parseInt(portField.getText()), userField.getText());
            });
        }
    }

}
