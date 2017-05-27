package analyzer;


class Labels {
    private static int labelCount = 0;

    String getLabel() {
        //He afegit el guió per a evitar que un usuari pugui crear una funció amb el mateix nom que una etiqueta nostra
        String newLabel = "eti-" + labelCount;
        labelCount ++;
        return newLabel;
    }
}
