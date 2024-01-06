package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer implements Runnable {

    private int actualPort;
    private Map<Integer, Socket> mapClient = new TreeMap<Integer, Socket>();
    PrintWriter out;

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(8887)) {
            actualPort = 8887;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = dateFormat.format(new Date());

            System.out.println("Server started by Igor Pron on " + currentDate + " and listening on port " + actualPort);
            System.out.println("Waiting for clients ...");

            int numberClient = 1;
            Socket client = null;

            while (true) {
                client = server.accept();
                Thread clientThread = new Thread(new ClientThread(client, this, numberClient));
                clientThread.setDaemon(true);
                clientThread.start();
                mapClient.put(numberClient, client);
                
                dateFormat = new SimpleDateFormat("HH:mm:ss");
                String currentTime = dateFormat.format(new Date());
                
                System.out.println("Client #" + numberClient + " connected (" + currentTime + ")");
                numberClient++;
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error creating ServerSocket: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void sendMessageForAllClient(int numberClient, String clientMessage){

        if(clientMessage.equals("exit")){
            try {
                mapClient.get(numberClient).close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            mapClient.remove(numberClient);

            try {
                out = new PrintWriter(mapClient.get(numberClient).getOutputStream(), true);
                out.println("exit");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            for(Map.Entry<Integer, Socket> entry : mapClient.entrySet()){
                if(entry.getKey() != numberClient){
                    try {
                        out = new PrintWriter(entry.getValue().getOutputStream(), true);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        String currentTime = dateFormat.format(new Date());
                        
                        out.println("Client #" + numberClient + ": " + clientMessage + " (" + currentTime + ")");
                    } catch (IOException ex) {
                        try {
                            entry.getValue().close();
                            mapClient.remove(entry.getKey());
                        } catch (IOException ex1) {
                            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex1);
                            throw new RuntimeException(ex1);
                        }
                    }
                }
            }
        }
    }
}