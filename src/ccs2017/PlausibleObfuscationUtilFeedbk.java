package ccs2017;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nsleheen on 5/14/2017.
 */
public class PlausibleObfuscationUtilFeedbk {
    public static void main(String[] args) throws IOException {

        double[][] markovChain = getSampleMarkovChain();

        PlausibleObfuscationUtilFeedbk obj = new PlausibleObfuscationUtilFeedbk();

        List<int[]> allPaths = GenAllPath(markovChain, new int[]{0}, new int[]{9}, 7);
        int[] sensitiveNodes = new int[]{5};
        System.out.println("Total paths: " + allPaths.size());
        for (int[] path : allPaths) {
            for (int nd : path) System.out.print(nd + "->");
            System.out.println("::: prior=" + obj.getPathProbability(path, markovChain));
//            obj.PlausibleObfuscationAlgorithm(path, 0.4, markovChain, allPaths, sensitiveNodes);
        }

        int[] path = new int[]{0, 1, 2, 5, 7, 8, 9};
//
        obj.PlausibleObfuscationAlgorithm(path, 0.4, markovChain, allPaths, sensitiveNodes);

    }

    /*Example paper*/
    static public double[][] getSampleMarkovChain() {
        double[][] markovChain = new double[10][10];
        markovChain[0][1] = 1;

        markovChain[1][2] = 0.5;
        markovChain[1][3] = 0.5;

        markovChain[2][4] = 0.5;
        markovChain[2][5] = 0.5;

        markovChain[3][4] = 0.5;
        markovChain[3][5] = 0.5;

        markovChain[4][6] = 1.0;

        markovChain[5][6] = 0.5;
        markovChain[5][7] = 0.5;

        markovChain[6][8] = 1.0;
        markovChain[7][8] = 1.0;

        markovChain[8][9] = 1.0;
        return markovChain;
    }


    static public double[][] getSampleMarkovChain1() {
        double[][] markovChain = new double[12][12];
        markovChain[0][1] = 1;

        markovChain[1][2] = 0.4;
        markovChain[1][3] = 0.3;
        markovChain[1][4] = 0.3;

        markovChain[2][6] = 1.0;
        markovChain[3][5] = 0.4;
        markovChain[3][6] = 0.6;
        markovChain[4][5] = 0.5;
        markovChain[4][6] = 0.5;

        markovChain[5][7] = 0.5;
        markovChain[5][8] = 0.5;
        markovChain[6][7] = 0.3;
        markovChain[6][8] = 0.5;
        markovChain[6][9] = 0.2;

        markovChain[7][10] = 1.0;
        markovChain[8][10] = 1.0;
        markovChain[9][10] = 1.0;

        markovChain[10][11] = 1.0;
        return markovChain;
    }

    public static List<int[]> GenAllPath(double[][] M, int[] sNodes, int[] eNodes, int T) {
        List<int[]> allPaths = new ArrayList<>();

        for (int sNode : sNodes) {
            int[] x = new int[T];
            x[0] = sNode;
            genPathsRec(x, 1, M, eNodes, T, allPaths);
//            break;
        }
        return allPaths;
    }

    private static void genPathsRec(int[] x, int curIndx, double[][] M, int[] eNode, int T, List<int[]> allPaths) {
        if (isContains(x[curIndx - 1], eNode)) {
            int[] y = new int[curIndx];
            for (int i=0; i<curIndx; i++) y[i]=x[i];
            allPaths.add(y);
            return;
        }

        int preNode = x[curIndx - 1];
        for (int i = 0; i < M.length; i++)
            if (M[preNode][i] > 0) {
                x[curIndx] = i;
                genPathsRec(x, curIndx + 1, M, eNode, T, allPaths);
            }
    }

    private static boolean isContains(int val, int[] A) {
        for (int v : A)
            if (val == v)
                return true;
        return false;
    }

    int[] PlausibleObfuscationAlgorithm(int[] x, double delta, double[][] M, List<int[]> allPaths, int[] sensitiveNodes) {
//    int PlausibleObfuscationAlgorithm(int[] x, double delta, double[][] M, List<int[]> allPaths, int[] sensitiveNodes) {

        int sai_util = x.length;
        List<int[]> IndistingSet = null;
        boolean isPrivate = false;
        while (!isPrivate && sai_util >=0) {
            IndistingSet = GenIndistingSet(x, M, allPaths, sai_util);

//            System.out.println("Indisting set size: "+IndistingSet.size() + ":: Util: "+sai_util);
            if (IndistingSet == null || IndistingSet.size() == 0){ sai_util--; continue;}
            isPrivate = CheckPrivacy(IndistingSet, delta, M, sensitiveNodes, allPaths, sai_util);
            if (!isPrivate)
                sai_util--;
        }
//        System.out.println(">>>>>Indisting set size: "+IndistingSet.size() + ":: Util: "+sai_util + " ::: privacy check="+isPrivate);
//        assert IndistingSet != null;
        if (!isPrivate) {
            int[] x_tilde = new int[x.length];
            for (int i=0; i<x.length; i++) x_tilde[i]=-1;
            return null;
//            return x_tilde;
//            return sai_util;
        }
        int index = (int) (Math.random() * (IndistingSet.size()));
//        index %= IndistingSet.size();
//        System.out.print(","+IndistingSet.size() + ": "+index);
        int[] x_tilde = IndistingSet.get(index);
//        return sai_util;
        return x_tilde;
    }

