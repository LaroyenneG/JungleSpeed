/*
    Cette classe est complète et ne nécessite à priori aucun changement
 */

import java.io.*;

class ClientJungle {

    public static void usage() {
        System.err.println("usage : java "+ClientJungle.class.getSimpleName()+" server_ip port");
        System.exit(1);
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            usage();
        }
        int port = Integer.parseInt(args[1]);
        ClientTCPJungle client = null;
        try {
            client = new ClientTCPJungle(args[0], port);
            client.initLoop(); // loop to send a pseudo to server
            client.requestLoop(); // loop to send request (list parties, create party, join party, ...)
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("cannot communicate with server");
            System.exit(1);
        }
    }
}
