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
import java.util.Map;
import server.model.Game;

/**
 *
 * @author enes
 */
public class Main_Server {

    private ServerSocket mainServerSocket;
    private Thread mainServerThread;
    private final Map<String, Player> players = new HashMap<>();
    private final Map<String, Lobby> activeLobbys = new HashMap<>();
    private final Map<String, Game> activeGames = new HashMap<>();

    protected void start(int port) throws IOException {

        mainServerSocket = new ServerSocket(port);
        System.out.println("ANA SERVER BAŞLATILDI...");
        mainServerThread = new Thread(() -> {
            while (!mainServerSocket.isClosed()) {
                try {

                    Socket clientSocket = mainServerSocket.accept();
                    new ListenAllClients(clientSocket).start();

                } catch (IOException ex) {
                    System.out.println("Hata: " + ex);
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

    protected void startGame(Player player_1, Player player_2, String gameID) {
        Game g = new Game(player_1, player_2);  // lobby deki oyuncuları oyuna ekle
        g.setGameId(gameID);  // lobbyID yi gameID olarak ayarla
        activeGames.put(gameID, g); //oyunu aktif oyunlar listesine ekle

        //oyuncu durumlarını hazır değil yap
        player_1.isReady = false;
        player_2.isReady = false;

        activeLobbys.remove(gameID);  // gameID geldikleri lobbyID ye eşit, oyun başladığı için lobby silindi

    }

    protected void readyToStart(String p_id, String g_id, int matrix[][]) throws IOException {
        Player p = players.get(p_id);
        Game g = activeGames.get(g_id);
        p.isReady = true;
        p.setGameMatrix(matrix);

        if (g.readyToStart()) {
            g.player_1.clientOutput.writeObject("ready_to_start:1"); // send all ready
            g.player_2.clientOutput.writeObject("ready_to_start:2"); // send all ready
        }

    }

    //oyuncuların hazır olma durumlarını düzenler
    protected void setPlayerStatusToReady(String playerId, String lobbyId) throws IOException {
        if (playerId != null && lobbyId != null) {
            Player p = players.get(playerId);
            p.isReady = true;
            Lobby l = activeLobbys.get(lobbyId);
            l.checkPlayerReady();

            System.out.println("username: " + p.userName + " -> Hazır!");

            l.players[0].clientOutput.writeObject("users_status:" + l.players[0].isReady + "/" + l.players[1].isReady); // send player1 to user satatus
            l.players[1].clientOutput.writeObject("users_status:" + l.players[0].isReady + "/" + l.players[1].isReady); // send player2 to user satatus

            if (l.readyToStart) {
                System.out.println("username: " + l.players[0].userName + " | username: " + l.players[1].userName + " -> Hazır!");

                //player 1 e herkesin hazır olduğu bilgisini ve kullanıcı isimlerini gönderir
                l.players[0].clientOutput.writeObject("everyone_ready:" + l.lobbyId + "/" + l.players[0].userName + "/" + l.players[1].userName + "/" + l.players[0].id);
                //player 2 ye herkesin hazır olduğu bilgisini ve kullanıcı isimlerini gönderiri
                l.players[1].clientOutput.writeObject("everyone_ready:" + l.lobbyId + "/" + l.players[1].userName + "/" + l.players[0].userName + "/" + l.players[1].id);

                startGame(l.players[0], l.players[1], l.lobbyId);  // tüm oyuncular hazır olduğu için oyunu başlat
            }
        } else {
            System.out.println("Hata: hazır konumuna getirilecek kullanıcı veya oda bulunamadı!");
        }
    }

    protected void hit(String gameID, String p_id, int x, int y) throws IOException {
        Game g = activeGames.get(gameID);
        Player p = players.get(p_id);
        if (p == g.player_1) {
            int r = g.player_2.checkAndUptadeMatrix(x, y);
            p.clientOutput.writeObject("hit_made:" + r + "/" + x + "/" + y);
            g.player_2.clientOutput.writeObject("came_hit:"+ r + "/" + x + "/" + y);
        }else if (p == g.player_2){
            int r = g.player_1.checkAndUptadeMatrix(x, y);
            p.clientOutput.writeObject("hit_made:" + r + "/" + x + "/" + y);
            g.player_1.clientOutput.writeObject("came_hit:"+ r + "/" + x + "/" + y);
        } else {
            System.out.println("kullanıcı eşleşmedi");
        }
        
        int w = g.gameOver();
        
         if(w == 1){
             g.player_1.clientOutput.writeObject("game_over:1");  //  1 = kazandın
             g.player_2.clientOutput.writeObject("game_over:0");  //  0 = kayettin
         } else if (w == 2){
             g.player_2.clientOutput.writeObject("game_over:1");  //  1 = kazandın
             g.player_1.clientOutput.writeObject("game_over:0");  //  0 = kayettin
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
                // client'dan gelen mesajları okumak için
                clientInput = new ObjectInputStream(clientSocket.getInputStream());
                // server'a bağlı olan client'a mesaj göndermek için
                clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());

                Object receivedMessage;
                Player p = null;   // şuan mesaj gelen kullanıcı

                // client mesaj gönderdiği sürece mesajı al
                while ((receivedMessage = clientInput.readObject()) != null) {

                    //gelen mesajı çıktı olarak yaz
                    System.out.println(this.getName() + " : " + receivedMessage);

                    String command;
                    String content;
                    Object[] o = null;
                    // client'in gönderdiği mesajı komut ve içerik olarak parçala
                    if (receivedMessage instanceof String) {
                        String message[] = receivedMessage.toString().split(":");
                        command = message[0];
                        content = message[1];
                    } else {
                        o = (Object[]) receivedMessage;
                        command = o[0].toString();
                        content = null;
                    }

                    //eğer save_user komutu gelmişse gelen username ile birlikte kullanıcı oluştur ve kayıt et
                    switch (command) {
                        case "save_user": {
                            // save_user:username şeklinde mesaj gönderilmeli
                            p = createPlayer(content);
                            p.clientInput = clientInput;
                            p.clientOutput = clientOutput;

                            // kullanıcı kayıt edildi bilgisini client a gönder
                            sendMessageToClient(p, "user_saved:" + p.id + "/" + p.userName);
                            break;
                        }
                        case "create_lobby": {
                            // parametresiz olarak create_lobby: şeklinde gönderilebilir
                            if (p != null) {
                                Lobby l;
                                l = createLobby(p);

                                // lobby oluşturuldu bilgisi client a gönderilir
                                sendMessageToClient(p, "lobby_created:" + l.lobbyId + "/" + p.userName + "/" + p.id);
                            } else {
                                System.out.println("Kullanıcı bulunamadı!");
                            }
                            break;
                        }
                        case "join_lobby": {
                            // join_lobby:lobbyId şeklinde mesaj gönderilmeli
                            if (p != null) {
                                Lobby l;
                                l = joinLobby(p, content);

                                //lobiye katılma bilgisi client a gönderilir
                                sendMessageToClient(p, "joined_to_lobby:" + l.lobbyId + "/" + l.players[0].userName + "/" + l.players[1].userName + "/" + p.id);
                                sendMessageToClient(l.players[0], "someone_joined:" + l.players[1].userName);
                            } else {
                                System.out.println("Kullanıcı bulunamadı!");
                            }
                            break;
                        }
                        case "im_ready": {
                            // im_ready:userId/lobbyId şeklinde mesaj gönderilmeli
                            String params[] = content.split("/");
                            String userId = params[0];                             // get userId
                            String lobbyId = params[1];                            // get lobbyId
                            setPlayerStatusToReady(userId, lobbyId);               // gelen player ı hazır olarak set et     
                            break;
                        }
                        case "start_game": {
                            // start_game:gameID/UserId
                            String gameID = o[1].toString();
                            String userID = o[2].toString();
                            int matrix[][] = (int[][]) o[3];

                            readyToStart(userID, gameID, matrix);
                            break;
                        }
                        case "hit": {
                            String params[] = content.split("/");
                            String gameID = params[0];
                            String playerId = params[1];
                            int x = Integer.parseInt(params[2]);
                            int y = Integer.parseInt(params[3]);
                            hit(gameID, playerId, x, y);
                            break;
                        }
                        default:
                            //herhangi bir komut ile eşleşme sağlanamazsa
                            System.out.println("Server a gelen komut anlaşılamadı!: " + command);
                            break;
                    }
                }

            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Hata:: " + ex);
            } finally {
                try {
                    if (clientInput != null) {
                        clientInput.close();
                    }
                    if (clientOutput != null) {
                        clientOutput.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                    System.out.println("Kapatıldı: " + clientSocket);
                } catch (IOException ex) {
                    System.out.println("Hata: " + ex);
                }
            }
        }
    }

}
