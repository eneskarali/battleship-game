/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.model.Player;
import server.model.Lobby;
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

    // server ı durdur
    protected void stop() throws IOException {
        // bütün streamleri ve soketleri kapat
        if (mainServerSocket != null) {
            mainServerSocket.close();
        }
        if (mainServerThread != null) {
            mainServerThread.interrupt();
        }
    }

    //yeni gelen kullanıcıyı listeye kaydet
    protected Player createPlayer(String userName) {
        Player p = new Player("", userName);
        p.generatePlayerId();
        players.put(p.id, p);
        System.out.println("Connected userId:" + p.id);
        return p;
    }

    //lobi oluştur
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

    //lobiye katıl
    protected Lobby joinLobby(Player p, String lobbyId) {
        if (p != null && lobbyId != null) {
            Lobby l = activeLobbys.get(lobbyId);
            l.players[1] = p;
            System.out.println("player id: " + p.id + " katıldı -> lobby id: " + l.lobbyId);
            return l;
        } else {
            System.out.println("Oda oluşturulamadı: aktif kullanıcı veya lobi bulunamadı!");
        }
        return null;
    }

    //oyuncuların hazır olma durumlarını düzenler
    protected void setPlayerStatusToReady(String playerId, String lobbyId) throws IOException {
        if (playerId != null && lobbyId != null) {
            Player p = players.get(playerId);
            p.isReady = true;
            Lobby l = activeLobbys.get(lobbyId);
            l.checkPlayerReady();
            System.out.println(p.userName + " : kullanıcı hazır!");
            l.players[0].clientOutput.writeObject("users_status:" + l.players[0].isReady + "/" + l.players[1].isReady); // send player1 to user satatus
            l.players[1].clientOutput.writeObject("users_status:" + l.players[0].isReady + "/" + l.players[1].isReady); // send player2 to user satatus
            if (l.readyToStart) {
                System.out.println("Tüm Kullanıcılar Hazır!");
            }
        } else {
            System.out.println("Hata: hazır konumuna getirilecek kullanıcı veya oda bulunamadı!");
        }
    }

    //istenen bir client a istenen mesajı gönder
    protected void sendMessageToClient(Player p, String message) throws IOException {
        p.clientOutput.writeObject(message);
    }

    class ListenAllClients extends Thread {

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
                        if (p != null) {
                            Lobby l;
                            l = createLobby(p);
                            sendMessageToClient(p, "lobby_created:" + l.lobbyId + "/" + p.userName + "/" + p.id); // lobby oluşturuldu bilgisi client a gönderilir
                        } else {
                            System.out.println("Kullanıcı bulunamadı!");
                        }
                    } else if (command.equals("join_lobby")) {                      // join_lobby:lobbyId şeklinde mesaj gönderilmeli
                        if (p != null) {
                            Lobby l;
                            l = joinLobby(p, content);
                            sendMessageToClient(p, "joined_to_lobby:" + l.lobbyId + "/" + l.players[0].userName + "/" + l.players[1].userName + "/" + p.id); //lobiye katılma bilgisi client a gönderilir
                            sendMessageToClient(l.players[0], "someone_joined:" + l.players[1].userName);
                        } else {
                            System.out.println("Kullanıcı bulunamadı!");
                        }
                    } else if (command.equals("im_ready")) {                        // im_ready:userId/lobbyId şeklinde mesaj gönderilmeli
                        String params[] = content.split("/");
                        String userId = params[0];                                  // get userId
                        String lobbyId = params[1];                                 // get lobbyId
                        setPlayerStatusToReady(userId, lobbyId);
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
