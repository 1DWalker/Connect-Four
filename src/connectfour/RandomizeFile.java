package connectfour;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static connectfour.CreateData.file;

//Reads all lines from input files and creates and output file with all the input lines randomized
public class RandomizeFile {

    public static void main(String[] args) {
        List<String> lines = new ArrayList();
        System.out.println("Reading train_data13.txt");
        lines.addAll(readFile("train_data13.txt"));

        System.out.println("Reading train_data14.txt");
        lines.addAll(readFile("train_data14.txt"));

        System.out.println("Reading train_data15.txt");
        lines.addAll(readFile("train_data15.txt"));

        System.out.println("Reading train_data16.txt");
        lines.addAll(readFile("train_data16.txt"));

        System.out.println("Reading train_data17.txt");
        lines.addAll(readFile("train_data17.txt"));

        System.out.println("Reading train.txt");
        lines.addAll(readFile("train_data.txt"));

        System.out.println("Shuffling lines");
        Collections.shuffle(lines);

        System.out.println("Writing...");
        writeFile(lines, "train_data.txt");
        System.out.println("Complete!");
    }

    public static List<String> readFile(String file) {
        List<String> lines = new ArrayList();
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return lines;
    }

    public static void writeFile(List<String> lines, String file) {
        try(FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            for (String line : lines) {
                out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
