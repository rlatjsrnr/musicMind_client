package login;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController implements Initializable{
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
	public void initialize(URL location, ResourceBundle resources) {
		Client.mainClient.loginController = this;
        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
		
	}
    
    @FXML private void handleRegister(ActionEvent event) {
    	Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/join/Join.fxml"));
            Parent root = loader.load();
            Client.mainClient.joinController.setStage(stage);
            stage.setTitle("회원가입");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        // 로그인 로직
        Client.mainClient.send("0", username+","+password);
    }

    public void openWaitingRoom() {
    	stage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/waitingRoom/WaitingRoom.fxml"));
            Parent root = loader.load();
            Stage waitingRoomStage = new Stage();
            waitingRoomStage.setTitle("대기실");
            waitingRoomStage.setScene(new Scene(root));
            waitingRoomStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(new ActionEvent());
        }
    }

    

    public void showLoginFailedAlert() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("로그인 실패");
        alert.setHeaderText("로그인 실패");
        alert.setContentText("아이디 또는 비밀번호가 잘못되었습니다.");
        alert.showAndWait();
    }

	
}
