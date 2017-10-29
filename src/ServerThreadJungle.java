import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

class ServerThreadJungle extends Thread {

    private static Random loto = new Random(Calendar.getInstance().getTimeInMillis());

    Socket comm;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    Game game;
    Player player; // the player associated to this thread
    PartyServer currentParty; // the party associated to this thread

    int idThread; // the unique identifier of the client and thus the thread.

    public ServerThreadJungle(Socket comm, Game game, int idThread) {

        this.game = game;
        this.comm = comm;
        currentParty = null;
        this.idThread = idThread;
        player = null;
    }

    public void run() {

        try {
            oos = new ObjectOutputStream(comm.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(comm.getInputStream());
            initLoop();
        } catch (IOException | ClassNotFoundException e) {
            errorReport("cannot create streams or player");
            return;
        }
        try {
            requestLoop();
        } catch (IOException e) {
            /* A COMPLETER :
                - supprimer le joueur de game
             */
            game.deletePlayer(player);
            debugReport("client disconnected while in request loop.");
        }
    }

    public void initLoop() throws IOException, ClassNotFoundException {
        debugReport("entering initLoop().");
        /* A COMPLETER :
            Cette méthode permet de boucler tant que le client n'envoie pas un pseudo valide (= pas pris par un autre joueur)
            S'il est invalide, on renvoie au client un booléen valant false. Si il est valide, on crée un joueur et on renvoie
            au client true.
         */

        boolean success;
        do {
            Object object = ois.readObject();
            if (object instanceof String) {
                String name = (String) object;
                player = game.createPlayer(name);
                success = (player != null);
            } else {
                success = false;
            }

            oos.writeBoolean(success);
            oos.flush();
        } while (!success);
    }

    public void requestLoop() throws IOException {
        debugReport("entering requestLoop().");

        /* A COMPLETER :
            Cette methode attend les 3 requetes possibles et appelle les méthodes associées:
                - 1 = listes des parties
                - 2 = créer une partie
                - 3 = rejoindre une partie

        IMPORTANT : quand on retourne de "créer une partie" ou "rejoindre une partie", il existe
         deux solutions selon que l'on veut implémenter une version basique de la fin de partie, ou bien
         une version améliorée en changeant le protocole (cf. reamrque finale de partyLoop() dans le client)
         Pour la version basique, cela consiste simplement à quitter requestLoop() avec un return, ce qui va provoquer l'arrêt
         du thread et donc la rupture de la connexion avec le client.
         Pour l'autre solution, si elle est mise correctement en place, il ne faut surtout pas sortir de requestLoop().

         */

        int idRequest = ois.readInt();

        switch (idRequest) {
            case 1:
                requestListParties();
                break;

            case 2:
                requestCreateParty();
                break;

            case 3:
                requestJoinParty();
                break;

            default:
                debugReport("Bad request");
                break;
        }

    }

    public void requestListParties() throws IOException {
        debugReport("processing list parties request.");

        List<String> partyList = game.getAllParty();
        oos.writeObject(partyList);
        oos.flush();
    }

    public void requestCreateParty() throws IOException {
        debugReport("processing create party request.");

        /* A COMPLETER :
            - lire le nombre de joueur,
            - créer la partie
            - si impossible de la créer renvoyer au client false,
            - sinon
                - renvoyer true
                - rentrer dans la boucle de partie
                - signaler que le joueur quitte la partie et si c'est le dernier, supprimer la partie de game.
         */

        int nbPlayer = ois.readInt();

        boolean status = game.createParty(player, nbPlayer);

        oos.writeBoolean(status);
        oos.flush();

        if (status) {
            partyLoop();
        }

    }

    public void requestJoinParty() throws IOException {
        debugReport("processing join party request.");

        /* A COMPLETER :
            - lire le n° de partie
            - récupérer cette partie
            - si elle n'existe pas renvoyer au client false,
            - sinon
                - renvoyer true
                - mettre à jour la partie en lui ajoutant le joueur
                - rentrer dans la boucle de partie
                - signaler que le joueur quitte la partie et si c'est le dernier, supprimer la partie de game.
         */

        int idParty = ois.readInt();

        PartyServer partyServer = game.getParty(idParty);

        boolean status = false;

        if (partyServer != null) {
            status = partyServer.addPlayer(player);
        }

        oos.writeBoolean(status);
        oos.flush();

        if (status) {
            partyLoop();
        }
    }

    public boolean isPartyOver() {
        int state = currentParty.getState();
        if ((state == PartyServer.STATE_ENDWIN) || (state == PartyServer.STATE_ENDBROKEN)) return true;
        return false;
    }

    public void partyLoop() throws IOException {

        debugReport("entering partytLoop().");

        /* A COMPLETER :
            - envoyer l'id du joueur au client
            - attendre que le nombre requis de joueurs soit atteint
            - tant que !stop :
                - si partie finie ; return
                - récupérer le joueur courant

                - si joueur courant = player :
                    - faire un petit dodo (par ex, de 1 à 2 secondes)
                    - révéler sa carte

                - attendre la révélation

                - envoyer au client les cartes visibles (cf. allRevealedCards)
                - si partie finie : return
                - lire l'entier représentant l'ordre de jeu du client
                - si ordre de jeu = quitter ; changer l'état de la partie à ENDBROKEN
                - sinon, intégrer l'ordre dans la partie
                - si partie finie : return

                - attendre que tous les threads aient reçu et intégrer l'ordre de leur client à la partie.

                 - si partie finie : return
                 - si joueur courant = player :
                    - analyser le résultat

                - attendre find de tour
                - envoyer le résultat au client (cf. resultMsg)
                - si résultat signale partie finie, mettre stop à true

            Remarques :
                - le tant que doit être pris dans un try/catch sachant que si on tombe dans le catch,
                c'est que le client s'est déconnecté et qu'il faut changer l'état de la partie à ENDBROKEN

                - le fait de tester régulièrement si la partie est finie permet d'éviter de faire des
                traitement inutiles pour les threads qui sont encore connectés à leur client.
         */

        oos.writeInt(player.id);
        oos.flush();
    }

    private void debugReport(String msg) {
        System.err.println("Thread [" + idThread + "] - " + msg);
    }

    private void errorReport(String msg) {
        System.err.println("Thread [" + idThread + "] - " + msg);
    }
}
