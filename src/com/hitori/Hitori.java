package com.hitori;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Hitori {
    int rowCol = 5;
    String finishTime = "";
    boolean timeStarted = false;
    Random random;
    String resourceFile;
    int flg = 0;
    boolean won = false;
    String result = "";
    private final String ip = "localhost";
    private final int port = 22222;
    private final int errors = 0;
    private JPanel mainPanel;
    private JPanel gridPane;
    private JPanel detailPane;
    private JLabel submit;
    private JLabel start;
    private JLabel detail;
    private JLabel time;
    private JPanel message;
    private JLabel splash;
    private int playerID;
    private int opponentID;
    private final boolean isSubmitPressed = false;
    private final boolean enemyWon = false;
    private Client cs;
    private int finishedIn;
    private final JLabel[][] btn = new JLabel[rowCol][rowCol];
    private ArrayList<Integer> hitoridata;
    private final GridLayout gridLayout = new GridLayout(rowCol, rowCol);

    public Hitori() throws IOException {
        random = new Random();
        createGrid();
        initialize();
        connect();
        gui();
    }

    public static void main(String[] args) throws IOException {
        Hitori hitori = new Hitori();
        JFrame frame = new JFrame("Hitori");
        frame.setContentPane(hitori.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(250, 100);
        //frame.setSize(350, 410);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    private void gui() {
        submit.setBorder(BorderFactory.createRaisedBevelBorder());
        submit.setFocusable(true);
        submit.addMouseListener(new BtnActionListener());
        start.setBorder(BorderFactory.createRaisedBevelBorder());
        start.setFocusable(true);
        start.addMouseListener(new BtnActionListener());
        //start.setOpaque(false);
        detail.setBorder(BorderFactory.createEtchedBorder());
        detail.setText("Press start to play..");
        time.setBorder(BorderFactory.createEtchedBorder());
        System.out.println(playerID);
        if (playerID == 1) {
            detail.setText("You are Player #1");
            opponentID = 2;
        } else {
            detail.setText("You are Player #2");
            opponentID = 1;
        }
    }

    public void createGrid() throws IOException {
        resourceFile = (random.nextInt(5) + 1) + ".txt";
        System.out.println(resourceFile);
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream resourceStream = classLoader.getResourceAsStream(resourceFile);
        BufferedReader br = null;
        String no = "";
        String[] all;
        if (resourceStream != null) {
            br = new BufferedReader(new InputStreamReader(resourceStream));
            //System.out.println(br.lines().collect(Collectors.joining(" ")));
        } else
            System.out.println("File Not Found");

        mainPanel.setUI(gridPane.getUI());
        mainPanel.setUI(detailPane.getUI());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        gridPane.setLayout(gridLayout);
        gridPane.setSize(300, 300);
        for (int i = 0; i < rowCol && br != null; i++) {
            no = br.readLine().trim();
            all = no.split(" ");
            for (int j = 0; j < rowCol; j++) {
                btn[i][j] = new JLabel(all[j]);
                btn[i][j].addMouseListener(new BtnActionListener());
                btn[i][j].setBorder(BorderFactory.createEtchedBorder());
                btn[i][j].setHorizontalAlignment(0);
                btn[i][j].setOpaque(true);
                gridPane.add(btn[i][j]);
            }
        }
    }

    public void initialize() {
        showGrid(false);
        if (!gridVisible()) {
            removeGrid();
            BoxLayout bl = new BoxLayout(gridPane, BoxLayout.LINE_AXIS);
            gridPane.setLayout(bl);
            String rules = "<html><div style=\"text-align:center\">--------------Rules------------<br/>" +
                    "1. Eleminate the duplicate numbers<br/>" +
                    "2. No adjacent cells should be black<br/" +
                    "3. After solving press submit &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<br/>4. If incorrect try again then submit &nbsp&nbsp</div>";
            splash.setText(rules);

        }
    }

    private void removeGrid() {
        for (int i = 0; i < rowCol; i++) {
            for (int j = 0; j < rowCol; j++) {
                gridPane.remove(btn[i][j]);
            }
        }
    }

    public boolean isAnySelected() {
        boolean isSelected = false;
        for (int i = 0; i < rowCol; i++) {
            for (int j = 0; j < rowCol; j++) {
                if (btn[i][j].getBackground().equals(Color.gray)) {
                    isSelected = true;
                }
            }
        }
        return isSelected;
    }

    private void showGrid(boolean bool) {
        for (int i = 0; i < rowCol; i++) {
            for (int j = 0; j < rowCol; j++) {
                btn[i][j].setVisible(bool);
            }
        }
    }

    private boolean gridVisible() {
        boolean flag = false;
        for (int i = 0; i < rowCol; i++) {
            for (int j = 0; j < rowCol; j++) {
                if (btn[i][j].isVisible())
                    flag = true;
            }
        }
        return flag;
    }

    public boolean isValid() {
        boolean flag = true;
        for (int i = 0; i < rowCol; i++) {
            for (int j = 0; j < rowCol; j++) {
                if (btn[i][j].getBackground().equals(Color.gray)) {
                    if (j != 0 && (j - 1) >= 0 && btn[i][j - 1].getBackground().equals(Color.gray)) {
                        flag = false;
                        return flag;
                    }
                    if ((j + 1) < 5 && btn[i][j + 1].getBackground().equals(Color.gray)) {
                        flag = false;
                        return flag;
                    }
                    if ((i + 1) < 5 && btn[i + 1][j].getBackground().equals(Color.gray)) {
                        flag = false;
                        return flag;
                    }
                    if (i != 0 && (i - 1) >= 0 && btn[i - 1][j].getBackground().equals(Color.gray)) {
                        flag = false;
                        return flag;
                    }
                }
            }
        }
        return flag;
    }

    private int[] count(String data, int row, int col) {
        int inRow = 0, inCol = 0;
        int[] cnt = new int[2];
        System.out.println("data" + data);
        for (int j = 0; j < rowCol; j++) {
            if (btn[row][j].getText().equalsIgnoreCase(data) && !(btn[row][j].getBackground().equals(Color.gray))) {
                inCol++;
            }
        }
        for (int i = 0; i < rowCol; i++) {
            if (btn[i][col].getText().equalsIgnoreCase(data) && !(btn[i][col].getBackground().equals(Color.gray))) {
                inRow++;
            }
        }
        cnt[0] = inCol;
        cnt[1] = inRow;
        return cnt;
    }

    private boolean process() {
        int[] rcCount;
        int rcnt = 0;
        int ccnt = 0;
        boolean sts = false;
        ArrayList<Integer> listRow = new ArrayList<>();
        ArrayList<Integer> listCol = new ArrayList<>();
        if (!isValid()) {
            detail.setText("Adjacent cells cannot be Black");
            detail.setBackground(Color.red);
            return sts;
        } /*else if (!isAnySelected()) {
            detail.setText("No cell selected");
            detail.setBackground(Color.YELLOW);
        } */ else {
            for (int i = 0; i < rowCol; i++) {
                for (int j = 0; j < rowCol; j++) {
                    if (!(btn[i][j].getBackground().equals(Color.gray))) {
                        //System.out.println(i + " " + j);
                        rcCount = count(btn[i][j].getText(), i, j);
                        listRow.add(rcCount[0]);
                        listCol.add(rcCount[1]);
                    }
                }
            }

            for (int i = 0; i < listRow.size(); i++) {
                if (listRow.get(i) > 1) {
                    rcnt++;
                }
            }

            for (int i = 0; i < listCol.size(); i++) {
                if (listCol.get(i) > 1) {
                    ccnt++;
                }
            }

            if (rcnt == 0 && ccnt == 0) {
                detail.setOpaque(false);
                detail.setBackground(Color.green);
                sts = true;
                return sts;
            } else {
                //detail.setText("Incorrect");
                if (detail.getBackground().equals(Color.green)) {
                    detail.setBackground(Color.red);
                } else
                    detail.setBackground(Color.red);
                return sts;
            }
        }
        //return sts;
    }

    private void connect() {
        cs = new Client();
    }

    private class BtnActionListener implements MouseListener {
        JLabel source;

        @Override
        public void mouseClicked(MouseEvent e) {
            source = (JLabel) e.getSource();
            for (int i = 0; i < rowCol; i++) {
                for (int j = 0; j < rowCol; j++) {
                    if (!(source.getBackground() == Color.GRAY) && !(source.getText().equals("start")) && !(source.getText().equals("submit"))) {
                        source.setBackground(Color.GRAY);
                    } else
                        source.setBackground(Color.getColor("#BBBBBB"));
                }
            }

            if (source.getText().equals("submit")) {
                if (gridVisible()) {
                    if (won == false)
                        won = process();
                    if (won && flg == 0) {
                        detail.setText("Correct answer");
                        finishedIn = (180 - Integer.parseInt(time.getText()));
                        System.out.println(finishedIn);
                        flg++;
                        cs.sendMsg(playerID, detail.getText(), finishedIn);
                    }
                    if (detail.getText().equalsIgnoreCase("time up") || detail.getText().equalsIgnoreCase("cannot play! Time up")) {
                        detail.setText("cannot play! Time up");
                        submit.setEnabled(false);
                    }
                    if (flg >= 1) {
                        detail.setText("You Won");
                        source.disable();
                    }
                    if (flg > 1 && (!detail.getText().equalsIgnoreCase("You Lost") || !detail.getText().equalsIgnoreCase("Lost the game cannot try again") || !detail.getText().equalsIgnoreCase("You Won"))) {
                        detail.setText("Incorrect.Try again.");
                    }
                    if (detail.getText().equalsIgnoreCase("You Lost") || detail.getText().equalsIgnoreCase("Lost the game cannot try again")) {
                        source.disable();
                        detail.setText("Lost the game cannot try again");
                    }

                    if (detail.getText().equalsIgnoreCase("You Lost") || detail.getText().equalsIgnoreCase("Lost the game cannot try again")) {
                        source.disable();
                        //source.setVisible(false);
                        detail.setText("Lost the game cannot try again");

                    }
                    if (detail.getText().equalsIgnoreCase("time up") || detail.getText().equalsIgnoreCase("cannot play! Time up")) {
                        detail.setText("cannot play! Time up");
                    }
                } else {
                    detail.setText("Press start to play..");
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            source = (JLabel) e.getSource();
            timeStarted = false;
            if (source.getText().equals("start") && timeStarted == false && !gridVisible() && (detail.getText().equalsIgnoreCase("Press start to play..") || detail.getText().contains("You are Player #"))) {
                detail.setText("Game Started");
                source.setBackground(Color.lightGray);
                source.setBorder(BorderFactory.createEtchedBorder());
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    int i = 180;

                    @Override
                    public void run() {
                        if (i == 0 && !(detail.getText().equalsIgnoreCase("correct"))) {
                            detail.setText("Time up");
                        }
                        if (i == 0 && (detail.getText().equalsIgnoreCase("correct"))) {
                            detail.setText("Correct");
                        }
                        if (i == 0 && (detail.getText().equalsIgnoreCase("Incorrect.Try again."))) {
                            detail.setText("Time up");
                        }
                        if (detail.getText().equalsIgnoreCase("correct")) {
                            finishedIn = (180 - i);
                            //detail.setText("Finished in " + finishedIn + " sec");
                            //finishTime = detail.getText();
                        }
                        if (i >= 0) {
                            time.setText(Integer.toString(i--));
                        }
                        if (i < 5) {
                            time.setBackground(Color.RED);
                        }
                        if (detail.getText().equalsIgnoreCase("You Won") || detail.getText().equalsIgnoreCase("You Lost")) {
                            cs.close();
                            timer.cancel();
                            submit.disable();
                        }
                        //cs.receiveMsg();
                    }
                };
                timer.schedule(task, 1000, 900);
                timeStarted = true;
                gridPane.remove(splash);
                try {
                    createGrid();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (source.getText().equals("submit")) {
                if (source.isEnabled()) {
                    source.setBackground(Color.lightGray);
                    source.setBorder(BorderFactory.createEtchedBorder());
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            source = (JLabel) e.getSource();
            if (source.getText().equals("start") && source.getBorder() == BorderFactory.createEtchedBorder()) {
                source.setBorder(BorderFactory.createRaisedBevelBorder());
                source.setBackground(Color.getColor("#BBBBBB"));
            }

            if (source.getText().equals("submit") && source.getBorder() == BorderFactory.createEtchedBorder()) {
                source.setBorder(BorderFactory.createRaisedBevelBorder());
                source.setBackground(Color.getColor("#BBBBBB"));
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    //Client-side Connection
    private class Client {
        private final int port = 22222;
        private Socket socket;
        private final String ip = "localhost";
        private DataInputStream in;
        private DataOutputStream out;

        public Client() {
            System.out.println("------Client-----");
            try {
                socket = new Socket(ip, port);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                playerID = in.readInt();
                System.out.println("Connected to server Player #" + playerID);
                ClientSide cs = new ClientSide(socket);
                Thread t = new Thread(cs);
                t.start();

            } catch (IOException e) {
                System.out.println("IOException from Client Side constructor");
            }
        }

        private void sendMsg(int id, String status, int time) {
            try {
                out.writeInt(id);
                out.flush();
                out.writeUTF(status);
                out.flush();
                out.writeInt(time);
                out.flush();
            } catch (IOException e) {
                System.out.println("IOException in client sendMsg");
            }
        }

        private void close() {
            try {
                socket.close();
                System.out.println("-----Connection Closed----");
            } catch (IOException e) {
                System.out.println("IOException from close Connection");
            }
        }
    }

    private class ClientSide implements Runnable {
        private final Socket socket;
        private DataInputStream din;
        private DataOutputStream dos;
        private String msg;


        public ClientSide(Socket s) {
            socket = s;
            try {
                din = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("IOException from run() server side");
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    result = din.readUTF();
                    if (result.equalsIgnoreCase("You Won")) {
                        detail.setText(result);
                        break;
                    }
                    if (result.equalsIgnoreCase("You Lost")) {
                        detail.setText(result);
                        detail.setBackground(Color.red);
                        break;
                    }
                }
                //cs.close();
            } catch (IOException e) {
                System.out.println("IOException from run server side");
            }
        }

    }

}
