package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;



import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {

	Socket socket;
	TextArea textArea;

	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				}
				catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("서버 접속 실패");
						Platform.exit();
					}
				}
			}
		};

		thread.start();
	}

	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1)
					throw new IOException();
				String msg = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(() -> {
					textArea.appendText(msg);
				});
				
				
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
	}

	public void send(String msg) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = msg.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}





	@Override
	public void start(Stage primaryStage) {

	}

	public static void main(String[] args) {
		launch(args);
	}
}
