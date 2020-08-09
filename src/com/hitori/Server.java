package com.hitori;

import java.net.*;
import java.io.*;

public class Server {
    private ServerSocket ss;
    private int playerNum;
    private String ip = "localhost";
    private int port = 22222;
    private ServerSide player1;
    private ServerSide player2;


    private void Server() {
        System.out.println("-----Game Server-----");
        playerNum = 0;
    }

    private void createSocket() {
        try {
            ss = new ServerSocket(port, 9, InetAddress.getByName(ip));
        } catch (IOException e) {
            System.out.println("IOException from Server");
        }
    }

    private void acceptConnection() {
        try {
            System.out.println("Waiting for Connection..");
            while (playerNum < 2) {
                if (ss != null) {
                    Socket s = ss.accept();
                    playerNum++;
                    System.out.println("Player #" + playerNum + " has online ");
                    ServerSide serverSide = new ServerSide(s, playerNum);
                    if (playerNum == 1) {
                        player1 = serverSide;
                    } else {
                        player2 = serverSide;
                    }
                    Thread t = new Thread(serverSide);
                    t.start();
                }
            }
            System.out.println("Connection for accepting players closed");
        } catch (IOException e) {
            System.out.println("IOException from acceptConnection()");
        }
    }

    private void sendDeclaration(int player) {
        if (player1.playerID == player) {
            try {
                player1.dos.writeUTF("You Won");
                player2.dos.writeUTF("You Lost");

            } catch (IOException e) {
                System.out.println("IOException from sendDeclaration");
            }

        } else {
            try {
                player2.dos.writeUTF("You Won");
                player1.dos.writeUTF("You Lost");

            } catch (IOException e) {
                System.out.println("IOException from sendDeclaration");
            }
        }
    }

    private class ServerSide implements Runnable {
        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dos;
        private int playerID;
        private int pid;
        private int finishTime;
        private String msg;


        public ServerSide(Socket s, int id) {
            socket = s;
            playerID = id;
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
                dos.writeInt(playerID);
                dos.flush();
                while (true) {
                    pid = din.readInt();
                    System.out.println("Player #" + pid);
                    msg = din.readUTF();
                    //System.out.println("" + msg);
                    finishTime = din.readInt();
                    System.out.println("Message" + finishTime);
                    sendDeclaration(pid);
                    player1.close();
                    player2.close();
                }
            } catch (IOException e) {
                System.out.println("IOException from run server side");
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

    public static void main(String[] args) {
        Server gs = new Server();
        gs.createSocket();
        gs.acceptConnection();
    }

}
