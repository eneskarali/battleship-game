/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 *
 * @author enes
 */
public class Player {
   
    String id;
    String userName;
    boolean isReady = false;
    
    ObjectInputStream clientInput;    
    ObjectOutputStream clientOutput;
    
    int gameMatrix[][] = new int[10][10];

    public Player(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }
    
    public void generatePlayerId(){
       String idPlayer = UUID.randomUUID().toString();
       idPlayer = idPlayer.replaceAll("-","");
       idPlayer = idPlayer.substring(0,15);
       
       this.id = idPlayer ;
   
    }
    
    public void setGameMatrix(int matrix[][]){
        this.gameMatrix = matrix; 
    }
 }
