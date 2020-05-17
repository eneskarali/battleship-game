/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.pages;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author Faruk KAAN
 */
public class Game_Page extends javax.swing.JFrame {

    /**
     * Creates new form playerScreen
     */
    public ObjectOutputStream clientOutput;

    String p1_name, p2_name, lobby_id, userID;

    public int row, col, erow, ecol, shipSize, remainingShipSize;
    public int prevCoords[];
    boolean aShipSelected = false;
    public int shipMatrix[][] = new int[10][10];
    JButton myMatrix[][] = new JButton[10][10];
    JButton enemyMatrix[][] = new JButton[10][10];

    class ButtonPressed implements ActionListener {

        int r, c;

        public ButtonPressed(int row, int col) {
            r = row;
            c = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            row = r;
            col = c;
            if (myMatrix[row][col].getBackground().getRGB() != -16777216) {
                placeShips(shipSize);
            }
        }
    }

    class enemyButtonPressed implements ActionListener {

        int r, c;

        public enemyButtonPressed(int row, int col) {
            r = row;
            c = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            erow = r;
            ecol = c;
            if(enemyMatrix[erow][ecol].getBackground().getRGB() != -65536 && enemyMatrix[erow][ecol].getBackground().getRGB() != -16776961 ){
                try {
                    clientOutput.writeObject("hit:"+lobby_id+"/"+userID+"/"+erow+"/"+ecol);
                } catch (IOException ex) {
                    Logger.getLogger(Game_Page.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public Game_Page(ObjectOutputStream out, String lID, String p1Name, String p2Name, String uId) {
        initComponents();

        prevCoords = new int[2];
        btn_basla.setEnabled(false);

        this.clientOutput = out;
        this.p1_name = p1Name;
        this.p2_name = p2Name;
        this.lobby_id = lID;
        this.userID = uId;

        jLabel2.setText(p1Name + " (You)");
        jLabel3.setText(p2Name);
        jLabel5.setText("GAME ID: " + lobby_id);

        int row = 0, col = 0;
        Component c[] = jPanel1.getComponents();
        Component enemyC[] = jPanel2.getComponents();
        for (int i = 0; i < c.length; i++) {
            JButton myb = (JButton) c[i];
            myb.setEnabled(false);
            myMatrix[row][col] = myb;
            JButton enemyb = (JButton) enemyC[i];
            enemyb.setEnabled(false);
            enemyMatrix[row][col] = enemyb;
            myMatrix[row][col].addActionListener(new ButtonPressed(row, col));
            enemyMatrix[row][col].addActionListener(new enemyButtonPressed(row, col));
            col++;
            if (col == 10) {
                row++;
                col = 0;
            }
        }

    }

    public void setAllElementsEnablty(boolean status, JButton matrix[][]) {
        for (JButton[] buttons : matrix) {
            for (JButton btn : buttons) {

                if (btn.getBackground().getRGB() != -16777216) {
                    btn.setEnabled(status);
                }

            }
        }
    }

    public void setEnemyMatrix(boolean status) {
        for (JButton[] jButtons : enemyMatrix) {
            for (JButton jButton : jButtons) {
                jButton.setEnabled(status);
            }
        }
    }
    
    public void turn(boolean status){
        for (JButton[] jButtons : enemyMatrix) {
            for (JButton b : jButtons) {
                if(b.getBackground().getRGB() != -65536 && b.getBackground().getRGB() != -16776961 && b.getBackground().getRGB() != -16777216)
                b.setEnabled(status);
            }
        }
    }

    public void readyAll(String who) {
        if (who.equals("1")) {
            jLabel6.setText("Started! Your turn, Take a SHOT!");
            setEnemyMatrix(true);
        } else {
            jLabel6.setText("Started! Waiting For The Your Enemy's Shot...");
        }
        btn_basla.setText("STARTED");
    }
    
    public void made_hit(int r, int x, int y){
        if(r ==2){
            enemyMatrix[x][y].setBackground(Color.RED);
        }
        else if (r == -1){
            enemyMatrix[x][y].setBackground(Color.BLUE);
        }
        turn(false);
        jLabel6.setText("Waiting For The Your Enemy's Shot...");
    }
    
    public void came_hit(int r, int x, int y){
         myMatrix[x][y].setEnabled(true);
        if(r ==2){
            myMatrix[x][y].setBackground(Color.RED);
        }
        else if (r == -1){
            myMatrix[x][y].setBackground(Color.BLUE);
        }
        turn(true);
        jLabel6.setText("Your turn, Take a SHOT!");
    }
    
    public void gameOver(int w) throws IOException{
        if(w == 1){
            JOptionPane.showMessageDialog(this, "YOU LOSE!");
            this.setVisible(false);
            Login_Page l = new Login_Page();
            l.setVisible(true);
        } else if(w == 0){
            JOptionPane.showMessageDialog(this, "YOU WIN!");
            this.setVisible(false);
            Login_Page l = new Login_Page();
            l.setVisible(true);
        }
    }
    public void checkAlignmentEnd(boolean status) {
        btn_basla.setEnabled(status);
    }

    public void placeShips(int size) {
        boolean validPlacement[] = new boolean[4];
        Arrays.fill(validPlacement, Boolean.TRUE);
        if (!aShipSelected) {
            setAllElementsEnablty(false, myMatrix);
            myMatrix[row][col].setEnabled(true);
            myMatrix[row][col].setBackground(Color.black);
            shipMatrix[row][col] = 1;
            for (int i = 0; i < 4; i++) {
                for (int j = 1; j < size; j++) {
                    if (i == 0 && (row + j) <= 9 && shipMatrix[row + j][col] == 1) {
                        validPlacement[i] = false;
                    }
                    if (i == 1 && row > j && shipMatrix[row - j][col] == 1) {
                        validPlacement[i] = false;
                    }
                    if (i == 2 && (col + j) <= 9 && shipMatrix[row][col + j] == 1) {
                        validPlacement[i] = false;
                    }
                    if (i == 3 && col > j && shipMatrix[row][col - j] == 1) {
                        validPlacement[i] = false;
                    }
                }
            }
            if (row < 9 && (9 - row) >= (size - 1) && validPlacement[0]) {
                myMatrix[row + 1][col].setEnabled(true);
            }
            if (row > 0 && row >= (size - 1) && validPlacement[1]) {
                myMatrix[row - 1][col].setEnabled(true);
            }

            if (col < 9 && (9 - col) >= (size - 1) && validPlacement[2]) {
                myMatrix[row][col + 1].setEnabled(true);
            }
            if (col > 0 && col >= (size - 1) && validPlacement[3]) {
                myMatrix[row][col - 1].setEnabled(true);
            }

            aShipSelected = true;
            remainingShipSize = size - 1;
            prevCoords[0] = row;
            prevCoords[1] = col;
        } else {
            if (remainingShipSize > 0) {

                int rowCoe = prevCoords[0] - row;
                int colCoe = prevCoords[1] - col;
                myMatrix[row][col].setEnabled(true);
                myMatrix[row][col].setBackground(Color.black);
                shipMatrix[row][col] = 1;

                if (remainingShipSize > 1 && row - rowCoe < 10 && row - rowCoe >= 0 && col - colCoe < 10 && col - colCoe >= 0) {
                    myMatrix[row - rowCoe][col - colCoe].setEnabled(true);
                } else {
                    btn_battleship.setEnabled(btn_battleship.isEnabled() == false && btn_battleship.getBackground().getRGB() != -16750849);
                    btn_carrier.setEnabled(btn_carrier.isEnabled() == false && btn_carrier.getBackground().getRGB() != -16750849);
                    btn_patrol.setEnabled(btn_patrol.isEnabled() == false && btn_patrol.getBackground().getRGB() != -16750849);
                    btn_seawolf.setEnabled(btn_seawolf.isEnabled() == false && btn_seawolf.getBackground().getRGB() != -16750849);
                    aShipSelected = false;
                    boolean isAlignmentEnd = !btn_battleship.isEnabled() && !btn_carrier.isEnabled() && !btn_patrol.isEnabled() && !btn_seawolf.isEnabled();
                    checkAlignmentEnd(isAlignmentEnd);
                }

                if (remainingShipSize == size - 1 && prevCoords[0] + rowCoe < 10 && prevCoords[0] + rowCoe >= 0 && prevCoords[1] + colCoe < 10 && prevCoords[1] + colCoe >= 0 && shipMatrix[prevCoords[0] + rowCoe][prevCoords[1] + colCoe] == 0) {
                    myMatrix[prevCoords[0] + rowCoe][prevCoords[1] + colCoe].setEnabled(false);
                }
                if (prevCoords[0] - colCoe < 10 && prevCoords[0] - colCoe >= 0 && prevCoords[1] - rowCoe < 10 && prevCoords[1] - rowCoe >= 0 && shipMatrix[prevCoords[0] - colCoe][prevCoords[1] - rowCoe] == 0) {
                    myMatrix[prevCoords[0] - colCoe][prevCoords[1] - rowCoe].setEnabled(false);
                }
                if (prevCoords[0] + colCoe < 10 && prevCoords[0] + colCoe >= 0 && prevCoords[1] + rowCoe < 10 && prevCoords[1] + rowCoe >= 0 && shipMatrix[prevCoords[0] + colCoe][prevCoords[1] + rowCoe] == 0) {
                    myMatrix[prevCoords[0] + colCoe][prevCoords[1] + rowCoe].setEnabled(false);
                }
                prevCoords[0] = row;
                prevCoords[1] = col;
                remainingShipSize--;

            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jButton77 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jButton81 = new javax.swing.JButton();
        jButton82 = new javax.swing.JButton();
        jButton83 = new javax.swing.JButton();
        jButton84 = new javax.swing.JButton();
        jButton85 = new javax.swing.JButton();
        jButton86 = new javax.swing.JButton();
        jButton87 = new javax.swing.JButton();
        jButton88 = new javax.swing.JButton();
        jButton89 = new javax.swing.JButton();
        jButton90 = new javax.swing.JButton();
        jButton91 = new javax.swing.JButton();
        jButton92 = new javax.swing.JButton();
        jButton93 = new javax.swing.JButton();
        jButton94 = new javax.swing.JButton();
        jButton95 = new javax.swing.JButton();
        jButton96 = new javax.swing.JButton();
        jButton97 = new javax.swing.JButton();
        jButton98 = new javax.swing.JButton();
        jButton99 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton101 = new javax.swing.JButton();
        jButton102 = new javax.swing.JButton();
        jButton103 = new javax.swing.JButton();
        jButton104 = new javax.swing.JButton();
        jButton105 = new javax.swing.JButton();
        jButton106 = new javax.swing.JButton();
        jButton107 = new javax.swing.JButton();
        jButton108 = new javax.swing.JButton();
        jButton109 = new javax.swing.JButton();
        jButton110 = new javax.swing.JButton();
        jButton111 = new javax.swing.JButton();
        jButton112 = new javax.swing.JButton();
        jButton113 = new javax.swing.JButton();
        jButton114 = new javax.swing.JButton();
        jButton115 = new javax.swing.JButton();
        jButton116 = new javax.swing.JButton();
        jButton117 = new javax.swing.JButton();
        jButton118 = new javax.swing.JButton();
        jButton119 = new javax.swing.JButton();
        jButton120 = new javax.swing.JButton();
        jButton121 = new javax.swing.JButton();
        jButton122 = new javax.swing.JButton();
        jButton123 = new javax.swing.JButton();
        jButton124 = new javax.swing.JButton();
        jButton125 = new javax.swing.JButton();
        jButton126 = new javax.swing.JButton();
        jButton127 = new javax.swing.JButton();
        jButton128 = new javax.swing.JButton();
        jButton129 = new javax.swing.JButton();
        jButton130 = new javax.swing.JButton();
        jButton131 = new javax.swing.JButton();
        jButton132 = new javax.swing.JButton();
        jButton133 = new javax.swing.JButton();
        jButton134 = new javax.swing.JButton();
        jButton135 = new javax.swing.JButton();
        jButton136 = new javax.swing.JButton();
        jButton137 = new javax.swing.JButton();
        jButton138 = new javax.swing.JButton();
        jButton139 = new javax.swing.JButton();
        jButton140 = new javax.swing.JButton();
        jButton141 = new javax.swing.JButton();
        jButton142 = new javax.swing.JButton();
        jButton143 = new javax.swing.JButton();
        jButton144 = new javax.swing.JButton();
        jButton145 = new javax.swing.JButton();
        jButton146 = new javax.swing.JButton();
        jButton147 = new javax.swing.JButton();
        jButton148 = new javax.swing.JButton();
        jButton149 = new javax.swing.JButton();
        jButton150 = new javax.swing.JButton();
        jButton151 = new javax.swing.JButton();
        jButton152 = new javax.swing.JButton();
        jButton153 = new javax.swing.JButton();
        jButton154 = new javax.swing.JButton();
        jButton155 = new javax.swing.JButton();
        jButton156 = new javax.swing.JButton();
        jButton157 = new javax.swing.JButton();
        jButton158 = new javax.swing.JButton();
        jButton159 = new javax.swing.JButton();
        jButton160 = new javax.swing.JButton();
        jButton161 = new javax.swing.JButton();
        jButton162 = new javax.swing.JButton();
        jButton163 = new javax.swing.JButton();
        jButton164 = new javax.swing.JButton();
        jButton165 = new javax.swing.JButton();
        jButton166 = new javax.swing.JButton();
        jButton167 = new javax.swing.JButton();
        jButton168 = new javax.swing.JButton();
        jButton169 = new javax.swing.JButton();
        jButton170 = new javax.swing.JButton();
        jButton171 = new javax.swing.JButton();
        jButton172 = new javax.swing.JButton();
        jButton173 = new javax.swing.JButton();
        jButton174 = new javax.swing.JButton();
        jButton175 = new javax.swing.JButton();
        jButton176 = new javax.swing.JButton();
        jButton177 = new javax.swing.JButton();
        jButton178 = new javax.swing.JButton();
        jButton179 = new javax.swing.JButton();
        jButton180 = new javax.swing.JButton();
        jButton181 = new javax.swing.JButton();
        jButton182 = new javax.swing.JButton();
        jButton183 = new javax.swing.JButton();
        jButton184 = new javax.swing.JButton();
        jButton185 = new javax.swing.JButton();
        jButton186 = new javax.swing.JButton();
        jButton187 = new javax.swing.JButton();
        jButton188 = new javax.swing.JButton();
        jButton189 = new javax.swing.JButton();
        jButton190 = new javax.swing.JButton();
        jButton191 = new javax.swing.JButton();
        jButton192 = new javax.swing.JButton();
        jButton193 = new javax.swing.JButton();
        jButton194 = new javax.swing.JButton();
        jButton195 = new javax.swing.JButton();
        jButton196 = new javax.swing.JButton();
        jButton197 = new javax.swing.JButton();
        jButton198 = new javax.swing.JButton();
        jButton199 = new javax.swing.JButton();
        jButton200 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btn_carrier = new javax.swing.JButton();
        btn_battleship = new javax.swing.JButton();
        btn_seawolf = new javax.swing.JButton();
        btn_patrol = new javax.swing.JButton();
        btn_basla = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);

        jPanel2.setLayout(new java.awt.GridLayout(10, 10));

        jButton1.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton1);

        jButton2.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton2);

        jButton3.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton3);

        jButton4.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton4);

        jButton5.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton5);

        jButton6.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton6);

