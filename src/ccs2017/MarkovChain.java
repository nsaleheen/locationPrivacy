package ccs2017;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by nsleheen on 5/16/2017.
 */
public class MarkovChain {

    double[][] MC;
    int[] startState;
    int[] endState;

    void loadMC(String filenameMC, String filenameStartState, String filenameEndState) throws FileNotFoundException {

        Scanner in = new Scanner(new File(filenameMC));
        int r = 0;
        int cols = 0;
        while (in.hasNext()) {
            String line = in.nextLine();
            String[] toks = line.split(",");
            if (MC == null) {
                cols = toks.length;
                MC = new double[toks.length][toks.length];
            }
            for (int c = 0; c < toks.length; c++)
                MC[r][c] = Double.parseDouble(toks[c]);
            r++;
        }

        in = new Scanner(new File(filenameStartState));
        String line = in.nextLine();
        String[] toks = line.split(",");
        if (startState == null) {
            startState = new int[toks.length];
        }
        for (int c = 0; c < toks.length; c++)
            startState[c] = Integer.parseInt(toks[c]);


        in = new Scanner(new File(filenameEndState));
        line = in.nextLine();
        toks = line.split(",");
        if (endState == null) {
            endState = new int[toks.length];
        }
        for (int c = 0; c < toks.length; c++)
            endState[c] = Integer.parseInt(toks[c]);

        System.out.println("MC="+r+"x"+cols + ":: #startStates = "+startState.length+ ":: #endStates = "+endState.length);
    }

}
