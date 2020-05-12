/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author enes
 */
public class Player {
    int id;
    String userName;
    boolean isReady = false;
    
    int gameMatrix[][] = new int[10][10];

    public Player(int id, String userName) {
        this.id = id;
        this.userName = userName;
    }
    
}
