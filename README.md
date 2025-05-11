# Frogger
Rapport Frogger
Aaron Damy et Alex Mantoux
Sommaire :
1. Analyse fonctionnelle
a. Objectif général
b. Fonctionnalités à implémenter
c. Découpage en sous-problèmes
d. Architecture logicielle
e. Liens entre classes
2. Description des structures de données
3. Spécification précise des classes principales
a. Classe Client
b. Classe Serveur
c. Classe Jeu
d. Classe GameLogic
4. Architecture logicielle détaillée
5. Jeu de test
6. Conclusion
Rapport Frogger 1
1. Analyse fonctionnelle
Objectif général :
Recréer le jeu Frogger en Java en y ajoutant des fonctionnalités avancées, en
particulier des modes multijoueurs en réseau, des obstacles dynamiques, un
classement des joueurs en fonction de leur score et un serveur multi-clients
adaptatif.
Fonctionnalités à implémenter
1. Mode Solo
Le joueur dirige une grenouille qui doit traverser une route puis une rivière
sans se faire écraser ou tomber dans l'eau.
Obstacles classiques : voitures (route), rondins (rivière), crocodiles (rivière),
tondeuses/serpents (terre-plein central).
Objectif : atteindre un nombre de points d’arrivée (accueil des grenouilles).
2. Modes Réseau
A. Mode Collaboratif
Variante 1 : Objectif d’équipe — sauver un maximum de grenouilles en
coopération (nombre total).
Variante 2 : Un ou plusieurs joueurs incarnent une grenouille carnivore et
doivent manger les autres joueurs.
B. Mode Compétition
Variante 1 : Le premier joueur à sauver n grenouilles gagne.
Variante 2 : Le joueur ayant sauvé le plus de grenouilles au bout de m minutes
gagne.
Rapport Frogger 2
3. Obstacles dynamiques
Présence de crocodiles dans la rivière (qui apparaissent et disparaissent sur
un intervalle de temps donné).
4. Classement utilisateur
Score du joueur (Si son score est dans le classement des 10 meilleurs scores
réalisés par les joueurs alors le tableau des scores est modifié et le nouveau
record est inscrit)
Stockage dans un fichier .txt associé à une classe Java pour l’écriture
5. Serveur Multi-clients et adaptatif
Plusieurs clients peuvent se connecter.
Le serveur gère plusieurs types de parties.
Si un client lance une partie réseau et ne trouve pas d’autres clients (minimum
1) en 60 secondes, alors elle bascule automatiquement en mode solo.
Le serveur adapte la difficulté du jeu (vitesse des obstacles) en fonction du
niveau du joueur.
Découpage en sous-problèmes
1. Communication Réseau
Sockets, threads pour échanges entre clients et serveur.
Gestion de plusieurs clients (thread par client ?).
Gestion de l’état de chaque partie côté serveur.
2. Écran d’accueil / navigation
Sélection du pseudo.
Choix du mode (solo, collaboratif, compétitif).
Connexion au serveur ou démarrage d'une partie locale.
3. Moteur de jeu
Boucle de jeu.
Rapport Frogger 3
Détection de collisions.
Déplacement de la grenouille et des obstacles.
Mécanique de victoire/défaite.
4. Entités du jeu
Joueur (position, état).
Obstacles : Voitures, Rondins, Crocodiles.
Partie (mode, chrono, score, vie).
5. Gestion multijoueur
Synchronisation des états.
Transmission des mouvements.
Logique selon le mode (coopératif, compétitif, carnivore).
6. Classement et persistance
Enregistrement et chargement des scores.
Mise à jour en fin de partie.
Adaptation du niveau selon les performances passées.
Architecture logicielle
1. Serveur
Écoute les connexions clients.
Crée et gère les parties.
Peut modifier les paramètres de difficulté selon le classement.
Fait office d’arbitre dans les modes multi.
2. Client
Envoie les inputs du joueur.
Rapport Frogger 4
Reçoit les mises à jour du jeu.
Interagit avec le serveur (mouvements, score, fin de partie).
3. AccueilScreen
Interface de lancement.
Saisir le pseudo.
Choix du mode de jeu.
Se connecte au serveur ou lance FroggerGame.
4. FroggerGame
Lance une partie en mode solo.
Initialise Jeu , GameLogic , Joueur...
5. Jeu
Interface graphique (affichage dynamique du jeu).
Boucle de rendu.
Reçoit les inputs.
6. GameLogic
Gère les règles du jeu.
Détecte collisions, gestion du temps, score...
Gère la victoire, défaite, conditions spéciales.
Appelle les bons comportements selon le type de partie (compétition, coop,
carnivore...).
7 . Joueur
Coordonnées.
Statut : vivant/mort.
Score, nom d’utilisateur.
Rapport Frogger 5
8. Obstacle
Déplacement, interaction avec Joueur .
Voiture
Rondin
Crocodile
Tondeuse
Serpent
10. HighScores
Gère les stats de chaque joueur.
Peut être persisté dans un fichier JSON/CSV/BD.
Sert au serveur pour adapter la difficulté.
Liens entre les classes :
Lancement et Interface
AccueilScreen
↳ point d’entrée de l’application côté client
↳ permet à l’utilisateur de choisir le mode de jeu
↳ crée une instance de :
Client si mode réseau
FroggerGame si mode solo
Mode solo
FroggerGame
↳ initialise :
Jeu (l’interface du jeu)
GameLogic (la logique du jeu)
Rapport Frogger 6
Joueur (grenouille locale)
Les obstacles ( Voiture , Rondin , Crocodile , etc.)
GameLogic
↔ interagit avec :
Joueur
tous les obstacles (déplacements, collisions)
Jeu
↔ interagit avec :
GameLogic (rendu en fonction de l’état du jeu)
Joueur (affichage)
Obstacle (affichage et animation)
Mode réseau
Client
↔ se connecte au Serveur via socket
↔ envoie :
les inputs utilisateur (déplacements)
↔ reçoit :
les mises à jour de la partie (état des autres joueurs, obstacles, score, etc.)
Serveur
↳ gère :
plusieurs Client
une ou plusieurs Partie (en fonction des demandes clients)
le matchmaking, les délais de 60s
Entités du jeu
Joueur
↳ contient :
Rapport Frogger 7
position
pseudo
score
Voiture , Rondin , Crocodile , Tondeuse , Serpent
↳ chaque type a sa propre logique de déplacement et d’interaction avec
Joueur
2. Description des structures de données
3. Spécification précise des classes
principales
A. Classe Client (mode multijoueur du jeu Frogger)
Rapport Frogger 8
La classe Client représente l’interface et la logique côté joueur dans le mode
multijoueur de Frogger. Elle gère à la fois l’affichage graphique, l’interaction avec
le joueur, et la communication réseau avec le serveur.
Attributs principaux
Réseau :
PORT : Port utilisé selon le mode de jeu ( 8081 pour "TEAM", 8082 pour
"HUNTER").
pseudo , playerId , playerTeam , currentHunterId : Identifiants du joueur, son équipe,
etc.
out : Permet d’envoyer des commandes au serveur via un PrintWriter.
Éléments du jeu :
players , cars , rondins , trous , crocodiles : Maps contenant tous les objets à
afficher.
teamScores : Stocke les scores des différentes équipes.
gameLogic : Gère la logique locale du jeu (timer, score, etc.).
gameActive , hunterGagne , hunterPseudo : États du jeu en mode HUNTER.
spriteManager : Pour la gestion des sprites graphiques.
Graphique :
Hérite de JPanel et utilise paintComponent() pour dessiner les éléments.
restartButton , menuButton : Boutons de fin de partie.
image : Texture pour le fond (herbe).
Interaction joueur
Déplacement au clavier : Le joueur peut se déplacer avec les flèches. En
mode HUNTER, la touche espace sert à "attraper" un autre joueur proche (via
checkHunterCatch() ).
Boutons GUI : En fin de partie, les boutons Restart et Menu s’affichent pour
relancer une partie ou retourner à l’écran principal.
Rapport Frogger 9
Affichage ( paintComponent(Graphics g) )
Cette méthode dessine :
Le fond (eau, herbe, route).
Les obstacles (voitures, rondins, crocodiles, trous).
Les joueurs avec leur apparence respective.
Le score, les vies, le timer.
Les messages de victoire ou défaite, selon l’état du jeu.
Les scores d’équipe ou le rôle "Hunter" si activé.
Réception des messages du serveur
Dans la méthode main , le client établit une connexion au serveur, puis lit
continuellement les messages reçus :
ID;...
: Le serveur assigne un identifiant au joueur.
ELEMENTS;...
: Mise à jour des entités du jeu.
POS;...
: Mise à jour des positions des joueurs.
RESET_POSITION;... , LOSE_LIFE , GAME_OVER : Gestion des événements de jeu.
SCORE , TEAM_SCORES , TEAM_FINAL_SCORE : Gestion des scores.
HUNTER_UPDATE , HUNTER_CATCH_SUCCESS , HUNTER_CAUGHT : Mécaniques du mode
HUNTER.
SWITCH_TO_SINGLE : Retour au mode solo.
LE_Y;...
: Ajout de score lorsqu’une ligne est franchie.
Méthodes notables
checkHunterCatch() : Si le joueur est le Hunter, vérifie si un autre joueur est proche
pour l’attraper.
placementHunter() : Replace le Hunter au début (actuellement limité à un
changement de X).
restartGame() (non inclus ici, mais probablement appelé depuis le bouton restart).
Rapport Frogger 10
returnToMenu() : Bascule vers le menu d’accueil (implémenté via SWITCH_TO_SINGLE ).
Résumé
La classe Client :
Est responsable de l’expérience utilisateur côté client.
Combine interface graphique, réception réseau, et logique locale du joueur.
Est capable de gérer plusieurs modes de jeu (TEAM, HUNTER).
Est étroitement couplée à GameLogic et à la classe Serveur , qui lui envoie les
états de jeu en temps réel.
B. Classe Server
La classe Serveur représente la logique d'un serveur multi-joueurs pour un jeu
basé sur des éléments du type Frogger. Voici une description détaillée de ses
composants et de son fonctionnement :
Attributs principaux
PORT : Le port par défaut pour le serveur.
playerPositions : Une map qui associe un identifiant de joueur à sa position
(coordonnées X et Y).
clients : Une map qui associe un identifiant de joueur à son flux de sortie
(PrintWriter), permettant de communiquer avec ce joueur.
playerLives : Une map qui associe un identifiant de joueur à son nombre de vies
restantes.
playerTeams : Une map qui associe un identifiant de joueur à son équipe (A ou B,
dans le mode "TEAM").
teamScores : Une map qui associe une équipe (A ou B) à son score actuel.
nextPlayerId : Un compteur qui génère des identifiants uniques pour chaque
joueur.
Rapport Frogger 11
gameLogic : Un objet de type GameLogic qui gère la logique du jeu (obstacles,
mouvements, etc.).
voitures , rondins , crocodiles , trous : Des listes contenant des objets représentant
respectivement les voitures, rondins, crocodiles et trous, qui sont les
obstacles dans le jeu.
scheduler : Un planificateur pour exécuter des tâches à intervalles réguliers (par
exemple, la boucle du jeu).
gameStarted : Un booléen qui indique si le jeu a démarré.
gameMode : Le mode de jeu actuel ("HUNTER" ou "TEAM").
hunterId : L'identifiant du joueur actuel en mode "HUNTER" (s'il y en a un).
teamACount , teamBCount : Compteurs pour suivre le nombre de joueurs dans
chaque équipe, utile pour assigner les joueurs aux équipes.
Méthodes principales
initGameLogic(int modeParam) : Initialise la logique du jeu en fonction du mode
sélectionné (HUNTER ou TEAM), en récupérant les obstacles et en définissant
la logique du jeu.
main(String[] args) : La méthode principale qui lance le serveur. Elle choisit le
mode de jeu (HUNTER ou TEAM), crée des threads pour gérer les clients et
commence la boucle de jeu. Le serveur écoute ensuite sur un port (8080,
8081 ou 8082 en fonction du mode) pour accepter les connexions des clients.
broadcastGameElements() : Diffuse les éléments du jeu (voitures, rondins,
crocodiles, trous et positions des joueurs) à tous les clients connectés.
selectNewHunter() : Choisit un nouveau joueur pour être le "Hunter" dans le mode
"HUNTER", et envoie sa position au client correspondant.
broadcastHunterUpdate() : Envoie une mise à jour de l'état du Hunter à tous les
clients.
3. Gestion des clients
ClientHandler : Une classe interne qui gère la communication avec un client
individuel. Chaque joueur est traité dans un thread distinct. La classe gère la
Rapport Frogger 12
connexion, l'affectation à une équipe (si en mode "TEAM"), la réception de
messages, le déplacement des joueurs et la gestion des collisions.
run() : La méthode principale de chaque thread de client, qui lit les
messages envoyés par le client (comme les commandes de déplacement
ou les captures) et effectue les actions correspondantes (mise à jour des
positions, gestion des collisions, etc.).
startMultiplayerGame() et startSinglePlayerGame() : Démarre le jeu en mode multi-
joueurs ou en mode solo en fonction du nombre de joueurs connectés.
4. Gestion des événements du jeu
updatePlayerPosition(int playerId, int dx, int dy) : Met à jour la position d'un joueur en
fonction des déplacements reçus et envoie les informations mises à jour à
tous les clients.
checkCollisions() : Vérifie si un joueur entre en collision avec un obstacle (voiture,
rondin, crocodile, trou). Si une collision est détectée, le joueur perd une vie ou
meurt.
handleHunterCatch(int hunterId, int preyId) : Gère la situation où un Hunter attrape un
autre joueur (prédateur contre proie), affecte les vies du joueur attrapé et met
à jour les scores ou l'état du jeu.
5. Gestion des scores et équipes
isTeamDead(String team) : Vérifie si tous les joueurs d'une équipe sont morts (ont
perdu toutes leurs vies).
broadcastTeamScores() : Diffuse les scores actuels des équipes à tous les joueurs.
broadcastTeamFinalScore(String team) : Diffuse le score final d'une équipe une fois que
tous ses membres sont morts.
resetPlayerPosition(int playerId) : Remet la position d'un joueur à sa position de départ
en cas de collision ou de mort.
6. Envoi de messages aux clients
sendToClient(int playerId, String message) : Envoie un message spécifique à un client via
son flux de sortie ( PrintWriter ).
Rapport Frogger 13
Conclusion
La classe Server est une implémentation d'un serveur de jeu multi-joueurs avec
des modes compétitifs et collaboratifs. Elle gère les connexions des clients, la
logique de jeu, les déplacements, les collisions, les équipes, et les scores. Elle
utilise des threads pour chaque client et des mécanismes de synchronisation pour
garantir que le jeu fonctionne correctement en mode multi-joueurs
C. Classe Jeu
La classe Jeu est le cœur de l'interface graphique du jeu Frogger, héritant de
JPanel . Elle gère l’affichage du jeu, la boucle principale, les interactions clavier,
l’enregistrement des scores, et l’interface utilisateur avec des boutons pour
redémarrer ou revenir au menu.
Attributs principaux
fini : Indique si le jeu est terminé.
spriteManager : Gère le rendu des différents objets graphiques (voitures, rondins,
crocodiles...).
image : Représente le fond (herbe).
restartButton / menuButton : Boutons graphiques pour redémarrer la partie ou
retourner au menu.
gameLogic : Contient toute la logique du jeu (mouvements, collisions, score...).
pseudo : Nom du joueur saisi au lancement du jeu.
scoreEnregistre : Booléen pour éviter d’enregistrer plusieurs fois le score.
frame : Fenêtre contenant le panel du jeu.
Constructeur Jeu(int mode)
Demande le pseudo du joueur via une boîte de dialogue ( JOptionPane ).
Initialise la logique de jeu avec le mode choisi.
Rapport Frogger 14
Si le joueur fait partie du top 10, la vitesse des éléments est augmentée.
Crée la fenêtre de jeu, initialise les obstacles ( Voiture , Rondin , Crocodile ) dans le
spriteManager .
Installe un KeyListener pour réagir aux touches directionnelles du clavier.
Ajoute les boutons "Restart" et "Menu", masqués par défaut.
Lance la boucle de jeu dans un thread séparé.
Méthodes principales
1. paintComponent(Graphics g)
Redéfinit l’affichage graphique :
Dessine les routes, l’eau, l’herbe.
Affiche le score, le temps et les vies restantes.
Dessine les objets ( Trous , sprites, joueur).
Affiche un message de victoire ou de fin de partie avec les boutons
visibles.
Enregistre le score une seule fois lors du Game Over.
2. gameLoop()
Boucle principale du jeu, qui rafraîchit l'écran toutes les 10ms.
Appelle verifierJoueurSurObjet() et repaint() à chaque itération.
3. estDansTop10(String pseudo)
Lit le fichier highscores.txt.
Récupère les 10 meilleurs scores.
Vérifie si le pseudo du joueur est dans le top 10.
4. enregistrerScore()
Appelle la méthode statique saveScoreIfTop10 de HighScoreWindow pour enregistrer le
score.
Rapport Frogger 15
5. restartGame()
Relance une nouvelle partie en recréant une instance de Jeu .
6. returnToMenu()
Ferme la fenêtre de jeu actuelle.
Relance l’écran d’accueil ( AccueilScreen.main() ).
Interactions utilisateur
Clavier : flèches directionnelles pour déplacer le joueur.
Souris : clic sur les boutons "Restart" ou "Menu" après une défaite.
Résumé
La classe Jeu coordonne l’interface graphique, la logique de jeu et les
interactions utilisateur dans Frogger. Elle permet de jouer, suivre le score en
direct, détecter la fin du jeu, afficher les boutons de navigation, et gérer les
meilleurs scores de manière fluide et interactive.
D. Classe GameLogic
La classe GameLogic gère la logique centrale du jeu. Elle est responsable de
l'initialisation des éléments du jeu, de la gestion des collisions et des interactions
entre le joueur et les objets (voitures, rondins, crocodiles, trous), ainsi que de la
gestion du score et du temps de jeu.
Attributs
joueur : Instance de la classe Joueur , représentant le personnage contrôlé par le
joueur.
voitures : Liste contenant toutes les instances de Voiture présentes dans le jeu.
rondins : Liste des objets de type Rondin , qui permettent au joueur de traverser
la rivière.
Rapport Frogger 16
crocodiles : Liste des instances de Crocodile , servant d'obstacles mobiles dans
l'eau.
trous : Liste des objets de type Trou , où le joueur doit atteindre la fin du niveau.
trousRemplis : Nombre de trous déjà remplis par le joueur.
score : Score actuel du joueur.
casesVisitees : Tableau de booléens permettant de suivre les positions déjà
visitées par le joueur.
gameOver : Indique si la partie est terminée.
temps : Temps restant avant la fin de la partie.
scheduler : Service de planification pour gérer le compte à rebours du jeu.
Constructeur
GameLogic() : Initialise le jeu en créant le joueur, les véhicules, les rondins, les
crocodiles et les trous. Démarre également les threads pour chaque élément
en mouvement.
Méthodes principales
restart() : Réinitialise le jeu en restaurant les positions des éléments, le score, le
temps et les variables de suivi.
startTimer() : Démarre un compte à rebours pour la durée du jeu, en diminuant
temps chaque seconde.
resetTimer() : Réinitialise le temps restant à 60 secondes.
verifierJoueurSurObjet() : Vérifie si le joueur est sur un rondin ou un crocodile. Si ce
n'est pas le cas et qu'il est dans l'eau, il perd une vie. Détecte également les
collisions avec les voitures.
verifierJoueurDansTrou() : Vérifie si le joueur est arrivé dans un trou libre. Si c'est le
cas, le trou est rempli et le joueur gagne des points.
augmenterVitesseElements() : Augmente la vitesse des voitures, rondins et crocodiles
au fil du jeu pour accroître la difficulté.
resetCasesVisitees() : Réinitialise les cases visitées par le joueur.
Rapport Frogger 17
ajouterScore(int points) : Ajoute un nombre donné de points au score du joueur.
Accesseurs et mutateurs
getJoueur() , getVoitures() , getRondins() , getCrocodiles() , getTrous() : Renvoient les listes
d'objets du jeu.
getTrousRemplis() : Retourne le nombre de trous remplis par le joueur.
isGameOver() , setGameOver(boolean valeur) : Vérifie ou définit l'état de la partie.
getScore() : Retourne le score actuel.
estDansEau() : Indique si le joueur est dans la zone de l'eau.
Cette classe est le cœur du jeu, coordonnant les déplacements des objets, le
comportement du joueur et la progression de la partie.
4. Architecture logicielle détaillée
L’application est découpée en 4 couches fonctionnelles principales :
1. Couche Interface Homme-Machine (IHM)
Gère les interactions avec l'utilisateur et l'affichage graphique.
Responsabilités :
Affichage de l’accueil, des écrans de jeu, des scores
Récupération des inputs clavier
Affichage en fonction de l’état du jeu (grenouilles, obstacles, score)
Classes concernées :
AccueilScreen
Jeu
FroggerGame
Rapport Frogger 18
2. Couche Logique de Jeu
Cœur du jeu, traite la logique, les règles, la physique de
collision, les scores.
Responsabilités :
Déplacer les entités (joueurs, obstacles)
Gérer les collisions et interactions
Vérifier les conditions de victoire/défaite
Adapter la difficulté
Mettre à jour les scores et statistiques
Classes concernées :
GameLogic
HighScores (lecture/écriture de stats)
TimerManager ou logique de temps intégrée
Voiture , Rondin , Crocodile , Serpent , Tondeuse
Joueur
3. Couche Réseau et Système Multi-processus
Gère les communications entre les joueurs, la synchronisation
des états de jeu et le serveur.
Responsabilités :
Établir les connexions client-serveur
Créer ou rejoindre des parties réseau
Gérer plusieurs clients en parallèle
Traiter les entrées et sorties réseau
Gérer le matchmaking (recherche de joueurs) et le fallback vers solo
Rapport Frogger 19
Classes concernées :
Serveur (multi-threads, écoute et gestion des parties)
Client (communication avec le serveur, envoie les inputs, reçoit l'état du jeu)
Threads
4. Couche Accès aux Données
Stocke et récupère les informations liées aux joueurs, scores,
configurations.
Responsabilités :
Sauvegarder les performances/joueurs dans un fichier ou une base
Lire les stats et les envoyer au serveur pour adapter la difficulté
Potentiellement charger une configuration personnalisée
Classes concernées :
HighScores
Rapport Frogger 20
Schéma graphique de l’architecture
Rapport Frogger 21
5. Jeu de test
Lorsque l’utilisateur exécute le programme une fenêtre apparaît. L’utilisateur peut
interagir avec 3 boutons : Mode solo, Mode Multijoueur et High Scores.
Commençons par le Mode Solo.
Une fois le Mode Solo sélectionné la machine demande à l’utilisateur d’entrer son
pseudo.
Rapport Frogger 22
Une fois le pseudo entré ici “LePGM21”, la partie débute. Le joueur se retrouve sur
le jeu en 2d. Il peut y voir son personnage, ici une grenouille et les obstacles à
franchir pour accéder jusqu’à l’arrivée (nénuphar). Au départ, le joueur dispose de
3 vies et d’un temps de 60 secondes. De plus, il voit son score, incrémenté de 10 à
chaque déplacement vers l’avant.
Rapport Frogger 23
Il parvient à franchir les obstacles et se place sur le nénuphar en haut à droite. Il
n’en reste plus que 3 !
Malheureusement le joueur échoue à sauver toutes les grenouilles et perd ses 3
vies. Ainsi l’aventure est terminée pour lui dans cette partie. Le jeu lui affiche son
score et lui propose soit de rejouer ou bien de retourner au menu.
Rapport Frogger 24
“LePGM21” se demande si il est dans le classement des 10 meilleurs records c’est
pourquoi il retourne au menu principal et se dirige dans la section Hight Scores.
Rapport Frogger 25
C’est un nouveau record !! Il se place en 10ème position.
Le joueur retourne au menu d’accueil, ça tombe bien, il voulait tester un Mode
Multijoueur. Une fois le bouton cliquer, un menu déroulant apparait. Il a le choix
entre Collaboratif et Compétitif.
Rapport Frogger 26
Il regarde les propositions.
Collaboratif: il a le choix entre Chasseur et Équipe.
Compétitif: il a le choix entre Contre la montre et VS.
Rapport Frogger 27
Une nouvelle fois le jeu demande le pseudo du joueur avant de rentrer dans la
partie.
Il decide de se lancer dans le mode Chasseur ! Une nouvelle fois le jeu lui
demande d’entrer son pseudo.
Le joueur patiente 1 minute le temps que d’autres joueurs se connectent.
Rapport Frogger 28
La partie débute ! Il y a donc au moins un autre joueur de connecté. Si aucun
joueur n’avait rejoint la partie au bout de 1 minute, l’utilisateur aurait été redirigé
vers le mode solo. Un message indique que la partie commence.
Rapport Frogger 29
Nous pouvons voir ici les écrans des deux joueurs. Ils sont dans le mode
chasseur, c’est pourquoi il y a une grenouille lambda (orange) et une grenouille
carnivore (rouge). Le chasseur est sur l’écran de droite. La grenouille est à
gauche. Les joueurs n’apparaissent pas au même endroit afin de laisser une
chance à la grenouille d’arriver à se sauver.
Oh non ! La grenouille carnivore a dévoré la grenouille 3 fois de suite et à ainsi
consommer les 3 vies du joueur. Le hunter est déclaré vainqueur ! L’utilisateur
peut revenir au menu d’accueil s'il veut continuer à joueur et choisir le même ou
un autre mode de jeu.
Rapport Frogger 30
6. Conclusion
Le projet a été divisé en deux grandes parties correspondant aux deux couches
principales de l'architecture logicielle :
la couche Interface Homme-Machine (IHM) et logique locale du jeu,
et la couche Réseau, chargée de la communication entre les joueurs.
Afin de permettre à notre groupe de travailler en parallèle de manière efficace,
tout en assurant une cohérence globale dans le développement.
Alex — Interface et logique de jeu
Alex s’est concentré sur la conception et l’implémentation de la couche IHM et de
la logique de jeu locale, en particulier :
L’écran d’accueil ( AccueilScreen ) avec la sélection des modes de jeu ;
L’interface graphique du jeu ( Jeu ), en lien avec la bibliothèque d’affichage
utilisée ;
La gestion des entités de jeu :
Joueur : mouvement, position, état de la grenouille ;
Voiture , Rondin , Crocodile , trou : comportement et interactions ;
Rapport Frogger 31
La logique principale ( GameLogic ) : déplacements, collisions, conditions de
victoire et défaite.
Son travail a permis de construire un jeu fonctionnel en solo, testable
indépendamment de la partie réseau.
Aaron — Réseau et communication client-serveur
Aaron a pris en charge la partie réseau du projet, avec pour mission d’ajouter les
fonctionnalités multijoueur :
Implémentation du serveur ( Serveur ) capable de :
Gérer plusieurs connexions clients ;
Organiser les différentes parties (coopératif, compétitif, HUNTER...) ;
Adapter la difficulté selon le niveau des joueurs ;
Développement du client réseau ( Client ) :
Envoi des actions du joueur ;
Réception de l’état global du jeu envoyé par le serveur ;
Mise en place d’un système de matchmaking intelligent et de fallback vers le
mode solo si aucun adversaire n’est trouvé.
Le travail a été mené en respectant une structure de communication claire
(protocole de messages entre client et serveur), facilitant l’intégration avec la
partie IHM.
Après le développement des deux parties, nous avons travaillé ensemble pour :
Lier la couche IHM et la couche réseau afin d’obtenir une version fonctionnelle
du jeu en multijoueur ;
Résoudre les problèmes d’intégration et adapter les formats d’échange ;
Réaliser les tests globaux, corriger les bugs et affiner certains comportements
du jeu ;
Rapport Frogger 32
