# Specificatii si Cerinte - Sistem Distribuit de Stocare (Etapa 1)

## 1. Cerinte Functionale (Operatii)

### Incarcare fisier (put)
* Comanda: put <nume_fisier>
* Pasi prin sistem: 1. Clientul cere coordonatorului doua noduri active, distincte; 2. Trimite fisierul primului nod; 3. Primul nod il copiaza pe al doilea; 4. Dupa ambele confirmari, coordonatorul inregistreaza metadatele.
* Rezultat: ID-ul fisierului si cele doua noduri stocatoare.
* Erori tratate: Mai putin de 2 noduri active (refuz), fisier local inexistent, o copie esuata (se alege alt nod).

### Descarcare fisier (get)
* Comanda: get <nume_fisier> <destinatie>
* Pasi prin sistem: 1. Clientul cere coordonatorului locatiile copiilor; 2. Descarca de la primul nod; 3. Verifica checksum-ul.
* Rezultat: Fisierul salvat local, cu checksum verificat.
* Erori tratate: Fisier necunoscut, primul nod nu raspunde sau checksum gresit (incearca a doua copie), nicio copie accesibila (eroare explicita).

### Listare fisiere (ls)
* Comanda: ls
* Pasi prin sistem: Clientul trimite o cerere catre coordonator, care returneaza tabela curenta de metadate.
* Rezultat: Tabel in terminal cu ID-ul, numele, dimensiunea, checksum-ul si nodurile pe care se afla fiecare fisier.
* Erori tratate: Coordonator inactiv / eroare de retea.

### Stergere fisier (rm)
* Comanda: rm <nume_sau_id>
* Pasi prin sistem: 1. Clientul trimite cererea coordonatorului; 2. Coordonatorul trimite comanda de stergere nodurilor care detin copiile; 3. Coordonatorul sterge intrarea din metadate.
* Rezultat: Fisierul este sters de pe noduri si din metadate.
* Erori tratate: Fisier inexistent; daca un nod cu o copie este cazut in acel moment, stergerea se aplica pe cel activ, iar cel cazut va fi curatat ulterior.

### Starea sistemului (nodes)
* Comanda: nodes
* Pasi prin sistem: Clientul cere coordonatorului starea clusterului.
* Rezultat: Listarea nodurilor cunoscute, starea lor (activ/suspect/cazut) si cine este coordonator.
* Erori tratate: Coordonator nedisponibil.

### Adaugarea unui nod nou (perspectiva sistemului)
* Pasi prin sistem: Un nou nod porneste, se anunta in retea sau la coordonator, iar coordonatorul il adauga automat in lista de noduri active.

---

## 2. Modelul de date si Decizii
* Identificare fisier: Prin ID unic (UUID) generat de coordonator si prin nume (pentru comenzile utilizatorului).
* Nume existente: La incarcarea unui fisier cu un nume existent, se creeaza o noua intrare cu UUID distinct (sau versiune noua).
* Dimensiune maxima: Limitata de spatiul disponibil pe nodurile de stocare locale.

---

## 3. Cerinte Neffuncționale si Obiective Masurabile
* Integritate: 0 fisiere cu checksum diferit din 100 descarcari (testat prin script in Etapa 2).
* Descoperire: Un nod nou apare in lista celorlalți in cel mult 3 secunde (Etapa 2).
* Detectarea defectelor: Un nod oprit cu kill -9 este marcat cazut in cel mult 5 secunde (Etapa 3).
* Scalabilitate: Debitul de incarcare cu 3 noduri creste fata de 1 nod (Etapa 4).
* Coordonator: Noul coordonator este ales in cel mult 10 secunde dupa caderea vechiului lider (Etapa 5).
* Disponibilitate: Dupa oprirea oricarui nod, 100% din fisiere raman descarcabile (Etapa 7).
* Ipoteze de lucru: Retea locala (LAN), cel mult un nod cazut la un moment dat, defecte de tip crash (fara comportament bizantin).

---

## 4. Scenariul Demonstratiei (Acceptance Test)
1. Pornire noduri n1, n2, n3 in terminale separate (se descoperi; n1 e coordonator) - Etapa 2
2. put raport.pdf din client (afiseaza ID si 2 noduri distincte) - Etapa 2
3. get raport.pdf (fisier descarcat cu checksum identic) - Etapa 2
4. Pornire nod nou n4 fara oprirea serviciului (apare in nodes) - Etapa 4
5. kill -9 pe un nod care detine o copie (marcat cazut, get reuseste din a doua copie) - Etapa 3
6. kill -9 pe coordonator (nodurile ramase aleg un nou lider prin Bully) - Etapa 5
7. ls si get dupa realegere (metadate reconstruite, fisierul ramane accesibil) - Etapa 6/7

---

## 5. Schita Protocolului
* ALLOCATE (client -> coordonator): nume, dimensiune -> raspuns: NODES (cu ID si 2 noduri) sau ERROR.
* PUT (client -> nod): ID, metadate + octeti -> raspuns: STORED sau ERROR.
* REPLICATE (nod -> nod): Copierea fisierului catre al doilea nod.
* COMMIT (nod -> coordonator): Confirmare stocare copii -> raspuns: OK.
* LOCATE (client -> coordonator): nume sau ID -> raspuns: LOCATIONS.
* GET (client -> nod): Cerere flux fisier.
* LIST / DELETE / NODES: Mesaje pentru operatiile ls, rm si starea sistemului.