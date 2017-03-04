# Compilador-Babel
Fase 1: Analitzador lexicogràfic

ESPECIFICACIONS
Versió de Java utilitzada: 1.8

FUNCIONAMENT
La ruta del fitxer en Babel 2017 (.bab) seleccionat per compilar haurá de passar-se com un parametre
en el moment de l'execució del programa.
Per exemple, si volem utilitzar un dels fitxers de proves utilitzats al testing, caldria indicar la següent ruta:
/testing/test1.bab

RESULTATS
Si l'execució s'ha realitzat amb éxit, es generaran dos fitxers els quals contenen els resultats obtiguts:

-> [nom_fitxer_extrada].lex: conté el llistat de <i>tokens</i> identificats al fitxer .bab d'entrada. 
  Per cada, element s'indica el <i>token</i> que li correspon i el lexema associat.
  
-> [nom_fitxer_extrada].err: conté els errors i <i>warning</i> detectats sobre el fitxer durant el procés.

