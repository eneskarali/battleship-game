/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JFrame;

/**
 *
 * @author enes
 */
public class Client_Server_Manager {
    private Socket clientSocket;
    private static ObjectInputStream clientInput;
    private static ObjectOutputStream clientOutput;
    private Thread clientThread;
    private JFrame frame;
    
    protected void start(String host, int port,JFrame activeFrame) throws IOException {
        // client soketi oluşturma (ip + port numarası)
        clientSocket = new Socket(host, port);
        
        //şuan açık olan ekranı değişiklik yapabilmek için getir.
        frame= activeFrame;

        // input  : client'a gelen mesajları okumak için
        // output : client'dan bağlı olduğu server'a mesaj göndermek için
        clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());
        clientInput = new ObjectInputStream(clientSocket.getInputStream());

        // server'ı sürekli dinlemek için Thread oluştur
        clientThread = new ListenThread();
        clientThread.start();
        
    }
    
    protected void sendMessage(String message) throws IOException {
        // gelen mesajı server'a gönder
        clientOutput.writeObject(message);
    }
    
    class ListenThread extends Thread {

        // server'dan gelen mesajları dinle
        @Override
        public void run() {
            try {
                System.out.println("Server'a bağlandı ..");

                Object receivedMessage;
                // server mesaj gönderdiği sürece gelen mesajı al
                while ((receivedMessage = clientInput.readObject()) != null) {
                    //gelen mesajı çıktı olarak yaz
                    System.out.println(this.getName() + " : " + receivedMessage);

                    // client'in gönderdiği mesajı komut ve içerik olarak parçala
                    String message[] = receivedMessage.toString().split(":");
                    String command = message[0];
                    String content = message[1];
                    
                    if (command.equals("user_saved")){
                        lobbyOperations_page opPage = new lobbyOperations_page(clientOutput);
                        frame.setVisible(false);
                        opPage.setVisible(true);
                        String params[] = content.split("/");
                        opPage.setHeader("username: "+params[1] + " / " + "id: "+params[0]);
                    } else if (command.equals("lobby_created")) {
                        
                    }
                    
                    // "son" mesajı iletişimi sonlandırır
                    if (receivedMessage.equals("son")) {
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Error - ListenThread : " + ex);
            }
        }
    }
    
}
