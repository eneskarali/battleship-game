/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import client.pages.Game_Page;
import client.pages.Lobby_Page;
import client.pages.Lobby_Operations_Page;
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

    public void start(String host, int port, JFrame activeFrame) throws IOException {

        clientSocket = new Socket(host, port); // gönderilen host ve port için bir client socket oluştur

        frame = activeFrame;  //şuan açık olan ekranı değişiklik yapabilmek için getir

        // client'a gelen mesajları okumak için
        clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());
        // client'dan bağlı olduğu server'a mesaj göndermek için
        clientInput = new ObjectInputStream(clientSocket.getInputStream());

        // server ı dinelmek için Thrad oluştur
        clientThread = new ListenThread();
        clientThread.start();               // dinlemeyi başlat

    }

    public void sendMessage(String message) throws IOException {
        clientOutput.writeObject(message);
    }

    //server dinleyecek Thread class ı
    class ListenThread extends Thread {

        @Override
        public void run() {
            try {
                System.out.println("Server'a bağlandı ..");

                Object receivedMessage;

                // server mesajlarını sürekli dinlemek için göngü
                while ((receivedMessage = clientInput.readObject()) != null) {
                    //genel mesaj
                    System.out.println(this.getName() + " : " + receivedMessage);

                    // client'in gönderdiği mesajı komut ve içerik olarak parçala
                    String message[] = receivedMessage.toString().split(":");
                    String command = message[0];
                    String content = message[1];

                    switch (command) {
                        case "user_saved": {
                            // gelen parametreleri parçala
                            String params[] = content.split("/");  // [0] userid, [1] username

                            // lobby işlemleri sayfasına geçiş yap
                            Lobby_Operations_Page opPage = new Lobby_Operations_Page(clientOutput);
                            opPage.setVisible(true);
                            frame.setVisible(false);

                            opPage.setHeader("username: " + params[1] + " / " + "id: " + params[0]); // ilgili parametreleri sayfa içinde kullan

                            // aktif frame i lobi işlemleri sayfası yap
                            frame = opPage;
                            break;
                        }
                        case "lobby_created": {
                            String params[] = content.split("/");  // [0] lobby id, [1] player1 name , [2] player1 id| player 2 henüz katılmadı

                            // lobi sayfasına geçişi sağla
                            Lobby_Page lobby = new Lobby_Page(clientOutput, params[0], params[2]);
                            lobby.setVisible(true);
                            frame.setVisible(false);

                            // gelen parametreleri sayfa içerisinde kullan
                            lobby.setLobbyId(params[0]);
                            lobby.setPlayer1Name(params[1]);   // player1 odayı oluşturan, player2 odaya katılınca set edilecek
                            lobby.setPlayer2Name("");      // katılmadığından dolayı boş set ediliyor.

                            // aktif frame i lobi sayfası olarak ayarla
                            frame = lobby;
                            break;
                        }
                        case "joined_to_lobby": {
                            String params[] = content.split("/");  // [0] lobbyID, [1] player1, [2] player2, [3] player2 id

                            // lobby sayfasına geçişi sağla
                            Lobby_Page lobby = new Lobby_Page(clientOutput, params[0], params[3]);
                            lobby.setVisible(true);
                            frame.setVisible(false);

                            // gelen parametreleri sayfa içerisinde kullan
                            lobby.setLobbyId(params[0]);
                            lobby.setPlayer1Name(params[1]);
                            lobby.setPlayer2Name(params[2]);
                            lobby.setReadyLabelsVisibiltyTrue();   // ready veya not ready label larını görünür yap
                            lobby.setReadtButtonEnabledTrue();     // ready butonu kulanılabilir

                            // aktif frame i lobi sayfası olarak ayarla
                            frame = lobby;
                            break;
                        }
                        case "someone_joined": {
                            Lobby_Page p = (Lobby_Page) frame;
                            p.setPlayer2Name(content);
                            p.setReadyLabelsVisibiltyTrue();  // ready veya not ready label larını görünür yap
                            p.setReadtButtonEnabledTrue();
                            break;
                        }
                        case "users_status": {
                            String params[] = content.split("/");  // [0] player1 status, [1] player2 status
                            Lobby_Page p = (Lobby_Page) frame;
                            p.setPlayer1ToReady(params[0]);
                            p.setPlayer2ToReady(params[1]);
                            break;
                        }
                        case "everyone_ready": {
                            // gelen parametreleri parçala
                            String params[] = content.split("/"); // [0] lobbyID, [1]player1 name, [2] player2 name
                            
                            //oyun sayfasona geçişi sağla
                            Game_Page gPage = new Game_Page(clientOutput, params[0],params[1],params[2]);
                            gPage.setVisible(true);
                            frame.setVisible(false);
                            
                            //aktif frame i oyun sayfası olarak ayarla
                            frame = gPage;
                            break;
                        }
                        default:
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Hata: " + ex);
            }
        }
    }

}
