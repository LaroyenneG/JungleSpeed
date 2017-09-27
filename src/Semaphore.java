/*
    Cette classe est complète et ne nécessite à priori aucun changement
 */
public class Semaphore {

    int nbTokens;

    public Semaphore(int nbTokens) {
        this.nbTokens = nbTokens;
    }

    public synchronized void put(int nb) {
        nbTokens +=  nb;
        notifyAll();
    }

    public synchronized void get(int nb) {
        while(nbTokens < nb) {
            try {
                wait();
            }
            catch(InterruptedException e) {}
        }
        nbTokens -= nb;
    }
}
