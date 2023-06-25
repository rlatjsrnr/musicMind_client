package game;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import client.Client;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import result.ResultController;
import room.RoomController;
import waitingRoom.WaitingRoomController;

public class CanvasController implements Initializable {

	// 게임 화면
	
	// 채팅창
	@FXML private TextArea txtArea;
	// 채팅 및 정답 입력 필드
	@FXML private TextField txtInput;
	// 시작, 나가기, 채팅입력, 컨버스클리어, 지우개 버튼
	@FXML private Button btnStart, btnClose, btnSend, btnClear, btnEraser;
	// 캔버스
	@FXML private Canvas canvas;
	// 남은 시간 표시 바
	@FXML private ProgressBar progress;
	// 같은 방에 입장한 유저들에게 보여지는 유저 정보 박스 1,2,3,4
	@FXML private HBox userBox1, userBox2, userBox3, userBox4;
	// 문제 시간, 유저 아이디 레이블
	@FXML private Label lblTime, lblID1,lblID2,lblID3,lblID4;
	// 유저 현재 획득 점수 레이블
	@FXML private Label lblScore1, lblScore2, lblScore3, lblScore4;
	// 내 정보 총 획득 점수, 랭킹, 아이디 , 노래제목, 문제 번호 레이블
	@FXML private Label lblPoint, lblID, lblSongTitle, lblSongCnt;
	// 컬러 선택 박스
	@FXML private ColorPicker pick;
	// 선 굵기 조절 슬라이더
	@FXML private Slider slider;
	// 자기 순서 나타내는 그림
	@FXML private ImageView turn1, turn2, turn3, turn4;
	// 득점 이펙트
	@FXML private ImageView score1, score2, score3, score4;
	
	GraphicsContext gc;
	// 게임 화면이 보여지는 스테이지 
	Stage stage;
	// 같은 방에 참가중인 유저 리스트
	ObservableList<MemberVO> list;
	// 입장 효과를 위한 리스트 유저정보 박스, 유저 아이디
	List<HBox> userBox;
	List<Label> userId;
	List<Label> lblScore;
	List<ImageView> turn;
	List<ImageView> score;
	// MediaView를 통해 재생되는 resource를 제어하는 객체
	MediaPlayer mediaPlayer;
	// 재생해야할 resource 정보를 저장하는 객체
	Media media;
	// 자기 순서일 때 myturn 객체 생성
	MyTurn myturn;
	// 그림을 그릴 권한
	private boolean auth;
	// 아이디 저장
	public String id;
	private int pos;
	// 문제 수 카운트
	private static int count;
	// 출제된 노래 목록
	public List<Integer> musiclist;	
	
	MemberVO member;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Client.mainClient.canvasController = this;
		auth = true;
		count = 1;
		musiclist = new ArrayList<>();
		myturn = new MyTurn(mediaPlayer, media);
		gc = canvas.getGraphicsContext2D();
		// 각종 기능 초기화
		// 색 선택 박스 초기화
		pick.setValue(Color.BLACK);		
		// 선 색 초기화
		gc.setStroke(Color.BLACK);
		// 선 굵기 초기화
		gc.setLineWidth(1);				
		// 선 굵기 선택 슬리이더 최소값 최대값 설정
		slider.setMin(1);
		slider.setMax(10);
		// 채팅창에 글 못쓰게 함
		txtArea.setEditable(false);
		// 남은 시간 바 초기화
		progress.setProgress(0.0);
		
