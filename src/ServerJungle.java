/*
    Cette classe est complète et ne nécessite à priori aucun changement
 */

import java.io.*;

class ServerJungle {

    public static void usage() {
        System.exit(1);
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            usage();
        }
        int port = Integer.parseInt(args[0]);
        ServerTCPJungle server = null;
        try {
            server = new ServerTCPJungle(port);
            server.mainLoop();
        } catch (IOException e) {
            System.err.println("cannot communicate with client");
            System.exit(1);
        }
    }
}
