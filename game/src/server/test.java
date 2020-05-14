/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author enes
 */
public class test {

    private Socket clientSocket;
    private static ObjectInputStream clientInput;
    private static ObjectOutputStream clientOutput;
    private javax.swing.JTextPane historyJTextPane;
    private javax.swing.JLabel nameJLabel;
    private Thread clientThread;

    public static void main(String[] args) throws IOException {

        Main_Server m = new Main_Server();
        m.start(44444);

        Socket client = new Socket("localhost", 44444);
        clientOutput = new ObjectOutputStream(client.getOutputStream());
        clientInput = new ObjectInputStream(client.getInputStream());

        clientOutput.writeObject("save_user:deli");
        clientOutput.writeObject("create_lobby:as");
        Scanner sc = new Scanner(System.in); //System.in is a standard input stream  
        System.out.print("Enter a string: ");
        String lobbyId = sc.nextLine();              //reads string  
        clientOutput.writeObject("join_lobby:"+lobbyId);
         System.out.print("Enter a string: ");
        String playerId = sc.nextLine();   
        clientOutput.writeObject("im_ready:"+playerId+"/"+lobbyId);
    }

    class ListenThread extends Thread {

        // server'dan gelen mesajları dinle
        @Override
        public void run() {
            try {
                System.out.println("Server'a bağlandı ..");

                Object mesaj;
                // server mesaj gönderdiği sürece gelen mesajı al
                while ((mesaj = clientInput.readObject()) != null) {
                    // id mesajı kontrolü, id mesajı alınırsa name etiketini değiştirir 

                    // serverdan gelen mesajı arayüze yaz
                    System.out.println(mesaj);

                    // "son" mesajı iletişimi sonlandırır
                    if (mesaj.equals("son")) {
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Error - ListenThread : " + ex);
            }
        }
    }
}