		// 입장 효과를 위한 유저 정보 리스트 초기화
		// 안보이다가 유저가 입장하면 박스 생김
		userBox = new ArrayList<>();
		userBox.add(userBox1);
		userBox.add(userBox2);
		userBox.add(userBox3);
		userBox.add(userBox4);
		userId = new ArrayList<>();
		userId.add(lblID1);
		userId.add(lblID2);
		userId.add(lblID3);
		userId.add(lblID4);
		lblScore = new ArrayList<>();
		lblScore.add(lblScore1);
		lblScore.add(lblScore2);
		lblScore.add(lblScore3);
		lblScore.add(lblScore4);
		turn = new ArrayList<>();
		turn.add(turn1);
		turn.add(turn2);
		turn.add(turn3);
		turn.add(turn4);
		score = new ArrayList<>();
		score.add(score1);
		score.add(score2);
		score.add(score3);
		score.add(score4);
		// 시작버튼 클릭시 정해진 방식과 순서에 따라 게임이 진행되어야 함
		btnStart.setOnAction(e->{
			// 시작버튼을 누르고 시작버튼 비활성화
			btnStart.setDisable(true);
			// 그림을 그릴 권한을 가지면
			if(auth == true) {
				// 캔버스와 클리어 버튼 활성화
				canvas.setDisable(false);
				btnClear.setDisable(false);
				
				// 노래 재생
				setMyTurn();
				
				// canvas위에서 마우스가 눌러졌을 때
				// 눌러진 x, y 좌표를 서버로 전송하여 다른 클라이언트들에 beginPath() 설정
				// x - x,y 좌표 전송 표시
				// p - Pressed 전송 표시
				canvas.setOnMousePressed(e1->{
					gc.lineTo(e1.getX(), e1.getY());
					send("x", "p,"+e1.getX()+","+e1.getY());
				});
				
				// 마우스가 드래그 되는 동안
				// x,y 좌표를 서버로 전송하여 클라이언트들에 선을 그림
				// x - x, y 좌표 전송 표시
				// d - Dragged 전송 표시
				canvas.setOnMouseDragged(e1->{
					gc.lineTo(e1.getX(), e1.getY());
					send("x", "d,"+e1.getX()+","+e1.getY());
				});
				
				// 색 선택 박스에서 색을 선택하면 선택된 색의 R,G,B값을 
				// 서버로 전송하여 다른 클라이언트들의 선 색을 바꿈
				// c - ColorPicker 전송 표시
				pick.valueProperty().addListener((target, o, n)->{
					send("c",n.getRed()+","+n.getGreen()+","+n.getBlue());
				});
				
				// 선 굵기 선택 슬라이더에서 굵기를 선택하면 선택된 값을 서버로 전송
				// 다른 클라이언트들의 선 굵기를 바꿈
				// w - LineWidth 전송 표시
				slider.valueProperty().addListener((target,o,n)->{
					int value = n.intValue();
					//double result = value/10.0;
					send("w", ""+value);
				});
				
				// 지우개 버튼 클릭시 색 선택 상자의 색을 흰색으로 설정
				// 선 굵기를 10으로 설정하고
				// 그 정보들을 서버로 전송하여 지우개 역할
				btnEraser.setOnAction(e1->{
					send("c","1.0,1.0,1.0,1.0");
					send("w", "10");
				});
				
				// 클리어 버튼 클릭시 캔버스를 초기화 시키고
				// 선 색과 선 굵기도 초기화 시킴
				// r - clear버튼 눌렀다는 정보 서버로 전송
				btnClear.setOnAction(e1->{
					send("r", "clear");
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
					slider.setValue(1);
					pick.setValue(Color.BLACK);
				});
			
			// 그림을 그릴 권한이 없으면 캔버스와 클리어버튼 비활성화
			}else {
				canvas.setDisable(true);
				btnClear.setDisable(true);
			}
		});
		
		// 사용자가 입력한 문자를 서버로 전송해 주는 버튼
		// 이 전송 데이터는 채팅의 역할과
		// 문제 정답 입력 역할을 할 예정
		btnSend.setOnAction(e1->{
			String text = txtInput.getText().trim();
			if(text.equals("")) {
				displayText("메시지를 먼저 작성해주세요.");
				txtInput.requestFocus();
				return;
			}
			// 서버로 메세지 전달
			send("2", text+","+pos);
		});
		
		// 문자열 입력하고 엔터치면 바로 전송
		txtInput.setOnKeyPressed(event->{
			if(event.getCode() == KeyCode.ENTER) {
				btnSend.fire();
			}
		});
		
