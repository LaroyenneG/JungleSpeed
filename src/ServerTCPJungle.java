/*
    Cette classe est complète et ne nécessite à priori aucun changement
 */

import java.io.*;
import java.net.*;

class ServerTCPJungle {

    ServerSocket conn;
    Socket comm;
    Game game;
    static int idThread;

    public ServerTCPJungle(int serverPort) throws IOException {

        conn = new ServerSocket(serverPort);
        game = new Game();
        idThread = 1;
    }

    public void mainLoop() throws IOException {

        while (true) {

            comm = conn.accept();
            System.out.println("connection with "+comm.getRemoteSocketAddress());
            ServerThreadJungle t = new ServerThreadJungle(comm, game, idThread++);
            t.start();
        }
    }
}
