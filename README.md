# JungleSpeed
Jungle speed


1°/ Objectifs


Le but de ce projet est de mettre en place un jeu de type Jungle Speed en réseau. Ce type de jeu nécessite de mettre en place des solutions logicielles caractéristiques de la programmation client/serveur, notamment du fait que les joueurs vont devoir envoyer des ordres au serveur et recevoir l'état du jeu régulièrement. De plus, le jeu contient des alternances de phases où les joueurs jouent chacun à leur tour, ou tous ensembles au plus/moins rapide, nécessitant l'emploi de threads et de mécanismes d'attente d'événement. Enfin, le totem va être un objet partagé entre threads, ce qui implique d'utiliser des mutex pour y accéder. Toutes les problématiques classiques sont donc abordées.


2°/ Cahier des charges


2.1°/ Mise en place du jeu


Lorsqu'un client se connecte au serveur, il commence par envoyer un pseudo sous forme de chaîne de caractères et reçoit en retour un booléen. Si le booléen est vrai, le pseudo est accepté et le joueur peut continuer. Sinon le pseudo existe déjà et il doit renvoyer un pseudo, jusqu'à obtenir en retour vrai.
Immédiatement après cette phase d'enregistrement, le client peut envoyer 3 requêtes :


- lister les parties existantes non commencées,
- créer une partie à X (fourni par le client) joueurs,
- rejoindre une partie existante non commencée.


Pour la première, il reçoit une liste des parties, chaque ligne contenant entre autre le pseudo du créateur ainsi que le nombre de places disponibles. Cette liste est affichée sous la forme d'une énumération numérotée, avec des lignes du style :
1 – partie créée par Gonzo, reste 1 place disponible.
2 – partie créée par Toto, reste 2 places disponibles.
…
Pour la deuxième requête, le client envoie le nombre X de joueurs et reçoit en retour un booléen indiquant si la création a pu se faire ou non.


Pour la troisième, le client envoie le numéro de la partie qu'il veut rejoindre et reçoit en retour un booléen valant vrai s'il a pu la rejoindre et faux sinon.


Dès qu'un joueur a réussi à rejoindre une partie, il ne peut plus envoyer ces 3 types de requête et entre dans le boucle gérant la partie.


2.2°/ Une partie (avec X joueurs)


2.2.1°/ déroulement général


Chaque joueur d'une même partie est associé à un numéro de 1 à X, attribué selon l'ordre d'arrivée pour rejoindre la partie.
Une partie commence en distribuant un nombre égal de carte à chaque joueur. Dans le projet, les graphismes des cartes sont remplacées par des lettres qui se ressemblent : O, Q, B, P, E, F, I, J, C, G, plus le T et le H. Le paquet complet est constitué de 12*X cartes, chaque lettre étant présente en X exemplaires. Le serveur mélange le paquet et attribue 12 cartes à chaque joueur, qui sont considérées comme cachées.
Chacun son tour, chaque joueur va retourner la première carte de son tas et révéler une lettre. Il y a alors 3 cas possibles :
- si c'est une lettre T, tous les joueurs doivent s'efforcer de prendre le totem. Le premier à le faire met ses cartes découvertes (et pas celles encore cachées) sous le totem. Le jeu continue avec le joueur suivant.
- si c'est une lettre H, tous les joueurs doivent mettre leur main sur le totem. Le premier à le faire donne ses cartes découvertes au dernier, qui va prendre toutes ses cartes, les mélanger et commencer son tour.
- si c'est une autre lettre, il se peut qu'un autre joueur ait également cette lettre sur sa dernière carte découverte. Dans ce cas, les joueurs ont trois secondes pour attraper le totem. Deux cas se présentent.
        Un joueur a effectivement la même carte qu'un autre et prend le totem : il donne ses cartes découvertes à l'autre et celles qui sont éventuellement sous le totem. Le perdant prend toutes ses cartes, les mélange et commence son tour. 
        Un joueur (ou plusieurs) se trompe et prend le totem alors que sa carte n'est pas en double : tous les joueurs donnent leurs cartes découvertes et on crée un paquet avec, plus les cartes éventuellement sous le totem. Le fautif prend toutes les cartes de ce paquet, les mélange et commence son tour. S'il y a plusieurs fautifs, on répartit ce paquet équitablement et on tire entre tous celui qui recommence.
Si les joueurs laissent passer les 3 secondes, il est trop tard pour prendre le totem et c'est au joueur suivant (puisqu'il n'y a pas de perdant) de jouer.


