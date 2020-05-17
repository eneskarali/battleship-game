/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 *
 * @author enes
 */
public class Player {

    public String id;
    public String userName;
    public boolean isReady = false;

    public ObjectInputStream clientInput;
    public ObjectOutputStream clientOutput;

    public int gameMatrix[][] = new int[10][10];

    public Player(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public void generatePlayerId() {
        String idPlayer = UUID.randomUUID().toString();
        idPlayer = idPlayer.replaceAll("-", "");
        idPlayer = idPlayer.substring(0, 15);

        this.id = idPlayer;

    }

    public void setGameMatrix(int matrix[][]) {
        this.gameMatrix = matrix;
    }

    public void fillMatix(int dizi[][]) {

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                this.gameMatrix[i][j] = 0;
            }
        }

        for (int k = 0; k < dizi.length; k++) {
            int x, y;

            x = dizi[k][0];
            y = dizi[k][1];

            this.gameMatrix[x][y] = 1;
        }

    }

    public int checkAndUptadeMatrix(int x, int y) {
        if (this.gameMatrix[x][y] == 1) {
            this.gameMatrix[x][y] = 2;
            return 2;
        } else if (this.gameMatrix[x][y] == 0) {
            this.gameMatrix[x][y] = -1;
            return -1;
        }
        return 3;
    }

}
