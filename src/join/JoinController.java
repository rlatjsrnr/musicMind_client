package join;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import client.Client;

public class JoinController implements Initializable {
	@FXML private TextField txtID, txtPw, txtPwChk;
    @FXML private Button btnCancel, btnSave;

    private Stage stage;
    private PrintWriter clientWriter;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	Client.mainClient.joinController = this;
    	btnSave.setOnAction(e->{
    		handleJoin();
    	});
        btnCancel.setOnAction(e -> {
        	stage.close();
        });
    }

    @FXML
    private void handleJoin() {
    	String ID = txtID.getText();
		String Pw = txtPw.getText();
		String PwChk = txtPwChk.getText();			
		if(ID.trim().equals("")) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("ID를 입력해주세요.");
			alert.show();
			txtID.requestFocus();
			return;
		}			
		
		if(Pw.trim().equals("")) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("비밀번호를 입력해주세요.");
			alert.show();
			txtPw.requestFocus();
			return;
		}			
		if(!Pw.equals(PwChk)){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("비밀번호가 일치하지 않습니다.");
			alert.show();
			txtPw.requestFocus();
			return;
		}
		// 서버에 code : j 로 아이디와 비밀번호 전달
		Client.mainClient.send("j", ID+","+Pw);
    }

    public void showJoinSuccessAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("회원가입 성공");
        alert.setHeaderText("회원가입 성공");
        alert.setContentText("회원가입이 성공적으로 완료되었습니다.");
        alert.showAndWait();
        stage.close();
    }

    public void showJoinFailedAlert() {
    	txtID.clear();
    	txtPw.clear();
    	txtPwChk.clear();
    	txtID.requestFocus();
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("회원가입 실패");
        alert.setHeaderText("회원가입 실패");
        alert.setContentText("사용할 수 없는 아이디입니다.");
        alert.showAndWait();
    }
}
