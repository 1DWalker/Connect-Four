package connectfour;

import org.apache.commons.math3.special.Erf;

public class Statistics {
    private int wins, losses, draws, Xwins, Xlosses, Xdraws, current, size, totalGameLength;

    public Statistics(int size) {
        wins = draws = losses = Xwins = Xdraws = Xlosses = current = 0;
        this.size = size;

//        wins = 635;
//        losses = 560;
//        draws = 47;
//        current = wins + losses + draws;
//        Xwins = 483;
//        Xlosses = 712;
//        Xdraws = draws;
//        totalGameLength = (int) (36.57890499194847 * current);
//        this.size = 40000;
    }

    public void addStatistic(double result, boolean switchSides, int gameLength) {
        if (result == 1) {
            wins++;
            if (switchSides) Xlosses++;
            else Xwins++;
        } else if (result == 0.5) {
            draws++;
            Xdraws++;
        } else {
            losses++;
            if (switchSides) Xwins++;
            else Xlosses++;
        }
        current++;
        totalGameLength += gameLength;
    }

    public void changeSize(int size) {
        this.size = size;
    }

    public void print(int gameLength) {
        System.out.println("W-L-D " + wins + "-" + losses + "-" + draws + ". " + current + " out of " + size + " games completed.");
        //Information on elo can be found here: https://en.wikipedia.org/wiki/Elo_rating_system , https://en.wikipedia.org/wiki/Chess_rating_system
        System.out.print("Elo difference: " + elo((wins + 0.5 * draws) / current));
        System.out.print(" +" + (double) Math.round(1000 * (elo((wins + 0.5 * draws) / current + 1.644853 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000);
        System.out.print(" " + (double) Math.round(1000 * (elo((wins + 0.5 * draws) / current - 1.644853 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000);
        System.out.println(" (90%)");

        //https://chessprogramming.wikispaces.com/Match+Statistics
        System.out.println("LOS: " + (0.5 + 0.5 * Erf.erf((wins - losses) / Math.sqrt(2.0 * (wins + losses)))));
        System.out.println("Game length: " + gameLength);
        System.out.println("Average game length: " + (double) totalGameLength / current);
        System.out.println("(X Perspective) W-L-D " + Xwins + "-" + Xlosses + "-" + Xdraws);
        System.out.print("    Elo difference: " + elo((Xwins + 0.5 * Xdraws) / current));
        System.out.print(" +" + (double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current + 1.644853 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000);
        System.out.print(" " + (double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current - 1.644853 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000);
        System.out.println(" (90%)");

        //https://chessprogramming.wikispaces.com/Match+Statistics
        System.out.println("    LOS: " + (0.5 + 0.5 * Erf.erf((Xwins - Xlosses) / Math.sqrt(2.0 * (Xwins + Xlosses)))));
    }

    public void print() {
        System.out.println("W-L-D " + wins + "-" + losses + "-" + draws + ". " + current + " out of " + size + " games completed.");
        //Information on elo can be found here: https://en.wikipedia.org/wiki/Elo_rating_system , https://en.wikipedia.org/wiki/Chess_rating_system
        System.out.print("Elo difference: " + elo((wins + 0.5 * draws) / current));
        System.out.print(" +" + (double) Math.round(1000 * (elo((wins + 0.5 * draws) / current + 1.644853 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000);
        System.out.print(" " + (double) Math.round(1000 * (elo((wins + 0.5 * draws) / current - 1.644853 * standardErrorOfMeanScore()) - elo((wins + 0.5 * draws) / current))) / 1000);
        System.out.println(" (90%)");

        //https://chessprogramming.wikispaces.com/Match+Statistics
        System.out.println("LOS: " + (0.5 + 0.5 * Erf.erf((wins - losses) / Math.sqrt(2.0 * (wins + losses)))));
        System.out.println("Average game length: " + (double) totalGameLength / current);
        System.out.print("    Elo difference: " + elo((Xwins + 0.5 * Xdraws) / current));
        System.out.print(" +" + (double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current + 1.644853 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000);
        System.out.print(" " + (double) Math.round(1000 * (elo((Xwins + 0.5 * Xdraws) / current - 1.644853 * standardErrorOfMeanScore()) - elo((Xwins + 0.5 * Xdraws) / current))) / 1000);
        System.out.println(" (90%)");

        //https://chessprogramming.wikispaces.com/Match+Statistics
        System.out.println("    LOS: " + (0.5 + 0.5 * Erf.erf((Xwins - Xlosses) / Math.sqrt(2.0 * (Xwins + Xlosses)))));
    }

    public double elo(double mean) {
        return -400.0 * Math.log(1.0 / mean - 1) / Math.log(10.0);
    }

    public double standardErrorOfMeanScore() {
        double mean = (wins + 0.5 * draws) / current;
        double standardDeviation = Math.sqrt((wins * Math.pow(1 - mean, 2) + draws * Math.pow(0.5 - mean, 2) + losses * Math.pow(mean, 2)) / (current + 1));
        return standardDeviation / Math.sqrt(current);
    }

    public double XstandardErrorOfMeanScore() {
        double mean = (Xwins + 0.5 * Xdraws) / current;
        double standardDeviation = Math.sqrt((Xwins * Math.pow(1 - mean, 2) + Xdraws * Math.pow(0.5 - mean, 2) + Xlosses * Math.pow(mean, 2)) / (current + 1));
        return standardDeviation / Math.sqrt(current);
    }
}
