package ccs2017;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nsleheen on 5/15/2017.
 */
public class FeedbackEffect {
    public static void main(String[] args) throws IOException {


        MarkovChain mChain = new MarkovChain();
        String dir = "C:\\Users\\nsleheen\\DATA\\MSR dataset_MC\\";
//        String dir = "C:\\Users\\sakther\\DATA\\reality_mining_data\\";
        String mcFile = dir + "mc_new_120minDiv2.csv";
        String ssFile = dir + "start_states_120minDiv2.csv";
        String esFile = dir + "end_states_120minDiv2.csv";
        mChain.loadMC(mcFile, ssFile, esFile);
        double[][] markovChain = mChain.MC;
        int[] startStates = mChain.startState;
        int[] endStates = mChain.endState;


        PlausibleObfuscationUtilFeedbk obj = new PlausibleObfuscationUtilFeedbk();

        List<int[]> allPaths = PlausibleObfuscationUtilFeedbk.GenAllPath(markovChain, startStates, endStates, markovChain.length);
        int[] sensitiveNodes = new int[]{10};
//        int[] sensitiveNodes = new int[]{10, 39};
//        int[] sensitiveNodes = new int[]{10, 39, 30};
        System.out.println("Total paths: " + allPaths.size());

        int n = 0;
        double minPrior = 1000000;
        double totalPrior = 0;
        int totalSensitivePath = 0;
        int[] pathCount = new int[markovChain.length];
        int[] outputPathCount = new int[markovChain.length];

        for (int[] path : allPaths) {
//            n++;
//            if (n % 100 != 0) continue;

            for (int nd : path) {
                pathCount[nd]++;
//                System.out.print(nd + "->");
            }
            double prior = obj.getPathProbability(path, markovChain) / startStates.length;
            totalPrior += prior;
//            System.out.println("::: prior=" + prior);
            if (obj.isSensitivePath(path, sensitiveNodes)) {
                totalSensitivePath++;
                minPrior = Math.min(minPrior, prior);
            }
//            obj.PlausibleObfuscationAlgorithm(path, 0.4, markovChain, allPaths, sensitiveNodes);
        }
        System.out.println("Total sensitive paths: " + totalSensitivePath);

        double minDelta = 1 / (minPrior * allPaths.size());
        System.out.println("::: MinDelta=" + minDelta);
        System.out.println("::: Total Prior=" + totalPrior);

//        int[] x = new int[]{0, 1, 2, 5, 7, 8, 9};
//
//        for (double delta = 0; delta<=1.5; delta+=0.1) {


        for (double t = 2; t >= 0; t -= 0.1) {
            double delta = minDelta * t;
//        for (double delta = minDelta+1000; delta >=(minDelta-1000); delta -= 50) {
            int totalPathCount = 0;
            int totalUnreleasedPathCount = 0;
            obj.indistingSetSize = new HashMap<>();
            for (int j = 0; j < 1; j++) {
                for (int[] x : allPaths) {
//                    n++;
//                    if (n%100 !=0) continue;

                    int[] x_tilde = obj.PlausibleObfuscationAlgorithm(x, delta, markovChain, allPaths, sensitiveNodes);
                    totalPathCount++;
                    if (x_tilde == null)
                        totalUnreleasedPathCount++;


                }
            }

            System.out.println("t=" + t + ", Delta=" + delta + ", Total Paths=" + totalPathCount + ", Total unreleased paths=" + totalUnreleasedPathCount);

        }
    }

    private static List<Integer> getTop10Nodes(int[] pathCount) {
        List<Integer> top10Node = new ArrayList<>();
        for (int i = 0; i < pathCount.length; i++)
            top10Node.add(getMaxCountNode(pathCount, top10Node));
        return top10Node;
    }

    private static int getMaxCountNode(int[] pathCount, List<Integer> top10Node) {
        int maxIndx = 0;
        int maxCount = -1;
        for (int i = 0; i < pathCount.length; i++)
            if (!top10Node.contains(i) && pathCount[i] > maxCount) {
                maxCount = pathCount[i];
                maxIndx = i;
            }

        return maxIndx;
    }

}
