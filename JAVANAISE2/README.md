# Javanaise Project
This is a academic project of 2nd years of master at UGA.
The goal of this project is learn how make distributed cache.

## For teacher
La version 1 est opérationnel.
La version 2 est opérationnel.

Extension : 

- [x] Ajout d'un Bouton unlock et de ne pas faire le unlock 
sur read et write pour faire des tests
- [x] Tester en mode burst : stresser le systéme 
- [x] Optimisation de la synchro coord/jvnObject. Toutes les méthodes du coord sont synchronized -> voir comment // read et write sur 2 objets diff
Reduire le synchronized au grain du contenu de la methode
- [x] Terminaison propre d'un client
- [x] Pannes client
- [x] Pannes coord
- [ ] Invocation transactionnelles 