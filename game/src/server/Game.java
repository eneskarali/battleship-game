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
public class Game {
    
    int gameId;
    
    Player player_1;
    Player player_2;   

    public Game(int gameId, Player player_1, Player player_2) {
        this.gameId = gameId;
        this.player_1 = player_1;
        this.player_2 = player_2;
    }
    
}
