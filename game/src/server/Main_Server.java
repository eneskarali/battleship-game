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
import java.util.HashSet;

/**
 *
 * @author enes
 */
public class Main_Server {

    private ServerSocket mainServerSocket;
    private Thread mainServerThread;
    private final HashSet<ObjectOutputStream> allClients = new HashSet<>();

    protected void start(int port) throws IOException {

        mainServerSocket = new ServerSocket(port);
        System.out.println("Ana server başlatıldı...");

        mainServerThread = new Thread(() -> {
            while (!mainServerSocket.isClosed()) {
                try {
                    
                    Socket clientSocket = mainServerSocket.accept();
                    
                    new ListenThread(clientSocket).start();
                    
                } catch (IOException ex) {
                    System.out.println("Hata - new Thread() : " + ex);
                    break;
                }
            }

        });

    }
class ListenThread extends Thread {

        // dinleyeceğimiz client'ın soket nesnesi, input ve output stream'leri
        private final Socket clientSocket;
        private ObjectInputStream clientInput;
        private ObjectOutputStream clientOutput;

        private ListenThread(Socket clientSocket) {
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

                // Bütün client'lara yeni katılan client bilgisini gönderir
                for (ObjectOutputStream out : allClients) {
                    out.writeObject(this.getName() + " server'a katıldı.");
                }

                // broadcast için, yeni gelen client'ın output stream'ını listeye ekler
                allClients.add(clientOutput);

                // client ismini mesaj olarak gönder
                clientOutput.writeObject("@id-" + this.getName());

                Object mesaj;
                // client mesaj gönderdiği sürece mesajı al
                while ((mesaj = clientInput.readObject()) != null) {
                    // client'in gönderdiği mesajı server ekranına yaz
                    System.out.println(this.getName() + " : " + mesaj);

                    // bütün client'lara gelen bu mesajı gönder
                    for (ObjectOutputStream out : allClients) {
                        out.writeObject(this.getName() + ": " + mesaj);
                    }

                    // "son" mesajı iletişimi sonlandırır
                    if (mesaj.equals("son")) {
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
