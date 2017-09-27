/*
    Cette classe est complète et ne nécessite à priori aucun changement
 */

import java.util.*;

class Player {

    String name;
    CardPacket hiddenCards;
    CardPacket revealedCards;
    PartyServer currentParty; // the party that player is in, null if not playing a party
    int id; // player id in the current PartyServer, -1 if he didn't join a party.

    public Player(String name) {
        this.name = name;
        currentParty = null;
        id = -1;
        hiddenCards = null; //will be set when joining party
        revealedCards = null; //will be set when joining party
    }

    public void initializeParty(PartyServer party, int id, List<Card> heap) {
        currentParty = party;
        this.id = id;
        hiddenCards = new CardPacket(heap);
        revealedCards = new CardPacket();
    }

    public Card revealCard() {
        Card c = hiddenCards.removeFirst();
        revealedCards.addFirst(c);
        return c;
    }

    public Card currentCard() {
        if (revealedCards.isEmpty()) {
            return null;
        }
        return revealedCards.get(0);
    }

    public void takeCards(List<Card> heap) {
        hiddenCards.addCards(revealedCards);
        hiddenCards.addCards(heap);
        revealedCards.clear();
        hiddenCards.shuffle();
    }

    public List<Card> giveRevealedCards() {
        List<Card> cards = new ArrayList<Card>();
        cards.addAll(revealedCards.getAll());
        revealedCards.clear();
        return cards;
    }

    public boolean hasWon() {
        if ((hiddenCards.size() == 0) && (revealedCards.size() == 0)) return true;
        return false;
    }

}
