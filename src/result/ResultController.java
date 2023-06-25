package result;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;

import client.Client;
import dao.MemberDAOImpl;
import game.CanvasController;
import game.MemberVO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ResultController implements Initializable{
	@FXML private TableView<MemberVO> tableView;
	@FXML private Button btnexit;
	
	ObservableList<MemberVO> list;
	Stage stage;
	
	MemberVO member;
	MemberDAOImpl mDao;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Client.mainClient.resultController = this;
		btnCloseMethod();
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void setResultData(ObservableList<MemberVO> list) {
		this.list = list;
		mDao = new MemberDAOImpl();
		setTableView();
		for(MemberVO m : list) {
			if(m !=null) {
				if(m.getMId().equals(Client.mainClient.canvasController.id)) {
					this.member = m;
					
				}
			}
		}
	}
	
	public void setTableView() {
		Class<MemberVO> clazz = MemberVO.class;
		Field[] fields = clazz.getDeclaredFields();
		for(int i = 0; i < fields.length; i++) {
			if(i == 1) {
				continue;
			}
			String name = fields[i].getName();
			String headerText = (i == 0) ? "아이디" : name;
			if(i == 2) {
				headerText = "점수";
			}
		    TableColumn<MemberVO,?> tc = new TableColumn<>(headerText);
		    if (name.equals("mId")) {
	            tc.setCellValueFactory(new PropertyValueFactory<>("mId"));
	        } else if (name.equals("mPoint")) {
	            tc.setCellValueFactory(new PropertyValueFactory<>("point"));
	        }
			
			// column 너비 지정
			tc.setPrefWidth(450);
			// column 크기 수정 불가
			tc.setResizable(false);
			tc.setStyle("-fx-alignment:center;-fx-text-fill:red;");
			tableView.getColumns().add(tc);
		}
		
		tableView.setItems(list);
		
	}// end setTableView
	
	public void btnCloseMethod() {
		
		// 닫기 버튼 event
		btnexit.setOnAction(e->{
			//db업데이트 & 화면닫기
			mDao = new MemberDAOImpl();
			int oldPoint = mDao.selectPoint(member);
			
			// 점수가  기존 점수보다 크면
			if(member.getPoint()>oldPoint) {
				String res1 = mDao.update(member);
				Client.mainClient.canvasController.send("stop", "stop");
				this.stage.close();
			}else {
				Client.mainClient.canvasController.send("stop", "stop");
				this.stage.close();
			}
		});
	}
	
	
	
}
