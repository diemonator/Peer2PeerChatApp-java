/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.fontys.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import javafx.application.Application;
import static javafx.application.Application.launch;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author jaapg
 */
public class ChatClient extends Application {

    private final int RECONNECT_DELAY = 1000;

    private TextField textInput;
    private ObservableList<String> oList;

    private final static String NAME = "Marc";
    private final static String HOSTNAME = "localhost";
    private final static int PORT = 1974;

    // Use this to connect to the server
    private final static InetSocketAddress endpoint = new InetSocketAddress(HOSTNAME, PORT);

    // Scanner and PrintWriter to read and send strings from/to a socket
    private Scanner scanSock = null;
    private PrintWriter pw = null;
    private Thread thread;
    private Socket serverSocket;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("My Chat - client: " + NAME);
        textInput = new TextField();
        textInput.setOnAction(e -> sendText(textInput.getText()));

        ListView<String> listView = new ListView<>();
        oList = FXCollections.observableArrayList();

        listView.setItems(oList);

        BorderPane root = new BorderPane();
        root.setCenter(listView);
        root.setBottom(textInput);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();

        // create a thread which runs handleNetwork()
        thread = new Thread(this::handleNetwork);
        thread.start();
    }

    public void sendText(String line) {
        // Send the string to the socket
        pw.println(NAME + ": " + line);
    }

    public void handleNetwork() {
        // setup the connection
        serverSocket = new Socket();
        try {
            // Once connected create the PrintWriter & Scanner
            serverSocket.connect(endpoint);
            OutputStream out = serverSocket.getOutputStream();
            InputStream input = serverSocket.getInputStream();
            pw = new PrintWriter(out, true);
            scanSock = new Scanner(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // read data from the socket and show it on the UI
        while (!Thread.currentThread().isInterrupted()) {
            String line = scanSock.nextLine();
            if (line != null) Platform.runLater(() -> oList.add(line));
        }
    }

    @Override
    public void stop() {
        // stop the thread and close the socket
        thread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
