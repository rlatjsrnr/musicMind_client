package waitingRoom;

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
import java.util.ArrayList;

import client.Client;
import dao.MemberDAOImpl;
import game.MemberVO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WaitingRoomController {
    @FXML private ListView<String> waitingList;
    @FXML private ListView<String> memberList;
    @FXML private Label nickname, ranking, score, gameTitle; 
    
    //채팅, 랭킹
    @FXML private TextArea textarea, rank;
    @FXML private TextField textField;
    @FXML private Button btnJoin, btnCreate, btnSend, btnRefresh;;
    
    Socket server;
    
    InetAddress ip;
    
    MemberDAOImpl mDao;
    ObservableList<MemberVO> ranklist;

    public PrintWriter printer;
	public BufferedReader br;
    
    private ObservableList<String> waitingUsers;
    private ObservableList<String> roomList;
    
    ObservableList<MemberVO> array;
    
    MemberVO memberVO;
    
    String id;
	String pw;
    
    private int roomPort;
    private String roomTitle;
    
    public void initialize() {
    	Client.mainClient.waitingRoomController = this;
        waitingUsers = FXCollections.observableArrayList();
        roomList = FXCollections.observableArrayList();
        
        this.id = Client.member.getMId();
    	this.pw = Client.member.getmPw();
    	
    	// 방 목록 선택 시 방 이름 정보로 포트번호 받기
        waitingList.getSelectionModel().selectedItemProperty().addListener((target, o, n)->{
        	if(n != null) {
        		roomTitle = n.toString();
        		Client.mainClient.send("roomEnter", roomTitle);
        	}
        });
        
   	 	btnCreate.setOnAction(event -> {
        	try {
        		Client.mainClient.send("room", "create");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/room/Room.fxml"));
                Parent root = loader.load();
                Stage roomStage = new Stage();
                Client.mainClient.roomController.setStage(roomStage);
                roomStage.setScene(new Scene(root));
                roomStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
   	 	
        btnJoin.setOnAction(e->{
        	try {
				ip = InetAddress.getByName(Client.SERVER_IP);
				server = new Socket(ip, roomPort);
				// server와 통신할 입출력 스트림 초기화
    			InputStream is = server.getInputStream();
    			OutputStream os = server.getOutputStream();
    			printer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
    			br = new BufferedReader(new InputStreamReader(is));
    			printer.println("0|"+id+","+pw);
    			printer.println("roomTitle|"+roomTitle);
    		} catch (IOException e1) {
    			//stopClient();
    			return;
    		}
        	Stage stage = new Stage();
    		try {
    			FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/Canvas.fxml"));
    			Parent root = loader.load();
    			stage.setScene(new Scene(root));
    			// 캔버스 컨트롤러 가져오기
    			// 캔버스 컨트롤러에 무대정보 전달
    			Client.mainClient.canvasController.setStage(stage);
    			// 캔버스 컨트롤러에 클라이언트 컨트롤러 정보 전달
    			Client.mainClient.canvasController.setInfo(Client.member);
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
        
        btnSend.setOnAction(e->{
        	String message = textField.getText();
            // 채팅 메시지를 서버로 전송
            Client.mainClient.send("2", message);
            textField.clear();
        });

        //nickname 전달        
        nickname.setText(Client.member.getMId());
        
        loadRank();
        gameTitle.setText("게임 대기실");
        
        textField.setOnKeyPressed(e->{
        	if(e.getCode() == KeyCode.ENTER) {
        		btnSend.fire();
        	}
        });
        
        btnRefresh.setOnAction(e->{
        	 loadRank();
        });
        
    }

    private void loadRank() {
        mDao = new MemberDAOImpl();
        ArrayList<MemberVO> mDaoList = mDao.select();
        String rankStr= "";
        int i= 1;
        String username = Client.member.getMId();
        ranklist = FXCollections.observableArrayList(mDaoList);
        for(MemberVO m : ranklist) {
           String mId = m.getMId();
           int mPoint = m.getPoint();
           rankStr += i+"위 "+mId+" : "+mPoint+"점 \n";
           if(mId.equals(username)) {
              ranking.setText(i+"위");
              score.setText(String.valueOf(mPoint)+"점");
           }
           i++;
        }
        rank.setText(rankStr);
	}

	public void receive() {
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
						
					// 로그인 성공 
					}else if(code.equals("l")){
						Client.mainClient.canvasController.setInfo(Client.member);
					
					// 방 입장 실패 풀방임
					}else if(code.equals("roomEnterFail")) {
						Platform.runLater(()->{
							Client.mainClient.canvasController.getStage().close();
 							roomEnterFail();
 						});
 						
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
				} catch (IOException e) {
					stopClient();
					break;
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
    public void setEnterPort(int enterPort) {
    	this.roomPort = enterPort;
    }
    
    public void stopClient() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
			} catch (IOException e) {}
		}
	}
    
    // 채팅창에 문자열 입력
  	public void displayText(String text) {
  		Platform.runLater(()->{
  			textarea.appendText(text+"\n");
  		});
  	}

    public void setMemberList(ObservableList<String> list) {
    	this.waitingUsers = list;
    	memberList.setItems(waitingUsers);
    	
    }
    
    public void setwaitingList(ObservableList<String> list) {
    	this.roomList = list;
    	waitingList.setItems(roomList);
    }
    public void roomEnterFail() {    	
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.setTitle("입장 실패");
        alert.setHeaderText("풀방임");
        alert.showAndWait();
    }
}
