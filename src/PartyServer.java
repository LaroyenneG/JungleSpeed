import java.util.*;
import java.io.*;

class PartyServer {

    private static Random loto = new Random(Calendar.getInstance().getTimeInMillis());

    // the different states of the party
    final static int STATE_BEFORESTART = 0;
    final static int STATE_PLAYING = 1;
    final static int STATE_ENDWIN = 2;
    final static int STATE_ENDBROKEN = 3; // means a client has disconnected
    int state; // see finals above

    // general attributes for the party
    Player creator; // the creator of the party (needed e.g. when displaying the list of parties in a client)
    int nbPlayers; // the needed number of players for this party
    List<Player> players; // list of players, in their order of arrival in the party -> creator is at id=0
    private CardPacket allCards; // the card packet that contains cards for all players (ie. 12*nbPlayers cards)
    private List<Card> underTotem; // the card that are under the totem.

    // semaphore to synchronize threads
    /* A COMPLETER :
        Pour gérer facilement le jeu cette classe doit utiliser un sémaphore pour synchroniser les
        threads à des points critiques. Il en faut au moins quatre :
            - pour attendre le nombre requis de joueurs (i.e. nbPlayers) et pouvoir commencer la partie
            - pour attendre que le joueur courant ait retourner sa carte (cf. revealCard() )
            - pour attendre que tous les clients aient renvoyé leur ordre de jeu et qu'il ait été intégré au tour courant.
            - pour attendre la fin du tour pour pouvoir envoyer le résultat du tour

         A vous de déclarer/définir ces sémaphores ainsi que les compteurs associés
     */

    /* attributes to manage the current turn */

    /* NOTE: parmis ces attributs seuls quelques uns doivent être accessibles
       par les threads, notamment currentPlayer, AllRevealedCards, resultMsg, partywinner
       vous n'avez donc pas besoin de faire des getters pour tous les attributs.
     */

    // attributes for cards
    private Player currentPlayer; // the player that must reveal a card during the current turn
    private Card lastRevealedCard; // the card revealed by the current player
    private String allRevealedCards; // all the cards that are revealed : sent to all clients before they play

    // attributes to determine the turn result
    private List<Player> played; // the list of players that played during the current turn
    private List<Integer> result; // the result of the order of players.
    private boolean totemTaken; // if the totem has been taken during the current turn
    private boolean totemHand; // if a player has put his hand on the totem during the current turn
    private String resultMsg; // the message containing the reuslt of the current turn that threads send to their client.
    private Player partyWinner; // winner of the party (must stay at null until there is effectively a winner)
    private Player turnWinner; // winner of the current turn

    // attribute to manage the end of the party
    private int nbLeaveParty; // the number of threads that have currently leaved the party

    public PartyServer(Player creator, int nbPlayers) {
        this.creator = creator;
        this.nbPlayers = nbPlayers;
        state = STATE_BEFORESTART;
        partyWinner = null;
        nbLeaveParty = 0;

        /* A COMPLETER :
            - création des différentes collections
            - création du paquet complet allCards
            - ajouter le créateur à la partie (cf. addPlayer() )
            - creation des sémaphore et initialisation des compteurs.
         */
    }

    /* A COMPLETER :
        definition des getters pour les attributs qui doivent être visibles à l'extérieur de la classe.
     */
    public int getRemainingPlaces() {
        return nbPlayers-players.size();
    }

    /* A COMPLETER :
        definition des méthodes manipulant les sémaphores
     */

