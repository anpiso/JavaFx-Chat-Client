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

	//Ŭ���̾�Ʈ ����
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);	//IP�� port��ȣ�� �޾Ƽ� socket�� �ʱ�ȭ�ؼ�
					receive();						//�����κ��� �޼����� ���޹޴´�.
				}
				catch(Exception e) {
					if(!socket.isClosed()) {	//������ �����ִٸ�
						stopClient();			//Ŭ���̾�Ʈ�� �����Ų��.
						System.out.println("���� ���� ����");
						Platform.exit();		//���α׷��� �����Ų��.
					}
				}
			}
		};

		thread.start();
	}
	//Ŭ���̾�Ʈ ����
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	//�����κ��� �޼��� ���޹���
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();	//���Ͽ��� �����͸� inputStream���� ������ �޾Ƽ� inputStream�� in��ü�� ����
				byte[] buffer = new byte[512];				//byte�� �迭�� buffer�� �ִ� 512����Ʈ���� ���� �� �ֵ��� ��ü ����.
				int length = in.read(buffer);				//inputStream���κ��� �о���� ����Ʈ(������)�� buffer�� �����ϰ� ���� �о���� ����Ʈ ���� �����Ͽ� length�� ����
				if(length == -1) 							//�޼����� �о���� �� ������ �߻��ߴٸ�
					throw new IOException();				//IOExeption������ �߻���Ŵ
				
				String msg = new String(buffer, 0, length, "UTF-8");				
				
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
		
	}
	
	

	//������ �޼��� ����
	public void send(String msg) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();	//socket���� �޼����� �������� ���� OutputStream��ü ����
					byte[] buffer = msg.getBytes("UTF-8");			//���ڿ� ��ü�� msg�� UTF-8�� ���ڵ��ϰ� byte�������� ��ȯ�Ͽ� buffer�迭 ���� 
					out.write(buffer);								//buffer�� �ִ� ���̸�ŭ�� ����Ʈ�� OutputStream�� ����.
					out.flush();									//�׸��� OutputStream�� �ִ� ���۸� ����ش�.
					
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
		userName.setPromptText("�г����� �Է��ϼ���");
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
		
		
		
		Button sendButton = new Button("������");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		
		
		Button connectionButton = new Button("����");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("����")) {
				int port = 8008;
				try {
					port = Integer.parseInt(portText.getText());
				}catch(Exception e){
					e.printStackTrace();
				}
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("ä�ù� ���� \n");
				});
				connectionButton.setText("����");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			}else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("ä�ù� ���� \n");
				});
				connectionButton.setText("����");
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
		primaryStage.setTitle("ä�� Ŭ���̾�Ʈ");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
