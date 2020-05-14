/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.UUID;

/**
 *
 * @author enes
 */
public class Lobby {
    boolean readyToStart = false;
   
    String lobbyId;
    
    Player players[] = new Player[2];
    
        
    public void generateLobbyId(){
        String lid = UUID.randomUUID().toString();
        lid = lid.replaceAll("-","");
        int i =(int)(Math.random()*(lid.length()-8));
        lid = lid.substring(i,i+6);
        this.lobbyId = lid ; 
    
    }

    public void checkPlayerReady(){
        if (players[0].isReady == true && players[1].isReady == true)
            this.readyToStart = true;
    }

}
