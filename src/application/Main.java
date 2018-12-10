package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;



import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class Main extends Application {

	Socket socket;
	TextArea textArea;

	//클라이언트 실행
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);	//IP와 port번호를 받아서 socket을 초기화해서
					receive();						//서버로부터 메세지를 전달받는다.
				}
				catch(Exception e) {
					if(!socket.isClosed()) {	//소켓이 열려있다면
						stopClient();			//클라이언트를 종료시킨다.
						System.out.println("서버 접속 실패");
						Platform.exit();		//프로그램을 종료시킨다.
					}
				}
			}
		};

		thread.start();
	}
	//클라이언트 종료
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	//서버로부터 메세지 전달받음
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();	//소켓에서 데이터를 inputStream으로 리턴을 받아서 inputStream형 in객체를 생성
				byte[] buffer = new byte[512];				//byte형 배열인 buffer를 최대 512바이트까지 받을 수 있도록 객체 생성.
				int length = in.read(buffer);				//inputStream으로부터 읽어들인 바이트(데이터)를 buffer에 저장하고 실제 읽어들인 바이트 수를 리턴하여 length에 저장
				if(length == -1) 							//메세지를 읽어들일 때 오류가 발생했다면
					throw new IOException();				//IOExeption오류를 발생시킴
				
				String msg = new String(buffer, 0, length, "UTF-8");				
				
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
		
	}
	
	

	//서버로 메세지 전송
	public void send(String msg) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();	//socket에서 메세지를 내보내기 위한 OutputStream객체 생성
					byte[] buffer = msg.getBytes("UTF-8");			//문자열 객체인 msg를 UTF-8로 인코딩하고 byte형식으로 변환하여 buffer배열 생성 
					out.write(buffer);								//buffer에 있는 길이만큼의 바이트를 OutputStream에 쓴다.
					out.flush();									//그리고 OutputStream에 있는 버퍼를 비워준다.
					
					Platform.runLater(() -> {
						textArea.appendText(msg);
						
						
					});
				}catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}





	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("8008");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		
		
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		
		
		Button connectionButton = new Button("접속");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("접속")) {
				int port = 8008;
				try {
					port = Integer.parseInt(portText.getText());
				}catch(Exception e){
					e.printStackTrace();
				}
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("채팅방 접속 \n");
				});
				connectionButton.setText("종료");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			}else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("채팅방 퇴장 \n");
				});
				connectionButton.setText("접속");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("채팅 클라이언트");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
