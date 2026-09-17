# Progetto Finale LPO 2024-2025: Estensione del Linguaggio con Operazioni Insiemistiche

Questo repository contiene la mia implementazione del progetto finale per il corso di Linguaggi e Paradigmi di Programmazione (LPO) a.a. 2024-2025. 

Il progetto estende l'interprete Java sviluppato durante i laboratori del corso, aggiungendo il supporto per il tipo di dato `Set` (insiemi), operazioni insiemistiche, iteratori e il costrutto di controllo `while`. L'intera architettura della semantica statica (type-checking) e dinamica (valutazione) è stata rigorosamente implementata sfruttando il **Visitor Pattern**.

## Estensioni Implementate

Rispetto al linguaggio base del laboratorio, ho aggiunto il supporto per i seguenti costrutti e operazioni:

### 1. Costrutti di Controllo e Tipi Base
* **Statement `while`:** Implementazione del ciclo base con sintassi `while (Exp) Block`.
* **Insiemi (Set):** Aggiunta del costruttore `SetType`, che garantisce (staticamente) che un insieme contenga elementi tutti dello stesso tipo. (Es. `SetType(IntType)` o insiemi annidati come `SetType(SetType(IntType))`). A livello dinamico, i `Set` sono immutabili e implementati tramite il package `java.util`.

### 2. Operazioni Insiemistiche
La grammatica è stata estesa per supportare nuove operazioni, gestendo correttamente le precedenze e l'associatività a sinistra:
* **Costruzione e Iterazione:**
    * Literal `Set`: `{ Exp }`
    * Iteratore `Set` (Set Comprehension): `{ for IDENT in Exp | Exp }`. La variabile dichiarata `IDENT` vive in un nuovo livello di scope annidato, limitato alla valutazione della seconda espressione.
* **Operazioni Binarie:**
    * Appartenenza `in`: `Exp in Exp` (Ritorna un booleano).
    * Unione `++`: `Exp ++ Exp`.
    * Differenza `\`: `Exp \ Exp`.
* **Operazioni Unarie:**
    * Cardinalità `#`: `# Exp` (Restituisce la dimensione dell'insieme).

## Utilizzo dell'Interprete da Riga di Comando

L'interprete accetta i seguenti argomenti:

* `-i <filename>`: Legge il codice sorgente dal file specificato (in assenza, usa lo standard input).
* `-o <filename>`: Scrive l'output sul file specificato (in assenza, usa lo standard output).
* `-ntc`: Flag **No-Type-Checking**. Disabilita la fase di semantica statica ed esegue direttamente l'interprete dinamico. Utile per testare la robustezza dinamica o per eseguire programmi che fallirebbero il controllo dei tipi (es. insiemi con elementi di tipi misti).

## Struttura della Repository
* `/src`: Contiene tutto il codice sorgente Java dell'interprete (Lexer, Parser, AST e Visitors).
* `/semantics`: Contiene le specifiche formali della semantica statica e dinamica scritte in F# (`Semantics.fs`), fornite come riferimento per l'implementazione. Presente anche `Program.fs` per eseguire gli esempi tramite `dotnet run`.
* `/tests`: Una suite completa di test-case per validare l'implementazione:
    * `/success`: Programmi validi.
    * `/failure/syntax`: Programmi con errori sintattici.
    * `/failure/static-semantics`: Programmi che falliscono il type-checking ma che sollevano eccezioni dinamiche se avviati con `-ntc`.
    * `/failure/static-semantics-only`: Programmi scartati dal type-checker ma eseguibili con successo bypassando i controlli con `-ntc`.

## Note Implementative
* **Immutabilità:** Le operazioni come Unione (`++`) e Differenza (`\`) restituiscono sempre *nuovi* valori set, garantendo l'immutabilità richiesta dal linguaggio.
* **Visitor Pattern:** Separazione netta tra logica di parsing (AST) ed esecuzione. Il type-checking e la valutazione dinamica sono incapsulati in Visitor dedicati.
