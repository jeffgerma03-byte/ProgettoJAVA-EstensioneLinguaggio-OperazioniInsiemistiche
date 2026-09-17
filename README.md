# Progetto finale LPO a.a. 2024-2025
Il progetto finale consiste nell'implementazione di un'estensione del linguaggio sviluppato durante gli ultimi laboratori Java;
può quindi essere usata come base di partenza la soluzione proposta per l'ultimo laboratorio.
È comunque richiesto che le implementazioni della semantica statica e dinamica siano basate sul visitor pattern.

L'interfaccia da linea di comando per interagire con l'interprete è la stessa utilizzata nei laboratori finali:
- il programma da eseguire può essere letto da un file di testo `<filename>` con l’opzione `-i <filename>`, altrimenti viene letto dallo standard
input
- l'output del programma in esecuzione può essere salvato su un file di testo `<filename>` con l’opzione `-o <filename>`, altrimenti viene usato lo standard output
- l’opzione `-ntc` (abbreviazione di no-type-checking) permette di eseguire il programma senza effettuare prima il controllo di semantica statica del
typechecker 

## Definizione del linguaggio

### Sintassi
Il linguaggio è basato sui seguenti nuovi tipi di token:
- `DIFF` (simbolo `\`)
- `EXP_SEP` (simbolo `|`)
- `SIZE` (simbolo `#`)
- `UNION` (simbolo `++`)
- `FOR` (parola chiave `for`)
- `IN` (parola chiave `in`)
- `WHILE` (parola chiave `while`)

La sintassi del linguaggio è definita da questa grammatica non ambigua e in forma EBNF:

```
Prog ::= StmtSeq EOF
StmtSeq ::= Stmt (STMT_SEP StmtSeq)?
Stmt ::= VAR? IDENT ASSIGN Exp | PRINT Exp | IF OPEN_PAR Exp CLOSE_PAR Block (ELSE Block)? | ASSERT Exp | WHILE OPEN_PAR Exp CLOSE_PAR Block
Block ::= OPEN_BLOCK StmtSeq CLOSE_BLOCK
Exp ::= And (PAIR_OP And)*
And ::= Eq (AND Eq)*
Eq ::= IsIn (EQ IsIn)*
IsIn ::= DiffUnion (IN DiffUnion)* 
DiffUnion ::= Add (SetOp Add)*     
SetOp ::= DIFF | UNION             
Add ::= Mul (PLUS Mul)*
Mul::= Atom (TIMES Atom)*
Atom ::= FST Atom | SND Atom | MINUS Atom | NOT Atom | SIZE Atom | BOOL | NUM | IDENT | OPEN_PAR Exp CLOSE_PAR | SetAtom
SetAtom ::= OPEN_BLOCK (FOR IDENT IN Exp EXP_SEP)? Exp CLOSE_BLOCK
```
La grammatica **non** richiede trasformazioni e può essere utilizzata così com'è per sviluppare un parser con un solo token di lookahead.

Rispetto al linguaggio del laboratorio, sono stati aggiunti
- `SetAtom ::= OPEN_BLOCK Exp CLOSE_BLOCK`: il literal di tipo `set`  
- `SetAtom ::= OPEN_BLOCK FOR IDENT IN Exp EXP_SEP Exp CLOSE_BLOCK`: l'iteratore di tipo `set` 
- tre operatori binari sugli insiemi
  - `IsIn ::= DiffUnion (IN DiffUnion)*`: appartenenza a un insieme
  - `DiffUnion ::= Add (SetOp Add)*`: unione e differenza insiemistica, con stessa precedenza, dove `SetOp ::= DIFF | UNION`
- `Atom ::= SIZE Atom`: operatore unario di cardinalità (dimensione) di un insieme 
- `Stmt ::= WHILE OPEN_PAR Exp CLOSE_PAR Block`: lo statement `while` 

Tutti gli operatori binari sugli insiemi associano da sinistra e la loro precedenza è definita in modo
non ambiguo dalla grammatica. L'operatore unario di cardinalità ha precedenza su tutti gli operatori binari.

### Semantica statica e dinamica 
La semantica statica e dinamica del linguaggio sono definite in F# nel file [`semantics/Semantics.fs`](https://github.com/LPOatDIBRIS/project24-25/blob/main/semantics/Semantics.fs).
Il file [`semantics/Program.fs`](https://github.com/LPOatDIBRIS/project24-25/blob/main/semantics/Program.fs) contiene tutti gli esempi relativi ai programmi definiti in [`tests/success`](https://github.com/LPOatDIBRIS/project24-25/tree/main/tests/success).
Esso può essere eseguito con il comando `dotnet run`. 

#### Semantica statica

Il costruttore `SetType of staticType` corrisponde al nuovo tipo `set` degli insiemi i cui elementi hanno tipo `staticType`; quindi, ogni insieme
deve contenere tutti elementi dello stesso tipo.

Per esempio, `SetType IntType` è il tipo degli insiemi di interi, oppure  `SetType(SetType(PairType(IntType,IntType)))` è il tipo
degli insiemi i cui elementi sono, a loro volta, insiemi di coppie di numeri interi.

##### Regole della semantica statica
- se in `env` `exp1` ha tipo `SetType typ` e `exp2` ha tipo `SetType typ`, allora in `env` `Diff exp1 exp2` e `Union exp1 exp2` hanno tipo `Set typ` 
- se in `env` `exp1` ha tipo `typ` e `exp2` ha tipo `SetType typ`, allora in `env` `IsIn exp1 exp2` ha tipo `Bool`
- se in `env` `setExp` ha tipo `SetType typ` e in `env2` `elemExp` ha tipo `elTyp`, dove `env2` è ottenuto da `env` aggiungendo la
dichiarazione a livello annidato di `var` con tipo `typ`, allora in `env` `SetEnum(var,setExp,elemExp)` ha tipo `SetType elType` 
- se in `env` `exp` ha tipo `typ`, allora `SetLit(exp)` ha tipo `SetType typ` 
- se in `env` `exp` ha tipo `SetType typ`, allora `Size exp` ha tipo `Int` 
- se in `env` `exp` ha tipo `Bool` e `block` è corretto, allora in `env` `While(exp,block)` è corretto in `env`

#### Semantica dinamica

Il costruttore `SetValue of value Set` rappresenta i valori di tipo set, usando il tipo predefinito F# `value Set` degli insiemi 
con elementi di tipo `value`. 

*Note*:
- nell'implementazione in Java usare un corrispondente tipo definito nel package `java.util` per implementare gli insiemi.
- diversamente dai vincoli della semantica statica, la semantica dinamica ammette che un insieme possa contenere valori di tipo diverso (come accade per esempio nel programma `tests/failure/static-semantics/prog01.txt`).


##### Regole della semantica dinamica

- se in `env` `exp1` si valuta in `val1` e `set2` si valuta in `val2`, allora in `env` Diff exp1 exp2` si valuta in `SetValue(Set.difference (toSet val1) (toSet val2)) 
- se in `env` `exp1` si valuta in `val1` e `set2` si valuta in `val2`, allora in `env` `IsIn exp1 exp2` si valuta in `BoolValue(Set.contains val1 (toSet val2))` 
- se in `env` `setExp` si valuta in `val`, `newEnv` è l'ambiente ottenuto da `env` aggiungendo la
dichiarazione a livello annidato di `variable` con  un qualsiasi valore di inizializzazione (per esempio `IntValue 0`), allora  in `env` `SetEnum(variable,setExp,elemExp)` si valuta in `SetValue s`, dove `s` è l'insieme di tutti i valori accumulati a partire dall'insieme vuoto `Set.empty` tramite la valutazione di `elemExp` nell'ambiente `newEnv` dove il valore di `variable` è aggiornato con `setElem`, per ogni elemento `setElem` in `toSet val`
- se in `env` `exp` si valuta in `val`, allora in `env` `SetLit(exp)` si valuta in `Set.add val Set.empty` 
- se in `env` `exp` si valuta in `val`, allora in `env` `Size exp` si valuta in `IntValue(Set.count (toSet val))`
- se in `env` `exp1` si valuta in `val1` e `set2` si valuta in `val2`, allora in `env` Union exp1 exp2` si valuta in `SetValue(Set.union (toSet val1) (toSet val2)) 
- se in `env` `exp` si valuta in `val` e `toBool val` è vero, allora in `env` l'esecuzione di `While(exp,block)` è equivalente all'esecuzione in `env2` di  `While(exp,block)`, dove `env2` è ottenuto dall'esecuzione in `env` di `block`. Se invece `toBool val` è falso, allora in `env` l'esecuzione di `While(exp,block)` restituisce `env` 
 - conversione in stringa dei valori di tipo set: se `s` è un valore di tipo set, allora la sua stampa produce la stringa `"setOfSize("+n+")"` dove `n` è la conversione in stringa della cardinalità di `s`
  - uguaglianza tra valori di tipo set: due valori di tipo set sono uguali se e solo se rappresentano lo stesso insieme. **Nota bene**: gli elementi di un insieme **non** sono ordinati  
 
  *Note sull'iterazione di insiemi* `SetEnum(variable,exp1,exp2)`
  - la variabile `variable` per iterare sugli elementi dell'insieme `exp1` viene dichiarata in un nuovo livello più annidato ed è utilizzata esclusivamente per valutare l'espressione  `exp2` (ossia, lo scope è limitato alla sola espressione `exp2`)
  - poiché la variabile `variable` è dichiarata in un livello più annidato, essa può coprire le dichiarazioni di variabili con lo stesso nome presenti in livelli più esterni.

    Esempio: nell'espressione
    ```
    {for x in {{1}}++{{2}} |
       {for x in
          x |
	       x+1}
     ++x}
    ```
    le `x` alla linea 3 e 5 corrispondono alla dichiarazione `for x` alla linea 1, mentre la `x` alla linea 4 corrisponde alla dichiarazione `for x` alla linea 2

  *Nota sugli operatori `Diff` e `Union`*: gli insiemi sono valori *immutabili*, quindi le corrispondenti operazioni restituiscono dei nuovi valori e non possono modificare quelli preesistenti.


### Contenuto del repository

* [`semantics/Semantica.fs`](https://github.com/LPOatDIBRIS/project24-25/blob/main/semantics/Semantics.fs) : semantica statica e dinamica del linguaggio definita in F#
* [`semantics/Program.fs`](https://github.com/LPOatDIBRIS/project24-25/blob/main/semantics/Program.fs) : contiene tutti gli esempi relativi ai programmi definiti in `tests/success`, eseguibili con il comando `dotnet run`
* [`tests/success`](https://github.com/LPOatDIBRIS/project24-25/tree/main/tests/success): test corretti anche **senza** l'opzione `-ntc`
* [`tests/failure/syntax`](https://github.com/LPOatDIBRIS/project24-25/tree/main/tests/failure/syntax): test con errori di sintassi 
* [`tests/failure/static-semantics`](https://github.com/LPOatDIBRIS/project24-25/tree/main/tests/failure/static-semantics): test con errori statici **senza** l'opzione `-ntc` ed errori dinamici **con** l'opzione `-ntc`
* [`tests/failure/static-semantics-only`](https://github.com/LPOatDIBRIS/project24-25/tree/main/tests/failure/static-semantics-only): test con errori statici **senza** l'opzione `-ntc` e corretti con l'opzione `-ntc`

## Modalità di consegna

- La consegna è valida solo se il **progetto passa tutti i test** contenuti nel folder `tests`; la valutazione del progetto tiene conto dell'esecuzione di test aggiuntivi e della qualità del codice
- Sono disponibili cinque turni di consegna con scadenze in prossimità delle date delle prove scritte.
  Il calendario è disponibile nella [sezione di AulaWeb](https://2024.aulaweb.unige.it/mod/assign/view.php?id=57644) per la consegna del progetto.

  Dopo la scadenza di ogni turno, vengono corretti i progetti consegnati e pubblicati i relativi risultati prima che le consegne siano riaperte.

  **Dopo la scadenza dell'ultimo turno del 2026 non sarà più possibile consegnare progetti validi per l'anno accademico in corso**
- Il progetto può essere consegnato anche se l'esame scritto non è stato ancora superato
- Dopo il commit (e push) finale del progetto su GitHub, la consegna va segnalata da **un singolo componente del gruppo** utilizzando [AulaWeb](https://2024.aulaweb.unige.it/mod/assign/view.php?id=57644) e indicando **il numero del gruppo** definito nell'[elenco su AulaWeb](https://2024.aulaweb.unige.it/mod/wiki/view.php?id=57641)
- Per ricevere supporto durante lo sviluppo del progetto è consigliabile tenere sempre aggiornato il codice del progetto sul repository GitHub  
- Dopo che il progetto è stato valutato positivamente, il relativo colloquio **individuale** può essere sostenuto  anche se l'esame scritto non è stato ancora superato; esso ha lo scopo di verificare che ogni componente del gruppo abbia compreso il funzionamento del codice e abbia contribuito attivamente al suo sviluppo
- L'**OpenBadge Soft skills - Sociale base 1 - A** verrà assegnato ai componenti del gruppo solo se **tutti** avranno superato positivamente (ossia senza decremento del punteggio) il colloquio individuale
- Per ulteriori informazioni consultare la [pagina AulaWeb sulle modalità di esame](https://2024.aulaweb.unige.it/mod/page/view.php?id=57633)
