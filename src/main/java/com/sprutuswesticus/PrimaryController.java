package com.sprutuswesticus;

import java.io.IOException;
import java.util.Optional;

import com.sprutuswesticus.network.NetworkConfig;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class PrimaryController {
    private Board board;

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
        board = new Board();
    }

    @FXML
    private void initialize() 
    {
        gameLoadSpecBtn.setOnAction(evt -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New game from spec...");
            dialog.setHeaderText("");
            dialog.setContentText("Spec:");
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(specStr -> {
                System.out.println(specStr);
                board.specific(specStr);
                System.out.println(board.stringifygrid());
            });
        });

        networkHostBtn.setOnAction(evt -> {
            Dialog<NetworkConfig> dialog = new Dialog<>();
            dialog.setTitle("Host session based on current game...");
            dialog.setHeaderText("");

            TextField portField = new TextField("Port");
            TextField userField = new TextField("User");
            dialog.getDialogPane().setContent(new VBox(10, portField, userField));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

            Optional<NetworkConfig> result = dialog.showAndWait();
            result.ifPresent(networkConfig -> {
                ;
            });
        });
    }

}
