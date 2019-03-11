
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.fontys.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author jaapg
 */
public class ChatServer extends Application {

    private TextField textInput;
    private ObservableList<String> oList;
    private Thread thread;
    private ServerSocket socket;
    private Socket clientSocket;
    private ArrayList<Socket> sockets = new ArrayList<>();

    private final static String NAME = "Susan";

    // Scanner and PrintWriter to read and send strings from/to a socket
    private Scanner scanSock = null;
    private PrintWriter pw = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("My Chat - Server");
        textInput = new TextField();
        textInput.setOnAction(e -> sendText(textInput.getText()));

        ListView<String> listView = new ListView<>();
        oList = FXCollections.observableArrayList();
        listView.setItems(oList);

        BorderPane root = new BorderPane();
        root.setCenter(listView);
        root.setBottom(textInput);
        primaryStage.setScene(new Scene(root,300,250));
        primaryStage.show();


        // create a thread which runs handleNetwork()
        thread = new Thread(this::handleNetwork);
        thread.start();
    }

    public void sendText(String line) {
        // Send the string to the socket
        String text = NAME + ": " + line;
        printToClients(text);
        oList.add(text);
    }

    public void handleNetwork() {
        // This method should run as a thread
        // setup the connection
        try {
            socket = new ServerSocket(1974);
            while (!Thread.currentThread().isInterrupted()) {
                clientSocket = socket.accept();
                sockets.add(clientSocket);
                Thread t = new Thread(() -> {
                    try {
                        Scanner scanner = new Scanner(clientSocket.getInputStream());
                        while (!Thread.currentThread().isInterrupted()) {
                            String line = scanner.nextLine();
                            if (line != null) {
                                Platform.runLater(() -> oList.add(line));
                                printToClients(line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();
            }
            // read data from the socket and show it on the UI
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printToClients(String line) {
        for (Socket socket : sockets) {
            try {
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                pw.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        // stop the thread and close the socket
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
