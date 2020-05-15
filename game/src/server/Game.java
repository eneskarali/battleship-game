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
    
    String gameId;
    
    Player player_1;
    Player player_2;   

    public Game( Player player_1, Player player_2) {
        this.player_1 = player_1;
        this.player_2 = player_2;
    }
    
    public void setGameId(String id){
        this.gameId = id;
    } 
    
    public void gameOver(){
        int count1=0;
        int count2=0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (this.player_1.gameMatrix[i][j] == 2) 
                    count1+=1;     
                if (this.player_2.gameMatrix[i][j]==2)
                    count2+=1;
            }
        }
        
        if (count1 == 20 || count2 == 20) {
            System.out.println("oyun bitti");
        }
    }
    
}
