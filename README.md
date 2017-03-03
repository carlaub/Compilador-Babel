# Compilador-Babel
<h1>Fase 1: Analitzador lexicogràfic</h1>

<h2>Especificacions</h2>
Versió de Java utilitzada: 1.8

<h2>Funcionament</h1>
<p>La ruta del fitxer en Babel 2017 (.bab) seleccionat per compilar haurá de passar-se com un parametre
en el moment de l'execució del programa.</p>
<p>Per exemple, si volem utilitzar un dels fitxers de proves utilitzats al <i>testing</i>, caldria indicar la següent ruta:</p>
<p><code>/testing/test1.bab</code></p>

<h2>Resultats</h1>
<p>Si l'execució s'ha realitzat amb éxit, es generaran dos fitxers els quals contenen els resultats obtiguts:</p>
<ul>
  <li>[nom_fitxer_extrada]<b>.lex</b>: conté el llistat de <i>tokens</i> identificats al fitxer .bab d'entrada. 
  Per cada, element s'indica el <i>token</i> que li correspon i el lexema associat.</li>
  <li>[nom_fitxer_extrada]<b>.err</b>: conté els errors i <i>warning</i> detectats sobre el fitxer durant el procés.</li>
</ul>