        jButton7.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton7);

        jButton8.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton8);

        jButton9.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton9);

        jButton10.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton10);

        jButton11.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton11);

        jButton12.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton12);

        jButton13.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton13);

        jButton14.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton14);

        jButton15.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton15);

        jButton16.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton16);

        jButton17.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton17);

        jButton18.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton18);

        jButton19.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton19);

        jButton20.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton20);

        jButton21.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton21);

        jButton22.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton22);

        jButton23.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton23);

        jButton24.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton24);

        jButton25.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton25);

        jButton26.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton26);

        jButton27.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton27);

        jButton28.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton28);

        jButton29.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton29);

        jButton30.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton30);

        jButton31.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton31);

        jButton32.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton32);

        jButton33.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton33);

        jButton34.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton34);

        jButton35.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton35);

        jButton36.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton36);

        jButton37.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton37);

        jButton38.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton38);

        jButton39.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton39);

        jButton40.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton40);

        jButton41.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton41);

        jButton42.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton42);

        jButton43.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton43);

        jButton44.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton44);

        jButton45.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton45);

        jButton46.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton46);

        jButton47.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton47);

        jButton48.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton48);

        jButton49.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton49);

        jButton50.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton50);

        jButton51.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton51);

        jButton52.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton52);

        jButton53.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton53);

        jButton54.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton54);

        jButton55.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton55);

        jButton56.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton56);

        jButton57.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton57);

        jButton58.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton58);

        jButton59.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton59);

        jButton60.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton60);

        jButton61.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton61);

        jButton62.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton62);

        jButton63.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton63);

        jButton64.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton64);

        jButton65.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton65);

        jButton66.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton66);

        jButton67.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton67);

        jButton68.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton68);

        jButton69.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton69);

        jButton70.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton70);

        jButton71.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton71);

        jButton72.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton72);

        jButton73.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton73);

        jButton74.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton74);

        jButton75.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton75);

        jButton76.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton76);

        jButton77.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton77);

        jButton78.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton78);

        jButton79.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton79);

        jButton80.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton80);

        jButton81.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton81);

        jButton82.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton82);

        jButton83.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton83);

        jButton84.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton84);

        jButton85.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton85);

        jButton86.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton86);

        jButton87.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton87);

        jButton88.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton88);

        jButton89.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton89);

        jButton90.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton90);

        jButton91.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton91);

        jButton92.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton92);

        jButton93.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton93);

        jButton94.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton94);

        jButton95.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton95);

        jButton96.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton96);

        jButton97.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton97);

        jButton98.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton98);

        jButton99.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton99);

        jButton100.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel2.add(jButton100);

        jPanel1.setLayout(new java.awt.GridLayout(10, 10));

        jButton101.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton101);

        jButton102.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton102);

        jButton103.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton103);

        jButton104.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton104);

        jButton105.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton105);

        jButton106.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton106);

        jButton107.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton107);

        jButton108.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton108);

        jButton109.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton109);

        jButton110.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton110);

        jButton111.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton111);

        jButton112.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton112);

        jButton113.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton113);

        jButton114.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton114);

        jButton115.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton115);

        jButton116.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton116);

        jButton117.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton117);

        jButton118.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton118);

        jButton119.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton119);

        jButton120.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton120);

        jButton121.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton121);

        jButton122.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton122);

        jButton123.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton123);

        jButton124.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton124);

        jButton125.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton125);

        jButton126.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton126);

        jButton127.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton127);

        jButton128.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton128);

        jButton129.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton129);

        jButton130.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton130);

        jButton131.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton131);

        jButton132.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton132);

        jButton133.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton133);

        jButton134.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton134);

        jButton135.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton135);

        jButton136.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton136);

        jButton137.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton137);

        jButton138.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton138);

        jButton139.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton139);

        jButton140.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton140);

        jButton141.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton141);

        jButton142.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton142);

        jButton143.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton143);

        jButton144.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton144);

        jButton145.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton145);

        jButton146.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton146);

        jButton147.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton147);

        jButton148.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton148);

        jButton149.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton149);

        jButton150.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton150);

        jButton151.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton151);

        jButton152.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton152);

        jButton153.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton153);

        jButton154.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton154);

        jButton155.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton155);

        jButton156.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton156);

        jButton157.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton157);

        jButton158.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton158);

        jButton159.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton159);

        jButton160.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton160);

        jButton161.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton161);

        jButton162.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton162);

        jButton163.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton163);

        jButton164.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton164);

        jButton165.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton165);

        jButton166.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton166);

        jButton167.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton167);

        jButton168.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton168);

        jButton169.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton169);

        jButton170.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton170);

        jButton171.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton171);

        jButton172.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton172);

        jButton173.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton173);

        jButton174.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton174);

        jButton175.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton175);

        jButton176.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton176);

        jButton177.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton177);

        jButton178.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton178);

        jButton179.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton179);

        jButton180.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton180);

        jButton181.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton181);

        jButton182.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton182);

        jButton183.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton183);

        jButton184.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton184);

        jButton185.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton185);

        jButton186.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton186);

        jButton187.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton187);

        jButton188.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton188);

        jButton189.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton189);

        jButton190.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton190);

        jButton191.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton191);

        jButton192.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton192);

        jButton193.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton193);

        jButton194.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton194);

        jButton195.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton195);

        jButton196.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton196);

        jButton197.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton197);

        jButton198.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton198);

        jButton199.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton199);

        jButton200.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel1.add(jButton200);

        btn_carrier.setText("Carrier (5)");
        btn_carrier.setPreferredSize(new java.awt.Dimension(105, 35));
        btn_carrier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_carrierActionPerformed(evt);
            }
        });

        btn_battleship.setText("Battleship (4)");
        btn_battleship.setPreferredSize(new java.awt.Dimension(105, 35));
        btn_battleship.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_battleshipActionPerformed(evt);
            }
        });

        btn_seawolf.setText("Seawolf (3)");
        btn_seawolf.setPreferredSize(new java.awt.Dimension(105, 35));
        btn_seawolf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_seawolfActionPerformed(evt);
            }
        });

        btn_patrol.setText("Patrol (2)");
        btn_patrol.setPreferredSize(new java.awt.Dimension(105, 35));
        btn_patrol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_patrolActionPerformed(evt);
            }
        });

        btn_basla.setBackground(new java.awt.Color(0, 102, 255));
        btn_basla.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btn_basla.setText("START");
        btn_basla.setPreferredSize(new java.awt.Dimension(105, 35));
        btn_basla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_baslaActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(51, 51, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("First You should line up your ships!");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_seawolf, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(btn_carrier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_battleship, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addComponent(btn_patrol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btn_basla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_carrier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_battleship, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_seawolf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_patrol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_basla, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6)))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jLabel1.setText(" ");

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 153, 51));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("you");

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("your enemy");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("BATTLESHIP GAME");

        jLabel5.setText("GAME ID:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                                .addGap(70, 70, 70)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_carrierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_carrierActionPerformed
        // TODO add your handling code here:
        setAllElementsEnablty(true, myMatrix);
        btn_battleship.setEnabled(false);
        btn_patrol.setEnabled(false);
        btn_seawolf.setEnabled(false);
        btn_carrier.setBackground(new Color(0, 102, 255));
        shipSize = 5;
    }//GEN-LAST:event_btn_carrierActionPerformed

    private void btn_battleshipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_battleshipActionPerformed
        // TODO add your handling code here:
        setAllElementsEnablty(true, myMatrix);
        btn_carrier.setEnabled(false);
        btn_patrol.setEnabled(false);
        btn_seawolf.setEnabled(false);
        btn_battleship.setBackground(new Color(0, 102, 255));
        shipSize = 4;
    }//GEN-LAST:event_btn_battleshipActionPerformed

    private void btn_seawolfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_seawolfActionPerformed
        // TODO add your handling code here:
        setAllElementsEnablty(true, myMatrix);
        btn_battleship.setEnabled(false);
        btn_patrol.setEnabled(false);
        btn_carrier.setEnabled(false);
        btn_seawolf.setBackground(new Color(0, 102, 255));
        shipSize = 3;
    }//GEN-LAST:event_btn_seawolfActionPerformed

    private void btn_patrolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_patrolActionPerformed
        // TODO add your handling code here:
        setAllElementsEnablty(true, myMatrix);
        btn_battleship.setEnabled(false);
        btn_carrier.setEnabled(false);
        btn_seawolf.setEnabled(false);
        btn_patrol.setBackground(new Color(0, 102, 255));
        shipSize = 2;
    }//GEN-LAST:event_btn_patrolActionPerformed

    private void btn_baslaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_baslaActionPerformed
        jLabel6.setText("Waiting for the other player...");
        btn_basla.setEnabled(false);
        try {
            // TODO add your handling code here:
            Object[] data = new Object[4];
            data[0] = "start_game";
            data[1] = lobby_id;
            data[2] = userID;
            data[3] = shipMatrix;
            clientOutput.writeObject(data);  // servera oyunu başlatma isteği gönder.

        } catch (IOException ex) {
            Logger.getLogger(Game_Page.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_btn_baslaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Game_Page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Game_Page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Game_Page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Game_Page.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Game_Page(null, null, null, null, null).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_basla;
    private javax.swing.JButton btn_battleship;
    private javax.swing.JButton btn_carrier;
    private javax.swing.JButton btn_patrol;
    private javax.swing.JButton btn_seawolf;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton101;
    private javax.swing.JButton jButton102;
    private javax.swing.JButton jButton103;
    private javax.swing.JButton jButton104;
    private javax.swing.JButton jButton105;
    private javax.swing.JButton jButton106;
    private javax.swing.JButton jButton107;
    private javax.swing.JButton jButton108;
    private javax.swing.JButton jButton109;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton110;
    private javax.swing.JButton jButton111;
    private javax.swing.JButton jButton112;
    private javax.swing.JButton jButton113;
    private javax.swing.JButton jButton114;
    private javax.swing.JButton jButton115;
    private javax.swing.JButton jButton116;
    private javax.swing.JButton jButton117;
    private javax.swing.JButton jButton118;
    private javax.swing.JButton jButton119;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton120;
    private javax.swing.JButton jButton121;
    private javax.swing.JButton jButton122;
    private javax.swing.JButton jButton123;
    private javax.swing.JButton jButton124;
    private javax.swing.JButton jButton125;
    private javax.swing.JButton jButton126;
    private javax.swing.JButton jButton127;
    private javax.swing.JButton jButton128;
    private javax.swing.JButton jButton129;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton130;
    private javax.swing.JButton jButton131;
    private javax.swing.JButton jButton132;
    private javax.swing.JButton jButton133;
    private javax.swing.JButton jButton134;
    private javax.swing.JButton jButton135;
    private javax.swing.JButton jButton136;
    private javax.swing.JButton jButton137;
    private javax.swing.JButton jButton138;
    private javax.swing.JButton jButton139;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton140;
    private javax.swing.JButton jButton141;
    private javax.swing.JButton jButton142;
    private javax.swing.JButton jButton143;
    private javax.swing.JButton jButton144;
    private javax.swing.JButton jButton145;
    private javax.swing.JButton jButton146;
    private javax.swing.JButton jButton147;
    private javax.swing.JButton jButton148;
    private javax.swing.JButton jButton149;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton150;
    private javax.swing.JButton jButton151;
    private javax.swing.JButton jButton152;
    private javax.swing.JButton jButton153;
    private javax.swing.JButton jButton154;
    private javax.swing.JButton jButton155;
    private javax.swing.JButton jButton156;
    private javax.swing.JButton jButton157;
    private javax.swing.JButton jButton158;
    private javax.swing.JButton jButton159;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton160;
    private javax.swing.JButton jButton161;
    private javax.swing.JButton jButton162;
    private javax.swing.JButton jButton163;
    private javax.swing.JButton jButton164;
    private javax.swing.JButton jButton165;
    private javax.swing.JButton jButton166;
    private javax.swing.JButton jButton167;
    private javax.swing.JButton jButton168;
    private javax.swing.JButton jButton169;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton170;
    private javax.swing.JButton jButton171;
    private javax.swing.JButton jButton172;
    private javax.swing.JButton jButton173;
    private javax.swing.JButton jButton174;
    private javax.swing.JButton jButton175;
    private javax.swing.JButton jButton176;
    private javax.swing.JButton jButton177;
    private javax.swing.JButton jButton178;
    private javax.swing.JButton jButton179;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton180;
    private javax.swing.JButton jButton181;
    private javax.swing.JButton jButton182;
    private javax.swing.JButton jButton183;
    private javax.swing.JButton jButton184;
    private javax.swing.JButton jButton185;
    private javax.swing.JButton jButton186;
    private javax.swing.JButton jButton187;
    private javax.swing.JButton jButton188;
    private javax.swing.JButton jButton189;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton190;
    private javax.swing.JButton jButton191;
    private javax.swing.JButton jButton192;
    private javax.swing.JButton jButton193;
    private javax.swing.JButton jButton194;
    private javax.swing.JButton jButton195;
    private javax.swing.JButton jButton196;
    private javax.swing.JButton jButton197;
    private javax.swing.JButton jButton198;
    private javax.swing.JButton jButton199;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton200;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton82;
    private javax.swing.JButton jButton83;
    private javax.swing.JButton jButton84;
    private javax.swing.JButton jButton85;
    private javax.swing.JButton jButton86;
    private javax.swing.JButton jButton87;
    private javax.swing.JButton jButton88;
    private javax.swing.JButton jButton89;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton90;
    private javax.swing.JButton jButton91;
    private javax.swing.JButton jButton92;
    private javax.swing.JButton jButton93;
    private javax.swing.JButton jButton94;
    private javax.swing.JButton jButton95;
    private javax.swing.JButton jButton96;
    private javax.swing.JButton jButton97;
    private javax.swing.JButton jButton98;
    private javax.swing.JButton jButton99;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