    /*
    * Algorithm 3 Check-Privacy() ----------- START --------------------------------------
    * */
    private boolean CheckPrivacy(List<int[]> indistingSet, double delta, double[][] M, int[] sensitiveNodes, List<int[]> allPaths, int sai_util) {
        boolean isPrivate = true;
        double worst_case_prior = getWorstCasePrior(allPaths, sensitiveNodes, M);
        double worst_case_posterior = getWorstCasePosterior(indistingSet, M, sensitiveNodes, allPaths, sai_util);

//        System.out.println("Worst case posterior = " + worst_case_posterior + " / worst case prior = "+worst_case_prior);

        if (worst_case_posterior / worst_case_prior > 1 + delta) {
            isPrivate = false;
            return isPrivate;
        } else if (worst_case_posterior > 0){
//            System.out.print(",WorstCasePost="+worst_case_posterior+",worstCasePrior="+worst_case_prior+",post/pre="+(worst_case_posterior / worst_case_prior));
//            System.out.println((worst_case_posterior / worst_case_prior) + ","+(1+delta));
        }

        return isPrivate;
    }

    private double getWorstCasePosterior(List<int[]> indistingSet, double[][] M, int[] sensitiveNodes, List<int[]> allPaths, int sai_util) {
        double maxPrior = 0.0;
        for (int[] path : indistingSet) {
            if (isSensitivePath(path, sensitiveNodes)) {
                maxPrior = Math.max(maxPrior, getPosterior(path, indistingSet, M, allPaths, sai_util));
            }
        }
        return maxPrior;
    }

    private double getPosterior(int[] s, List<int[]> indistingSet, double[][] M, List<int[]> allPaths, int sai_util) {

        double postProb = 0;
//        List<int[]> indSenSet = GenIndistingSet(s, M, allPaths, sai_util);
        int indSenSetSize = getIndistingSetSize(s, M, allPaths, sai_util);
        for (int[] x_p : indistingSet) {
//            List<int[]> indTempSet = GenIndistingSet(x_p, M, allPaths, sai_util);
            int indTempSetSize = getIndistingSetSize(x_p, M, allPaths, sai_util);

            postProb = postProb + (1.0 * indSenSetSize / indTempSetSize);
        }
//        postProb/=indSenSet.size();
        postProb = 1.0 / postProb;

        return postProb;
    }
    Map<String, Integer> indistingSetSize = new HashMap<>();

    int getIndistingSetSize(int[] x, double[][] M, List<int[]> allPaths, int sai_util) {
        String s=sai_util+"";
        for (int v:x) s=s+v;
        if (!indistingSetSize.containsKey(s)) {
            List<int[]> indSenSet = GenIndistingSet(x, M, allPaths, sai_util);
            indistingSetSize.put(s, indSenSet.size());
        }
        return indistingSetSize.get(s);
    }


    private double getWorstCasePrior(List<int[]> indistingSet, int[] sensitiveNodes, double[][] M) {
        double minPrior = 1.0;
        for (int[] path : indistingSet) {
            if (isSensitivePath(path, sensitiveNodes)) {
                minPrior = Math.min(minPrior, getPathProbability(path, M));
            }
        }
        return minPrior;
    }

    public double getPathProbability(int[] path, double[][] M) {
        double prob = 1;
        for (int i = 1; i < path.length; i++) {
            prob *= M[path[i - 1]][path[i]];
        }
        return prob;
    }

    public boolean isSensitivePath(int[] path, int[] sensitiveNodes) {
        for (int node : path) {
            for (int sNode : sensitiveNodes)
                if (node == sNode)
                    return true;
        }
        return false;
    }


    /*-------End Algorithm 03 -------------------------*/

    private List<int[]> GenIndistingSet(int[] x, double[][] m, List<int[]> allPaths, int sai_util) {
        List<int[]> IndistingSet = new ArrayList<int[]>();
        for (int[] path : allPaths) {
            int util = calculateUtility(x, path);
            if (util == sai_util)
                IndistingSet.add(path);
        }
        return IndistingSet;
    }

    public static int calculateUtility(int[] x, int[] y) {
        int util = 0;
        for (int i = 0; i < x.length; i++)
            if (x[i] == y[i] || (x[i] == 2 && y[i] == 3) || (x[i] == 3 && y[i] == 2))
                util++;
        return util;
    }

    public static double calculateSensitive(int[] x, int[] sensitiveNodes) {
        int sens = 0;
        for (int v:x) {
            for (int s:sensitiveNodes) {
                if (v==s) {
                    sens++;
                    break;
                }
            }
        }

        return sens;
    }
}