		// 나가기 버튼 클릭시 게임 화면에서 벗어남
		btnClose.setOnAction(e1->{
			if(myturn.mediaPlayer != null) {
				myturn.stopMusic();
			}
			send("stop", "stop");
			stage.close();
		});
		
	}// initialize
	
	// 그림 그릴 권한 설정
	// 서버에서 문제를 맞힌 사람에게는 true와 문제 맞힌 사람의 id를 전송해줌
	// id를 이용해서 정답을 맞힌 사람을 다른 사람들에게 알림
	public void setAuth(boolean b, String id, String pos) {
		this.auth = b;
		Platform.runLater(()->{
			Alert alert1 = new Alert(AlertType.INFORMATION);
	        alert1.setTitle("정답!!!");
	        alert1.setHeaderText(id+"님 정답!!!");
	        alert1.showAndWait();
		});
		// 정답자 점수 증가
		for(MemberVO m : list) {
			if(m.getMId().equals(id)) {
				m.setPoint(m.getPoint()+1);
			}
		}
		// 문제를 맞힌 사람은 시작 버튼 활성화
		if(b == true) {
			btnStart.setDisable(false);
			
		// 다른 사람들은 시작버튼을 활성화해서 한번 눌러줌
		// 위에 시작 버튼 액션에 들어가서 캔버스와 클리어버튼을 비활성화 시킴
		}else if(b == false){
			btnStart.setDisable(false);
			btnStart.fire();
			Platform.runLater(()->{
				lblSongTitle.setText("노래 제목을 맞춰보세요");
			});
		}
		
		for(int i=0; i<list.size(); i++) {
			final int j = i;
			if(i != Integer.parseInt(pos)-1) {
				Platform.runLater(()->{
					turn.get(j).setVisible(false);
					score.get(j).setVisible(false);
					lblScore.get(j).setText(String.valueOf(list.get(j).getPoint()));
				});
			}else {
				Platform.runLater(()->{
					turn.get(j).setVisible(true);
					score.get(j).setVisible(true);
					lblScore.get(j).setText(String.valueOf(list.get(j).getPoint()));
				});
			}
		}
		// 듣고있던 노래 끄고
		if(myturn.mediaPlayer != null) {
			myturn.stopMusic();
		}
		// 캔버스도 초기화
		clear();
		// 문제 수 카운트 1 올려줌
		// 11되면 결과창 뜨고 종료되어야 함
		Platform.runLater(()->{
			lblSongCnt.setText(String.valueOf(count)+"/10");
		});
		count++;
		//게임 종료 시 결과창 오픈
		if(count == 11) {
		
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setHeaderText(null);
				alert.setContentText("게임종료");
				Optional<ButtonType> res = alert.showAndWait();
				// 확인 버튼 눌리면 true , 취소 : false
				if(res.get()== ButtonType.OK) {
					Stage stage = new Stage();
					FXMLLoader loader = null;
					Parent root = null;
					try {
						loader = new FXMLLoader(getClass().getResource("/result/Result.fxml"));
						root = loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Client.mainClient.resultController.setResultData(list);
					stage.setScene(new Scene(root));
					stage.setTitle("게임결과");
					Client.mainClient.resultController.setStage(stage);
					stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
						@Override
						public void handle(WindowEvent arg0) {
							send("stop", "stop");
						}
					});
					stage.show();
					btnClose.fire();
					
				}else {
				}

			});
		}
	}

	// 노래 제목
	public void setSongTitle(String title) {
		Platform.runLater(()->{
			lblSongTitle.setText(title);
		});
	}
	
	// 게임 화면 내 내 정보 데이터 설정 
	public void setInfo(MemberVO m) {
		this.member = m;
		this.id = m.getMId();
		Platform.runLater(()->{
			lblID.setText(id);
			lblPoint.setText(String.valueOf(m.getPoint()));
		});
		
	}
	
	// Progress bar 설정 예정
	public void setProgress(double p, String lblText) {
		progress.setProgress(p);
		lblTime.setText(lblText);
	}
	
	// 게임 화면 스테이지 저장
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	public Stage getStage() {
		return this.stage;
	}
	
	
	// 채팅창에 문자열 입력
	public void displayText(String text) {
		Platform.runLater(()->{
			txtArea.appendText(text+"\n");
		});
	}
	
	
	/**
	 	게임에 참가한 사용자 리스트
	 	방에 입장 하면 나타남
	 	서버가 보내주는 현재 사용자 리스트 정보 활용
	 */
	public void setList(ObservableList<MemberVO> list) {
		this.list = list;
		for(int i=0; i<list.size(); i++) {
			final int j = i;
			if(this.list.get(i) != null) {
				Platform.runLater(()->{
					userBox.get(j).setVisible(true);
					userId.get(j).setText(list.get(j).getMId());
				});
			}
		}
		// 사용자 리스트에서 내 정보를 찾아 위치정보 저장
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).getMId().equals(id)) {
				pos = i+1;
			}
		}
	}
	
	// 서버로 메시지 전달
	public void send(String code, String msg) {
		// code
		// 2 	: 메시지 전달
		// x	: p - beginPath, d - drag x, y 좌표 전달
		// c	: ColorPicker 선택 색 정보 전달
		// w	: slider로 선택한 LineWdith 값 전달
		// r	: clear버튼 이벤트 발생 전달
		if(Client.mainClient.roomController != null) {
			Client.mainClient.roomController.printer.println(code+"|"+msg);
		}else {
			Client.mainClient.waitingRoomController.printer.println(code+"|"+msg);
		}
		txtInput.clear();
		txtInput.requestFocus();
	}
	
	// 그림을 그리는 클라이언트가 아닌 다른 클라이언트에서 
	// 그림을 그린 클라이언트가 서버로 전송한 데이터를 서버에서 전송 받아
	// 캔버스에 그림을 그려줌
	// p - 받은 x, y 좌표에 beginPath 설정
	// d - 받은 x, y 좌표로 선을 그림
	public void draw(String code, double x, double y) {
		if(code.equals("p")) {
			gc.beginPath();
			gc.lineTo(x, y);
		}else if(code.equals("d")) {
			gc.lineTo(x, y);
			gc.stroke();
		}
	}
	
	// 색 정보를 서버에서 전송받아 선 색 설정
	public void pickColor(double r, double g, double b) {
		gc.setStroke(Color.color(r, g, b));
	}
	// 선 굵기 정보를 서버에서 전송받아 선 굵기 설정
	public void setLineW(int value) {
		gc.setLineWidth(value);
	}
	// 클리어 버튼이 눌러졌다는 정보를 서버에서 받아 캔버스 및 선 색, 굵기 초기화
	public void clear() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setLineWidth(1);
		gc.setStroke(Color.BLACK);
	}
	// 그림을 그릴 권한이 있는 사용자에게 노래 들려줌
	public void setMyTurn() {
		if(mediaPlayer != null) {
			myturn.stopMusic();
		}
		myturn.playMusic();
		// 음악 재생시 제목UI에 반영됨 
		setSongTitle(myturn.musicTitle);
	}
	
	public void setStartBtnDisable() {
		btnStart.setDisable(false);
	}
	
}