On remarque donc que l'ordre de jeu n'est pas forcément 1, 2, ...X, 1, 2, …, X, … puisque lorsqu'un joueur se prend des cartes, le jeu recommence à partir de celui-ci.


La partie se termine lorsqu'un joueur a réussi à se débarrasser de toutes ses cartes, ou bien lorsqu'un joueur abandonne (= quitte la partie ou se déconnecte).


Cas particuliers : 
- lorsqu'un joueur retourne un T ou un H mais qu'il n'est pas assez rapide, alors cette carte reste sur son paquet de cartes découvertes. Cependant, elle est désormais considérée comme inactive et ne doit pas être prise en compte jusqu'à sa disparition.
- si une carte H est révélée mais que tous les joueurs ne mettent pas la main sur le totem, le gagnant réparti ses cartes découvertes entre les retardataires et on tire au sort celui qui recommence à jouer.
- si rien ne se passe alors qu'un double existe, il se peut que dans les tours suivants un triplé apparaisse. Dans ce cas, s'il y a un gagnant, il répartit ses cartes découvertes entre les perdants et on tire au sort celui qui recommence à jouer. De même avec un quadruplé, quintuplé, ...


2.2.2°/ Implémentation


L'organisation client/serveur sera du type « client idiot ». Ce dernier se contente de recevoir des ordres du serveur et du joueur et ne stocke rien concernant l'état du jeu : tout est centralisé au niveau du serveur. La seule exception sera pour le timeout de 3s qui sera géré au niveau client.
Comme tous les joueurs vont pouvoir jouer en même temps, il faut que le serveur soit multithreadé avec au minimum un thread par client. Ces threads vont donc se partager des objets mémoire, représentés par les classe Game, PartyServer. De plus, chaque thread est associé à un objet Player représentant le joueur, qui est créer lorsque le joueur se connecte au serveur et valide son pseudo.
Conformément aux principes de programmation multithreadée, ce sont les objets partagés qui contiennent toute la logique de l'application, les threads se contenant de demander des « services » aux objets.


A chaque tour de jeu, chaque thread serveur va suivre l'algorithme suivant :
1 - récupère (auprès de la partie) qui est le joueur courant
2 - si ce joueur est celui associé au thread, aller en 3 sinon en 4
3 – demande à la partie de retourner la carte du joueur i. ( NB : cette action va prendre un temps aléatoire entre 1 et 3 secondes )
4 – attendre le retournement de la carte (via sémaphore)
5 – envoi au client des cartes révélées sur la table
6 – attend un ordre du client. Cet ordre peut être de prendre le totem (à juste titre ou non), poser la main dessus, ou simplement de ne rien faire. A noter que ce dernier n'est pas explicitement donné par le joueur mais par le client lorsque le délai de 3s est écoulé.
6 – demande de prendre en compte cet ordre pour modifier l'état de la partie.
7 – attendre que tous les clients aient envoyé un ordre (via sémaphore)
8 – si le joueur courant est celui associé au thread, demande à la partie de calculer le résultat du tour
9 – attendre fin de tour (via sémaphore)
10 – envoi au client du résultat du tour et le nom du prochain joueur.


A niveau du client, l'algorithme est le suivant :
1 – attendre l'état de la partie et l'afficher (sauf cas particulier)
2 – saisie de l'ordre.
3 – au bout de 3s, envoyer l'ordre saisi ou  « ne rien faire »
4 – attendre le résultat du tour, par exemple sous la forme :
- rien ne se passe. C'est à toto de jouer
- toto s'est trompé et prend les cartes de tous les joueurs.
- toto a pris le totem.
…
6 – affiche qui va jouer ensuite.


Les ordres tapés au clavier par le joueur sont les suivants :
- TT : take totem. Cet ordre n'est valide que si une carte T vient d'être retournée ou bien si le joueur a une carte identique à celle d'un (ou plusieurs) autre joueur.
- HT : hands on totem. Cet ordre n'est valide que si une carte H vient d'être retournée.


Toute autre combinaison est considérée comme invalide et donc comme perdante. A noter qu'il peut y avoir plusieurs perdants au cours d'un même tour, par exemple si plusieurs se trompent. Dans ce cas, le vrai perdant est celui qui s'est trompé en premier.
De même, plusieurs joueurs peuvent envoyer à juste titre un ordre. Dans ce cas, seul le plus rapide est le vrai gagnant.
