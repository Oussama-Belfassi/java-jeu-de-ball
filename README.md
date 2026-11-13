# Jeu de Ball (casse-briques, Java Swing)

Jeu de balle : une raquette, une balle et des briques à détruire (3 niveaux, 3 vies).

## Commandes
- Souris ou flèches ← → (ou A / D) : déplacer la raquette
- Clic ou Espace : lancer la balle / pause / rejouer
- R : recommencer

## Lancer
```
java -jar JeuDeBall.jar
```

## Compiler
```
javac -d out src/ball/*.java
printf "Main-Class: ball.Main\n" > manifest.txt
jar cfm JeuDeBall.jar manifest.txt -C out .
```
