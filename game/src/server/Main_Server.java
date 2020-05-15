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
    private final Map<String, Lobby> activeLobbys = new HashMap<>();

    protected void start(int port) throws IOException {

        mainServerSocket = new ServerSocket(port);
        System.out.println("ANA SERVER BAŞLATILDI...");

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

    protected Player createPlayer(String userName) {
        Player p = new Player("", userName);
        p.generatePlayerId();
        players.put(p.id, p);
        System.out.println("Connected userId:" + p.id);
        return p;
    }

    protected Lobby createLobby(Player p) {
        if (p != null) {
            Lobby l = new Lobby();
            l.players[0] = p;
            l.generateLobbyId();
            activeLobbys.put(l.lobbyId, l);
            System.out.println("lobby id: " + l.lobbyId + "  oluşuturdu");
            return l;
        } else {
            System.out.println("Oda oluşturulamadı: aktif kullanıcı bulunamadı!");
        }
        return null;
    }

    protected void joinLobby(Player p, String lobbyId) {
        if (p != null && lobbyId != null) {
            Lobby l = activeLobbys.get(lobbyId);
            l.players[1] = p;
            System.out.println("player id: " + p.id + " katıldı -> lobby id: " + l.lobbyId);
        } else {
            System.out.println("Oda oluşturulamadı: aktif kullanıcı veya lobi bulunamadı!");
        }
    }

    protected void setPlayerStatusToReady(String playerId, String lobbyId) {
        if (playerId != null && lobbyId != null) {
            Player p = players.get(playerId);
            p.isReady = true;
            Lobby l = activeLobbys.get(lobbyId);
            l.checkPlayerReady();
            if (l.readyToStart) {
                System.out.println("Kullanıcı Hazır!");
            }
        } else {
            System.out.println("Hata: hazır konumuna getirilecek kullanıcı veya oda bulunamadı!");
        }
    }

    protected void sendMessageToClient(Player p, String message) throws IOException {
        p.clientOutput.writeObject(message);
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
                Player p = null;
                // client mesaj gönderdiği sürece mesajı al
                while ((receivedMessage = clientInput.readObject()) != null) {

                    //gelen mesajı çıktı olarak yaz
                    System.out.println(this.getName() + " : " + receivedMessage);

                    // client'in gönderdiği mesajı komut ve içerik olarak parçala
                    String message[] = receivedMessage.toString().split(":");
                    String command = message[0];
                    String content = message[1];

                    //eğer save_user komutu gelmişse gelen username ile birlikte kullanıcı oluştur ve kayıt et
                    if (command.equals("save_user")) {                              // save_user:username şeklinde mesaj gönderilmeli
                        p = createPlayer(content);
                        p.clientInput = clientInput;
                        p.clientOutput = clientOutput;
                        sendMessageToClient(p, "user_saved:" + p.id + "/" + p.userName);  // kullanıcı kayıt edildi bilgisini client a gönder
                    } else if (command.equals("create_lobby")) {                    // parametresiz olarak create_lobby: şeklinde gönderilebilir
                        Lobby l = null;
                        l = createLobby(p);
                        sendMessageToClient(p, "lobby_created:" + l.lobbyId + "/" + p.userName); // lobby oluşturuldu bilgisi client a gönderilir
                    } else if (command.equals("join_lobby")) {                      // join_lobby:lobbyId şeklinde mesaj gönderilmeli
                        joinLobby(p, content);
                    } else if (command.equals("im_ready")) {                        // im_ready:userId/lobbyId şeklinde mesaj gönderilmeli
                        String params[] = content.split("/");
                        String userId = params[0];                                  // get userId
                        String lobbyId = params[1];                                 // get lobbyId
                        setPlayerStatusToReady(userId, lobbyId);
                    }

                    // "son" mesajı iletişimi sonlandırır
//                    if (receivedMessage.equals("son")) {
//                        break;
//                    }
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
