import java.io.*;
import java.net.*;
import java.util.*;

class ClientTCPJungle {

    Socket comm;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    BufferedReader consoleIn; // flux de lecture lignes depuis clavier

    int idPlayer; // the player's id in the party

    public ClientTCPJungle(String serverIp, int serverPort) throws IOException {

        comm = new Socket(serverIp, serverPort);
        ois = new ObjectInputStream(comm.getInputStream());
        oos = new ObjectOutputStream(comm.getOutputStream());
        oos.flush();
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
        idPlayer = -1;

    }

    public void initLoop() throws IOException, ClassNotFoundException {

		/* A COMPLETER :
            cette méthode doit demander au joueur de taper un pseudo au clavier
			puis l'envoyer au serveur (donc au thread associé à ce client).
			Ce dernier répond par un booléen indiquant si le pseudo est valide ou non (= déjà pris)
			Si c'est non, on repète depuis le début. Si oui, on sort de la méthode.
		 */

        System.out.print("Choice a pseudo :");
        String pseudo;
        do {
            pseudo = consoleIn.readLine();
            oos.writeObject(pseudo);
        } while (!ois.readBoolean());

    }

    public void requestLoop() throws IOException, ClassNotFoundException {

        String reqLine = null;
        String[] reqParts = null;
        boolean stop = false;

        while (!stop) {

            System.out.print("ClientJungle> ");
            reqLine = consoleIn.readLine();
            reqParts = reqLine.split(" ");

            // request "list" : list existing parties
            if (reqParts[0].equals("list")) {

				/* A COMPLETER :
                   - envoi de la requête,
				   - réception du résultat (sous forme de List<String>) et affichage
				 */
            }
            // request "create" : create a new party and wait that it starts
            else if (reqParts[0].equals("create")) {

				/* A COMPLETER :
                   - envoi de la requête, le nombre de joueurs se trouvant dans reqParts[1]
				   - réception d'un booléen indiquant si c'est ok ou non
				   - si ok, on appelle la méthode initiant une partie
				 */
            } else if (reqParts[0].equals("vs")) {
                /* A COMPLETER :
				   - envoi de la requête, le n° de partie se trouvant dans reqParts[1]
				   - réception d'un booléen indiquant si c'est ok ou non
				   - si ok, on appelle la méthode initiant une partie
				 */
            } else if (reqParts[0].equals("quit")) {
                stop = true;
            }
        }
    }


    /*
     * partyLoop(); initiate a new party
     */
    private void partyLoop() {

        Choice choice = new Choice(); // l'objet partagé avec le thread de saisie

		/* A COMPLETER :
			Cette méthode commence par recevoir le n° du joueur (1, 2, ...) au sein de la partie.
			Ensuite, tant que la partie n'est pas finie (que ce soit une fin normale, ou bien une
			fin provoquée par une déconnexion), on répète le même code pour chaque tour de jeu :
				- réception d'un String représentant les cartes visibles
				- demande au joueur de saisir un ordre de jeu (T: prendre totem, H:poser main sur totem)
				- envoi au serveur de l'ordre donné
				- réception d'un String donnant le résultat du tour.

			Quelques précisions :
				- pour faire la saisie en temps limité (de 3s par ex.), il faut utiliser une classe spéciale
				NonBlockingReader, en passant au constructeur une instance de Choice qui lui permet de stocker
				l'éventuelle saisie. Comme tout thread, il suffit d'instancier cette classe et d'appeler la méthode
				start() pour lancer son exécution. Pour l'interrompre, il suffit d'appeler la méthode interrupt().
				Exemple :
				NonBlockingReader t = new NonBlockingReader(choice);
				t.start();
				try {
					Thread.sleep(3000);
					t.interrupt();
					t.join();
				} catch (InterruptedException e) {
				}

				- le client ne doit pas envoyer directement ce qui a été éventuellement saisi. Le protocole
				défini les valeurs entières suivantes :
				   11 = saisie de TT (= prendre totem)
				   12 = saisie de HT (= poser main sur totem)
				   10 = aucune saisie ou bien saisie autre que TT ou HT (= erreur de saisie)

				- le String contenant le résultat du tour (cf. analyzeResult() ) permet de savoir si la partie est terminée en
				 vérifiant la présence du mot "wins" (ou "victoire" si vous traduisez les messages en français)


			IMPORTANT : la prise en compte de toutes les situations d'erreur est quelque chose de très complexe
			dans une appli réseau comme celle-ci. En effet, si l'un des joueurs se déconnecte alors il faut que la partie s'arrête.
			Cela revient à dire que les autres client doivent sortir de la méthode partyLoop(). Or, ils sont peut être bloqués
			en attendant une information de la part de leur thread serveur associé.
			La solution la plus basique dans ce cas consiste à rompre la connexion du côté du thread en terminant ce dernier,
			provoquant ainsi une IOException côté client, ce qui permet de sortir de partyLoop().
			Ce n'est pas très satisfaisant puisque le client doit ensuite se reconnecter au serveur pour avoir de nouveau accès au serveur.
			L'autre solution consiste à modifier le protocole pour que chaque fois que le client doit recevoir des infos de son thread
			ce dernier le prévient d'abord (par exemple avec un booléen) de si la partie doit continuer ou non. Si elle continue, le thread envoie
			l'info. Sinon, il n'envoie rien et sort de sa propre méthode partyLoop(), ce qui fait qu'il retourne dans requestLoop() sans se terminer.

			 Pour ce projet, il vous est demandé d'implémenter au minimum la solution basique qui termine abruptement le thread si ce
			 dernier constate que la partie s'est terminée à cause de la déconnexion d'un autre client.

			 Cependant, vous pouvez implémenter la version améliorée en modifiant le protocole à votre guise.
		 */

    }
}
