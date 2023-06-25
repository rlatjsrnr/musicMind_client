package room;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import client.Client;
import game.CanvasController;
import game.MemberVO;
import game.MyTurn;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import waitingRoom.WaitingRoomController;

public class RoomController implements Initializable {

    @FXML private TextField roomName;
    @FXML private Button btn1, btn2;
    public Socket server;
	
	// 접속하려는 방 서버 ip, port번호
	InetAddress ip;
	
	// 방을 생성한 client마다 서로 다른 포트 번호 저장
	public static int roomPort;
	
	// 입출력
	public PrintWriter printer;
	public BufferedReader br;
	
	// 닉네임
	String id;
	String pw;
	
	// 무대 정보를 캔버스로 넘겨줌
	Stage stage;
	
	// 현재 서버에 연결되어있는 사용자 리스트 
	ObservableList<MemberVO> array;
	// MediaView를 통해 재생되는 resource를 제어하는 객체
	MediaPlayer mediaPlayer;
	// 재생해야할 resource 정보를 저장하는 객체
	Media media;
	// 자기 순서일 때 myturn 객체 생성
	MyTurn myturn;
	
	MemberVO memberVO;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	Client.mainClient.roomController = this;
    	this.id = Client.member.getMId();
    	this.pw = Client.member.getmPw();
    	try {
			ip = InetAddress.getByName(Client.SERVER_IP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	btn1.setOnAction(e -> {
    		
    	    String roomNameText = roomName.getText();
    	    
    	    try {
    			server = new Socket(ip, roomPort);
    			// server와 통신할 입출력 스트림 초기화
    			InputStream is = server.getInputStream();
    			OutputStream os = server.getOutputStream();
    			printer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
    			br = new BufferedReader(new InputStreamReader(is));
    			printer.println("0|"+id+","+pw);
    			printer.println("roomName|"+roomNameText);
    		} catch (IOException e1) {
    			stopClient();
    			return;
    		}
    		// 게임 화면 무대 생성 및 입장
    		Stage stage = new Stage();
    		try {
    			FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/Canvas.fxml"));
    			Parent root = loader.load();
    			stage.setScene(new Scene(root));
    			// 캔버스 컨트롤러 가져오기
    			// 캔버스 컨트롤러에 무대정보 전달
    			Client.mainClient.canvasController.setStage(stage);
    			// 캔버스 컨트롤러에 클라이언트 컨트롤러 정보 전달
    			Client.mainClient.canvasController.setStartBtnDisable();
    			// 사용자 정보 설정 메소드
    			stage.setTitle("방 이름");
    			stage.setResizable(false);
    			stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
					@Override
					public void handle(WindowEvent arg0) {
						Client.mainClient.send("stop", "stop");
					}
				});
    			stage.show();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
    		// 서버에서 발신된 데이터 수신
    		receive();
    	});
        btn2.setOnAction(e -> {
        	this.stage.close();
        });
    }
	
	public void stopClient() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
			} catch (IOException e) {}
		}
	}
	// server에서 메세지 전달 받음
	public void receive() {
		btn2.fire();
		Thread t = new Thread(()->{
			while(true) {
				try {
					String receiveData = br.readLine();
					if(receiveData == null) {
						break;
					}
					// 서버로부터 전달받은 데이터를 코드와 메시지로 분류하고
					// 코드에 따라 약속된 기능 수행
					String[] data = receiveData.split("\\|");
					String code = data[0];
					String text = data[1];

					// code
					// 0 - 접속자 목록 
					// 1 - 일반 메시지
					// 2 - 게임 화면 채팅창에 띄울 메시지
					// x - x, y 좌표
					// c - 색 선택 정보
					// w - 선 굵기 정보
					// r - clear버튼 클릭
					
					// 방 입장 구현을 위해서 전송받은 접속자 목록을 리스트에 담아
					// 캔버스 컨트롤러에 전달해줌
					if(code.equals("0")) {
						String[] userInfo = text.split(" ");
						array = FXCollections.observableArrayList();
						//접속자 목록 갱신
						for(String s : userInfo) {
							String[] list = s.split(",");
							memberVO = new MemberVO(list[0], Integer.parseInt(list[1]));
							this.array.add(this.memberVO);
						}
						Client.mainClient.canvasController.setList(array);
					}else if(code.equals("l")){
						Client.mainClient.canvasController.setInfo(Client.member);
						
					// 클라이언트 textArea에 메시지 출력
					}else if(code.equals("1")) {
						// 일반 메세지 출력
					// 게임 화면 채팅창에 메시지 출력
					}else if(code.equals("2")){
						Client.mainClient.canvasController.displayText(text);
					
					// x,y 좌표값
					}else if(code.equals("x")) {
						String[] list = text.split(",");
						Client.mainClient.canvasController.draw(list[0], Double.parseDouble(list[1]), Double.parseDouble(list[2]));
						
					// ColorPicker 색깔 데이터 R,G,B
					}else if(code.equals("c")) {
						String[] list = text.split(",");
						Client.mainClient.canvasController.pickColor(Double.parseDouble(list[0]), Double.parseDouble(list[1]),Double.parseDouble(list[2]));
						
					// slider 선 굵기 데이터
					}else if(code.equals("w")) {
						Client.mainClient.canvasController.setLineW(Integer.parseInt(text));
						
					// clear버튼 클릭시
					}else if(code.equals("r")) {
						Client.mainClient.canvasController.clear();
					// 권한 설정
					// a|true,ID 형태
					// 그림 그릴 권한 설정에 사용
					}else if(code.equals("a")) {
						String[] s = text.split(",");
						if(s[0].equals("true")) {
							Client.mainClient.canvasController.setAuth(true, s[1], s[2]);
						}else if(s[0].equals("false")) {
							Client.mainClient.canvasController.setAuth(false, s[1], s[2]);
						}
					}
					// 득점
				} catch (IOException e) {
					stopClient();
					break;
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	public void setStage(Stage stage2) {
		this.stage = stage2;
	}

}



