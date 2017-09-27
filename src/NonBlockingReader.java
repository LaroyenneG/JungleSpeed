/*
    Cette classe est complète et ne nécessite aucun changement

    IMPORTANT: elle ne fonctionne que sous un système UNIX.
    Pour windows, solution NON TESTEE :
       remplacer "/dev/tty" par "CON" (ou "CON:")

    NOTE:
    Le principe de cette classe est de pouvoir faire une saisie au clavier qui soit interruptible.
    Pour cela, cette classe hérite de thread et ouvre en lecture le fichier représentant la terminal
    (ou la console). Ce thread peut alors être intterompu par un autre thread.
    Si une saisie est effectivement faite avant l'interruption, le résultat de la saisie est mis
    dans un objet partagé entre ce thread et celui qui interrompt, à savoir un objet Choice.
    Le thread interrompant peut donc tester l'état de cet objet pour savoir si une saisie a été faite ou non,
    et si oui, la récupérer.
 */

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

class NonBlockingReader extends Thread {

    BufferedReader consoleIn;
    Choice box;

    public NonBlockingReader(Choice box) {
        try {
            consoleIn = Files.newBufferedReader(Paths.get("/dev/tty"), Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("failed to open /dev/tty");
        }
        this.box = box;
    }

    public void run() {

        String outString = "";
        boolean error = false;
        while ((!error) && (!isInterrupted())) {
            try {
                outString = consoleIn.readLine();
                box.setValue(outString);
                return;
            } catch (IOException e) {
                error = true;
            }
        }
    }
}
