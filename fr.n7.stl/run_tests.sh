
#!/bin/bash

# Définition du chemin du fichier de sortie
output_file="./tests/output.txt"

# 1. Vérification de l'argument (ok ou ko)
if [ "$1" == "ok" ]; then
    dossier="./tests/Test_OK"
    echo "Lancement des tests censés FONCTIONNER (OK)..."
elif [ "$1" == "ko" ]; then
    dossier="./tests/Test_KO"
    echo "Lancement des tests censés ÉCHOUER (KO)..."
else
    echo "Erreur : Tu dois préciser quel dossier tester."
    echo "Utilisation : ./run_tests.sh ok   OU   ./run_tests.sh ko"
    exit 1
fi

# 2. Vérification de l'existence du dossier
if [ ! -d "$dossier" ]; then
    echo "Erreur : Le dossier $dossier est introuvable."
    exit 1
fi

# Nettoyage et création du nouveau fichier
rm -f "$output_file"
touch "$output_file"

total=0
valides=0

for filename in "$dossier"/*.bloc; do
    [ -e "$filename" ] || continue 

    ((total++))
    echo "Exécution de $(basename "$filename")..."
    
    echo "-------------------------------------------------------------------------------------" >> "$output_file"
    echo "Treating $filename" >> "$output_file"
    
    # ICI : On affiche tout le contenu du fichier au lieu de juste la ligne 2
    cat "$filename" >> "$output_file"
    echo "" >> "$output_file"
    echo "" >> "$output_file"
    
    # Exécution du compilateur
    java -cp "bin/cls:tools/*" fr.n7.stl.minic.Driver "$filename" >> "$output_file" 2>&1
    status=$?
    
    # 3. Comptage des succès
    if [ "$1" == "ok" ] && [ $status -eq 0 ]; then
        ((valides++))
    elif [ "$1" == "ko" ] && [ $status -ne 0 ]; then
        ((valides++))
    fi
done

echo -e "\n========================================================" | tee -a "$output_file"
echo "Bilan ($1) : $valides tests valides sur $total tests au total." | tee -a "$output_file"
echo "========================================================" | tee -a "$output_file"
echo "Les résultats détaillés sont dans $output_file"