    /* addPlayer(): adds  player to the party */
    public synchronized boolean addPlayer(Player p) {

        /* A COMPLETER :
            Cette méthode permet d'ajouter un joueur à la partie. Pour cela, il faut d'abord vérifier
            si le nombre de joueurs requis n'est pas déjà atteint. Si c'est le cas, la méthode renvoie false.
            Sinon, elle ajoute p à la liste des joueurs, prends les 12 premières cartes de allCards et les donne
            au joueur (cf. initializeParty() dans Player). L'id de p est simplement le nombre de joueurs actuellement
            associés à la partie. Par exemple, lorsque l'on ajoute le créateur à la liste des joueurs, cette liste fait
            une taille de 1 et le créateur reçoit l'id 1.

         */
        return true;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void setState(int newState) {
        boolean ok = false;
        if (newState == STATE_ENDBROKEN) {
            ok = true;
        } else {
            if ((state == STATE_BEFORESTART) && (newState == STATE_PLAYING)) {
                ok = true;
            } else if ((state == STATE_PLAYING) && (newState == STATE_ENDWIN)) {
                ok = true;
            }
        }
        if (ok) state = newState;
    }


    /* playerLeaveParty():

      NOTE: this method is called when a thread has exited from partyLoop()

    */
    public synchronized boolean playerLeaveParty(Player p) {

        /* A COMPLETER :
            Quand un joueur quitte une partie, c'est que celle-ci
            s'est terminé de deux façons : normale, ou bien par deconnexion
            d'un client. Dans ce dernier cas, c'est le thread associé à ce client
            qui appelle en premier cette méthode, sachant que les autres sont à priori
            bloqués soit en attente de réception de l'ordre de jeu du client, soit dans un sémaphore.
            Dans le premier cas, il suffit de vérifier après la réception que la partie n'est pas
            terminée (cf. isPartyOver() dans le thread). Dans le deuxième cas, il faut un moyen de
            débloquer les threads en attente. Pour cela, il suffit que ceux qui quittent la partie
            mettent des jetons dans les sémaphores.

            Cette méthode s'écrit donc :

             - si état = fin sur deconnexion : mettre nbPlayers jetons (pour être sur) dans les sémaphores
             - retirer p de players
             - incrémenter le nb de joueur ayant quitté la partie
             - si ce nb = nbPLayers retourner true sinon false.

         */
        return false;
    }

    public synchronized void revealCard() {

        lastRevealedCard = currentPlayer.revealCard();
        allRevealedCards = "";
        for (Player p : players) {
            if (p.revealedCards.size() > 0) {
                allRevealedCards += p.revealedCards.cards.get(0).card + " ";
            } else {
                allRevealedCards += "_ ";
            }
        }
    }


    public boolean checkSameCards(Player player) {

        /* A COMPLETER :
            Cette méthode vérifie si la carte retournée devant player (si elle existe)
            est la même que celle d'un autre joueur, auquel cas, elle renvoie true
         */

        return false;
    }

    /* NB : result values are :
       -2: a player made an error (see below)
       -1: a player lost because he didn't take the totem while he should
       0: a player took the good decision but is not the faster
       1: a player has win the turn

       error cases are the following :
       - a player does nothing while the last revealed card is 'hand on totem'
       - a player takes the totem while the last revealed card is 'hand on totem'
       - a player puts his hand on the totem while the last revealed card is 'take totem'
       - a player takes the totem while he hasn't the same card than another player and the last revealed is != 'H' or 'T'

       NB: order values are :
       10: do nothing
       11: take totem
       12: hand on totem
     */
    public synchronized void integratePlayerOrder(Player player, int order) {
        played.add(player);

        if (lastRevealedCard.card == 'H') {

            if (order == 10) {
                result.add(-2); // player made an error since he must put his hand on totem
            } else if (order == 11) {
                result.add(-2); // player made an error since he must put his hand on totem
            } else if (order == 12) {
                if (!totemHand) {
                    result.add(1); // winner
                    totemHand = true;
                } else {
                    if (played.size() == nbPlayers) {
                        result.add(-1); // looser : last player to put hand on the totem
                    } else {
                        result.add(0); // neither winner, neither looser
                    }
                }
            } else {
                result.add(-2); // player made an error by sending an invalid order
            }
        } else if (lastRevealedCard.card == 'T') {

            if (order == 10) {
                result.add(-1); // player lost but with no consequences
            } else if (order == 11) {
                if (!totemTaken) {
                    result.add(1); // first player to take totem -> winner
                    totemTaken = true;
                } else {
                    result.add(0); // neither winner, neither looser
                }
            } else if (order == 12) {
                result.add(-2); // player made an error: should take the totem
            } else {
                result.add(-2); // player made an error by sending an invalid order
            }
        } else {

            if (order == 10) {

                if (checkSameCards(player)) {
                    result.add(-1); // looser since he hasn't taken first the totem
                } else {
                    result.add(0); // neither winner, neither looser since nothing to do
                }
            } else if (order == 11) {

                if (checkSameCards(player)) {
                    if (!totemTaken) {
                        result.add(1); // first player to take totem -> winner
                        totemTaken = true;
                    } else {
                        result.add(-1); // looser since he hasn't taken first the totem
                    }
                } else {
                    result.add(-2); // should not take the totem -> error -> looser
                }
            } else if (order == 12) {
                result.add(-2); // player made an error: should take the totem
            } else {
                result.add(-2); // player made an error by sending an invalid order
            }
        }
    }


    public synchronized void analyseResults() {

        resultMsg = "";
        List<Player> lstErrors = new ArrayList<Player>(); // list of players that made an error this turn
        List<Player> lstLoosers = new ArrayList<Player>(); // list of players that lost (not an error) this turn

        for (int i = 0; i < nbPlayers; i++) {
            if (result.get(i) == -2) {
                lstErrors.add(played.get(i));
            }
            if (result.get(i) == -1) {
                lstLoosers.add(played.get(i));
            }
        }

        // if some players made an error
        if (!lstErrors.isEmpty()) {
            /* whatever the case, players that made an error are the ultimate loosers:
               - collect all revealed cards
	           - add those under the totem
	           - distribute them among loosers
            */
            CardPacket errorPack = getAllRevealedCards();

            int nb = (errorPack.size() + 1) / lstErrors.size();
            for (int i = 0; i < lstErrors.size(); i++) {
                Player p = lstErrors.get(i);
                if (i < lstErrors.size() - 1) {
                    p.takeCards(errorPack.takeXFirst(nb));
                    resultMsg += p.name + " made an error: he takes " + nb + "cards from all players.\n";
                } else {
                    resultMsg += p.name + " made an error: he takes " + errorPack.size() + "cards from all players.\n";
                    p.takeCards(errorPack.getAll());
                }
            }
            // now check if someone has won
            for (Player p : players) {
                if (p.hasWon()) {
                    resultMsg += p.name + " wins the party";
                    setState(STATE_ENDWIN);
                    return;
                }
            }
            currentPlayer = lstErrors.get(loto.nextInt(lstErrors.size()));
            resultMsg += "\n Next player: " + currentPlayer.name;
        }
        // else if no player made an error
        else {

            int indexWinner = -1;
            for (Integer r : result) {
                if (r == 1) {
                    indexWinner = r;
                    break;
                }
            }
            // if nobody wins this turn
            if (indexWinner == -1) {
                resultMsg += "Nobody won this turn\n";
                currentPlayer = players.get(currentPlayer.id % nbPlayers);
                resultMsg += "\n Next player: " + currentPlayer.name;
            }
            // else if a player is the winner : result depends on the last revealed cards
            else {
                turnWinner = players.get(indexWinner);
                resultMsg += turnWinner.name + " won the turn.\n";

                // if winner wins on a take totem
                if (lastRevealedCard.card == 'T') {
                    resultMsg += "He puts his cards under the totem.\n";
                    underTotem.addAll(turnWinner.revealedCards.getAll());
                    turnWinner.revealedCards.clear();
                }
                // else if winner wins on a hand on totem
                else if (lastRevealedCard.card == 'H') {
                    Player looser = lstLoosers.get(0); // normally there should be a single player in lstLoosers list
                    resultMsg += "He gives his cards and those under totem to " + looser.name + ".\n";
                    CardPacket winnerPack = getWinnerRevealedCards();
                    looser.takeCards(winnerPack.getAll());
                }
                // if winner wins because he has the same card than some other players
                else {
                    // distribute winner's revealed card to loosers
                    CardPacket winnerPack = getWinnerRevealedCards();
                    int nb = (winnerPack.size() + 1) / lstLoosers.size();
                    for (int i = 0; i < lstLoosers.size(); i++) {
                        Player p = players.get(i);
                        if (i < lstLoosers.size() - 1) {
                            p.takeCards(winnerPack.takeXFirst(nb));
                            resultMsg += p.name + " lost his a duel with " + turnWinner.name + ". He takes " + nb + "cards.\n";
                        } else {
                            p.takeCards(winnerPack.getAll());
                            resultMsg += p.name + " lost his a duel with " + turnWinner.name + ". He takes " + winnerPack.size() + "cards.\n";
                        }
                    }
                }
                // now check if someone has won
                for (Player p : players) {
                    if (p.hasWon()) {
                        resultMsg += p.name + " wins the party";
                        setState(STATE_ENDWIN);
                        return;
                    }
                }
                currentPlayer = lstLoosers.get(loto.nextInt(lstLoosers.size()));
                resultMsg += "\n Next player: " + currentPlayer.name;
            }
        }

        initNewTurn();
    }


    private void initNewTurn() {
        played.clear();
        result.clear();
        totemTaken = false;
        totemHand = false;
    }

    /* getAllRevealedCards() : constitutes a packet
        with the revealed cards of all players and those
        that are under the totem.
        This method is used when a player has made an error.
     */
    private CardPacket getAllRevealedCards() {

        CardPacket packet = new CardPacket();
        for (Player p : players) {
            packet.addCards(p.giveRevealedCards());
        }
        packet.addCards(underTotem);
        underTotem.clear();

        return packet;
    }

    /* getwinnerRevealedCards() : constitutes a packet
        with the revealed cards of the turn winner and those that
        are under the totem.
        This method is used when there is a winner for the current turn.

     */
    private CardPacket getWinnerRevealedCards() {

        CardPacket packet = new CardPacket();
        packet.addCards(turnWinner.giveRevealedCards());
        packet.addCards(underTotem);
        underTotem.clear();

        return packet;
    }

    @Override
    public String toString() {
        return creator.name+":"+getRemainingPlaces();
    }
}
