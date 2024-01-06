package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable {

    private Socket clientSocket;
    private ChatServer chatServer;
    private int numberClient;

    public ClientThread(Socket clientSocket, ChatServer chatServer, int numberClient) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
        this.numberClient = numberClient;
    }

    @Override
    public void run() {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex) {
            try {
                clientSocket.close();
            } catch (IOException ex1) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex1);
                throw new RuntimeException(ex1);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = dateFormat.format(new Date());
        
        //System.out.println("Client #" + numberClient + " is connected (" + currentTime + ")");

        try {
            new PrintWriter(clientSocket.getOutputStream(), true).println("Welcome to the chat, Client #" + numberClient + ".\n");
        } catch (IOException ex) {
            try {
                clientSocket.close();
            } catch (IOException ex1) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex1);
                throw new RuntimeException(ex1);
            }
        }
        String clientMessage = null;

        while(true) {
            try {
                clientMessage = in.readLine();
            } catch (IOException ex) {
                try{
                    in.close();
                    clientSocket.close();
                } catch(IOException ex1){
                    throw new RuntimeException(ex);
                }
            }
            if ("exit".equals(clientMessage)) {
                System.out.println("\nConnection for Client " + numberClient + " is terminated (" + currentTime + ")\n");
                try {
                    in.close();
                    clientSocket.close();
                } catch (IOException ex) {
                    System.err.println("Error closing resources: " + ex.getMessage());
                }
                break;
            }

            System.out.println("Client #" + numberClient + ": " + clientMessage + " (" + currentTime + ")");
            chatServer.sendMessageForAllClient(numberClient, clientMessage);
        }
    }
}