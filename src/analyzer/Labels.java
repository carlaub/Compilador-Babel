package analyzer;

/**
 * Created by carlaurrea on 20/05/2017.
 */
public class Labels {
    private int labelCount = 0;

    public String getLabel() {
        String newLabel = "eti" + labelCount;
        labelCount ++;
        return newLabel;
    }
}
