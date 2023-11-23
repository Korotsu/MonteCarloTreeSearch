# MonteCarloTreeSearch
## Fonctionnalités

- Charger des fichiers .benchData contenant des données de test.
- Sauvegarder des tests au format .benchData.
- Effectuer un test de performance d'une IA respectant la norme PDDL4.
- Afficher les résultats de test de manière configurable.
- Utilisation simple et pratique à base d'une liste de commande flexible dans un terminal.

## Comment l'utiliser ?

- Tout d'abord se placer dans le dossier src/benchmark/.
- Ensuite ouvrir un terminal de commande windows dans le dossier actuel.
- Ensuite pour utiliser le système, veuillez taper : py benchmark.py suivit des arguments suivants selon votre utilisation :
	- Pour effectuer une série de tests automatisés : --bench ou -b="domain=....;[^1] planner=....;[^2] (la suite est optionnelle) args=....;[^3] min=..; [^4] max=..;[^4] ".
		[^1]: domain= le chemin vers le fichier domain.pddl (chemin à partir de la racine du projet).
		[^2]:planner= le nom du package contenant le planner à tester, remarque : pour qu'un planner custom fonctionne avec le système, il doit avoir dans sa sortie "STATS:TIME=.....;PLAN=....;". 
		[^3]:args= tous les arguments optionnels à rajouter lors de chaque test en entrée de la commande du planner.
		[^4]:min= et max= correspondent respectivement à l'index du début et de fin de test dans un dossier domain de test. (si aucun n'argument est fourni, le test s'effectuera de 0 à max).

		 ##### Exemple de la commande -b ou --bench : 
		 ``` 
		 py benchmark.py -b="domain=src/test/resources/benchmarks/pddl/ipc2002/depots/strips-automatic/domain.pddl; planner=fr.uga.pddl4j.exercise.mcts.MCTS; args=-e FAST_FORWARD -w 1.2 -t1000 -nW 2000 -lW 10 -mS 7; min=5; max=9;". 
		```
	- Pour charger des fichiers .benchData contenant des données de test. : --load ou -l="...."[^5]
		[^5]:  Un chemin fichier ou dossier contenant les données à charger. (chemin à partir de la racine du projet).
		##### Exemple de la commande -l ou --load : 
		 ``` 
		 py benchmark.py -l="src\benchmark\benchmarks\HSP\depots" --load="src\benchmark\benchmarks\MCTS\depots". 

	- Pour afficher les résultats, il faut utiliser la commande --graph ou -g="domain=....;[^6] variable=....;[^7] planner=....;[^8]".
		[^6]: Le domaine utilisé par exemple : depots ou logistic (juste le nom, pas le chemin)(respecter la casse).
		[^7]: Le type de variable à analyser, deux sont actuellement disponibles : "timeSpent" et "planLength".
		[^8]: Le planner à utiliser comme référentiel pour l'axe des abscisses ex: MCTS ou HSP (seulement le nom, pas le chemin)(respecter la casse).
		 ##### Exemple de la commande -g ou --graph: 
		 ``` 
		 py benchmark.py -g="domain=depots; variable=planLength; planner=HSP" --graph="domain=depots; variable=timeSpent; planner=HSP". 
		```
	- Pour sauvegarder les résultats des tests, il suffit juste de rajouter le flag --save ou -s à la commande de base.
		 ##### Exemple de la commande -s ou --save: 
		 ``` 
		 py benchmark.py --save. 
		```
## Installation

Il suffit de clone le projet ainsi que d'avoir matplotlib à jour (installation possible via pip install matplotlib).
