import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("\n====== SECURE FILE PACKER & UNPACKER ======");
            System.out.println("1. Pack Files");
            System.out.println("2. Unpack Files");
            System.out.print("Enter choice: ");

            int choice = Integer.parseInt(br.readLine().trim());

            if (choice == 1) {
                System.out.print("Enter output pack file name: ");
                String out = br.readLine().trim();

                System.out.print("Enter password: ");
                String password = br.readLine().trim();

                System.out.println("Enter file paths (one per line). Type DONE to finish:");
                List<File> files = new ArrayList<>();

                while (true) {
                    String path = br.readLine().trim();
                    if (path.equalsIgnoreCase("DONE") || path.isEmpty())
                        break;
                    files.add(new File(path));
                }

                FilePacker.packFiles(out, password, files);

            } else if (choice == 2) {

                System.out.print("Enter packed file path: ");
                String packed = br.readLine().trim();

                System.out.print("Enter output folder: ");
                String folder = br.readLine().trim();

                System.out.print("Enter password: ");
                String password = br.readLine().trim();

                FileUnpacker.unpackFiles(packed, folder, password);

            } else {
                System.out.println("Invalid choice.");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
