#!/bin/bash

# 1. Vérification qu'un nom de dossier a bien été fourni
if [ -z "$1" ]; then
    echo "Erreur : Tu dois préciser le nom du dossier à tester."
    echo "Utilisation : ./run_tests.sh <nom_dossier>"
    echo "Exemple     : ./run_tests.sh caca"
    echo "Exemple     : ./run_tests.sh pipi"
    exit 1
fi

# Définition du chemin du dossier et du fichier de sortie
dossier="./tests/$1"
output_file="./tests/output_$1.txt"

# 2. Vérification de l'existence du dossier
if [ ! -d "$dossier" ]; then
    echo "Erreur : Le dossier $dossier est introuvable."
    exit 1
fi

echo "Lancement des tests dans le dossier : $dossier"

# Nettoyage et création du nouveau fichier de sortie
rm -f "$output_file"
touch "$output_file"

total=0
valides=0

for filename in "$dossier"/*.bloc; do
    # Sécurité au cas où le dossier ne contient pas de fichier .bloc
    [ -e "$filename" ] || continue 

    ((total++))
    echo -n "Exécution de $(basename "$filename")... "
    
    echo "-------------------------------------------------------------------------------------" >> "$output_file"
    echo "Treating $filename" >> "$output_file"
    
    # On affiche tout le contenu du fichier testé dans l'output
    cat "$filename" >> "$output_file"
    echo -e "\n\n" >> "$output_file"
    
    # Exécution du compilateur
    # Nouvelle ligne (miniJava) :
    java -cp "bin/cls:tools/*" fr.n7.stl.minijava.Driver "$filename" >> "$output_file" 2>&1
    status=$?
    
    # 3. Comptage des succès (on valide si le compilateur retourne 0)
    if [ $status -eq 0 ]; then
        ((valides++))
        echo "OK"
    else
        echo "KO (Erreur)"
    fi
done

echo -e "\n========================================================" | tee -a "$output_file"
echo "Bilan : $valides tests réussis sur $total tests au total." | tee -a "$output_file"
echo "========================================================" | tee -a "$output_file"
echo "Les résultats détaillés sont dans $output_file"