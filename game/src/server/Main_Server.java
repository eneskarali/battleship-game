/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author enes
 */
public class Main_Server {

    private ServerSocket mainServerSocket;
    private Thread mainServerThread;
    private final HashSet<ObjectOutputStream> allClients = new HashSet<>();
    private final Map<String, Player> players = new HashMap<>();
    private final Map<Integer, Lobby> activeLobbys = new HashMap<>();

    
    protected void start(int port) throws IOException {

        mainServerSocket = new ServerSocket(port);
        System.out.println("Ana server başlatıldı...");

        mainServerThread = new Thread(() -> {
            while (!mainServerSocket.isClosed()) {
                try {

                    Socket clientSocket = mainServerSocket.accept();

                    new ListenAllClients(clientSocket).start();

                } catch (IOException ex) {
                    System.out.println("Thread oluşturulamadı : " + ex);
                    break;
                }
            }

        });
        mainServerThread.start();
    }
    
    protected void createLobby(){
        
    }

    class ListenAllClients extends Thread {

        // dinleyeceğimiz client'ın soket nesnesi, input ve output stream'leri
        private final Socket clientSocket;
        private ObjectInputStream clientInput;
        private ObjectOutputStream clientOutput;

        private ListenAllClients(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            System.out.println("Bağlanan client için thread oluşturuldu : " + this.getName());

            try {
                // input  : client'dan gelen mesajları okumak için
                // output : server'a bağlı olan client'a mesaj göndermek için
                clientInput = new ObjectInputStream(clientSocket.getInputStream());
                clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());  

                Object receivedMessage;
                
                // client mesaj gönderdiği sürece mesajı al
                while ((receivedMessage = clientInput.readObject()) != null) {
                    
                    //gelen mesajı çıktı olarak yaz
                    System.out.println(this.getName() + " : " + receivedMessage);
                    
                    // client'in gönderdiği mesajı komut ve içerik olarak parçala
                    String message[] = receivedMessage.toString().split(":");
                    String command = message[0];
                    String content = message[1];
                    
                    //eğer save_user komutu gelmişse gelen username ile birlikte kullanıcı oluştur ve kayıt et
                    if(command.equals("save_user")){
                        Player p = new Player(0, content);
                        players.put(p.userName, p);
                        System.out.println("username:" + p.userName);
                    }
                    else if(command.equals("create_lobby")){
                        
                    }
                    

                    // bütün client'lara gelen bu mesajı gönder
                    for (ObjectOutputStream out : allClients) {
                        out.writeObject(this.getName() + ": " + receivedMessage);
                    }

                    // "son" mesajı iletişimi sonlandırır
                    if (receivedMessage.equals("son")) {
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Hata - ListenThread : " + ex);
            } finally {
                try {
                    // client'ların tutulduğu listeden çıkart
                    allClients.remove(clientOutput);

                    // bütün client'lara ayrılma mesajı gönder
                    for (ObjectOutputStream out : allClients) {
                        out.writeObject(this.getName() + " server'dan ayrıldı.");
                    }

                    // bütün streamleri ve soketleri kapat
                    if (clientInput != null) {
                        clientInput.close();
                    }
                    if (clientOutput != null) {
                        clientOutput.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                    System.out.println("Soket kapatıldı : " + clientSocket);
                } catch (IOException ex) {
                    System.out.println("Hata - Soket kapatılamadı : " + ex);
                }
            }
        }
    }

}
