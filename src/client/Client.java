package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import dao.MemberDAOImpl;
import game.CanvasController;
import game.MemberVO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import join.JoinController;
import login.LoginController;
import result.ResultController;
import room.RoomController;
import waitingRoom.WaitingRoomController;

public class Client extends Application {
	/**
	 * 서버 ip 주소
	 */
    public static final String SERVER_IP = "localhost";
    /**
     * 서버 포트 번호
     */
    public static final int SERVER_PORT = 5001;
    
    private Stage primaryStage;
    /**
     * main output stream
     */
    private PrintWriter clientWriter;
    /**
     * main input stream
     */
    private BufferedReader clientReader;
    
    /**
     * main 서버와 연결된 소캣
     */
    public Socket socket;
    
    /**
     * 서버와의 통신을 담당하는 클라이언트
     */
    public static Client mainClient;
    
    public LoginController loginController;
    public JoinController joinController;
    public WaitingRoomController waitingRoomController;
    public RoomController roomController;
    public ResultController resultController;
    public CanvasController canvasController;
    
    /**
     * 현재 연결된 유저 리스트 
     */
 	ObservableList<MemberVO> memberVOList; 	
 	/**
 	 * 대기실 유저목록 리스트뷰 표시용
 	 */
 	ObservableList<String> userList;
 	/**
 	 * 서버에서 전달받은 유저목록 리스트
 	 */
 	String[] list;
 	/**
 	 * 현재 생성 되어있는 방 리스트
 	 */
 	ObservableList<String> roomList;
 	/**
 	 * 서버에서 전달받은 방 목록 리스트
 	 */
    String[] room;
    
 	/**
 	 * 사용자 정보 객체
 	 */
 	public static MemberVO member;
 	
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        mainClient = this;
        try {
            // 서버에 연결
            socket = new Socket(SERVER_IP, SERVER_PORT);

            // 연결 확인	
            if (socket.isConnected()) {

                // 클라이언트와 서버 간의 통신을 위한 입출력 스트림 생성
                clientWriter = new PrintWriter(socket.getOutputStream(), true);
                clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 로그인 화면 표시
                Platform.runLater(() -> {
                    showLoginScreen();
                });
                receive();
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }

    // server에서 메세지 전달 받음
 	public void receive() throws ClassNotFoundException {
 		Thread t = new Thread(()->{
 			while(true) {
 				try {
 					String receiveData = clientReader.readLine();
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
 					// 2 - 게임 화면 채팅창에 띄울 메시지
 					// l - 로그인 성공 알려줌
 					// z - 로그인 실패 알려줌
 					// s - 회원가입 성공
 					// f - 회원가입 실패
 					// port - 방 만들기 시 소캣연결에서 사용할 포트번호
 					// roomPort - 방 입장 시 소캣연결에 사용할 포트번호
 					// roomListClear - 방 목록 갱신
 					
 					// 대기실 접속 사용자 목록 
 					if(code.equals("0")) {
 						String[] userInfo = text.split(" ");
 						memberVOList = FXCollections.observableArrayList();
 						userList = FXCollections.observableArrayList();
 						if(room != null) {
 							roomList = FXCollections.observableArrayList(room);
 						}
						//접속자 목록 갱신
						for(String s : userInfo) {
							list = s.split(",");
							MemberVO memberVO = new MemberVO(list[0], list[1]);
							this.memberVOList.add(memberVO);
							this.userList.add(list[0]);
						}
						if(waitingRoomController != null) {
							Platform.runLater(()->{
								waitingRoomController.setMemberList(this.userList);
								waitingRoomController.setwaitingList(this.roomList);	
							});
							
						}
					// 로그인 성공
 					}else if(code.equals("l")){
 						String[] info = text.split(",");
 						member = new MemberVO(info[0], info[1], 0);
 						MemberDAOImpl dao = new MemberDAOImpl();
 						int point = dao.selectPoint(member); 
 						member.setPoint(point);
 						Platform.runLater(()->{
 							loginController.openWaitingRoom();
 							waitingRoomController.setMemberList(this.userList);
 							waitingRoomController.setwaitingList(this.roomList);
 						});
 					
 					// 로그인 실패
 					}else if(code.equals("z")) {
 						Platform.runLater(()->{
 	 						loginController.showLoginFailedAlert();
 						});
 					// 클라이언트 textArea에 메시지 출력
 					}else if(code.equals("2")) {
 						waitingRoomController.displayText(text);
 						
 					}else if(code.equals("port")) {
 						RoomController.roomPort = Integer.parseInt(text);
 						
 					}else if(code.equals("enterPort")) {
 						waitingRoomController.setEnterPort(Integer.parseInt(text));
 					
 					}else if(code.equals("roomList")) {
 						room = text.split(" ");
 						if(room != null) {
 							roomList = FXCollections.observableArrayList(room);
 						}
 						Platform.runLater(()->{
 							waitingRoomController.setwaitingList(this.roomList);
 						});
 						
 					}else if(code.equals("roomListClear")) {
 						Platform.runLater(()->{
 							this.roomList.clear();
 						});
 					}else if(code.equals("s")) {
 						Platform.runLater(()->{
 							joinController.showJoinSuccessAlert();
 						});
 					}else if(code.equals("f")) {
 						Platform.runLater(()->{
 							joinController.showJoinFailedAlert();
 						});
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
 	// 연결 종료 
 	public void stopClient() {
 	//	displayText("[서버와 연결 종료]");
 		if(socket != null && !socket.isClosed()) {
 			try {
 				socket.close();
 			} catch (IOException e) {}
 		}
 	}
 	
 	public void send(String code, String msg) {
		// 0| 닉네임
		// 1| 메세지
    	clientWriter.println(code+"|"+msg);
	}
 	
 	// 로그인 창
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/Login.fxml"));
            Parent root = loader.load();
            loginController.setStage(primaryStage);
            primaryStage.setTitle("로그인");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
