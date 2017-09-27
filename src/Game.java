import java.util.*;

/* IMPORTANT :
   Since the collections parties and players are shared by threads, their
   direct access must be forbidden and protected by a mutex.
 */
class Game {

    private List<PartyServer> parties; // all created parties
    private List<Player> players; // all players connected to the server

    public Game() {
        parties = new ArrayList<PartyServer>();
        players = new ArrayList<Player>();
    }

    /* A COMPLETER :
        Cette classe contient les méthodes nécessaires à la gestion des parties et des joueurs.
        Elle doit donc contenir (au minimum):
           - une méthode pour créer un joueur en fournissant son pseudo et l'ajouter à players
           - une méthode pour créer une partie en fournissant son créateur et le nombre de joueurs, et l'ajouter à parties
           - une méthode pour récupérer une partie à partir de son id dans parties
           - une méthode pour obtenir la liste des parties crées (par ex, sous la forme d'une List<String>)
           - une méthode pour supprimer un joueur
           - une méthode pour supprimer une partie
           - une méthode qui permet à un joueur existant de rejoindre une partie existante.
     */

    public boolean createPlayer(String name) {

        for(Player player : players) {
            if(player.name.equals(name)) {
                return false;
            }
        }

        players.add(new Player(name));

        return true;
    }

}
    
