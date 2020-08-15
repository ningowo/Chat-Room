import java.io.*;
import java.util.ArrayList;

/**
 *
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
public class ChatFilter {
    ArrayList<String> badWordArrayList = new ArrayList<>();
    public static boolean signal = true;

    public ChatFilter(String badWordsFileName) {

        try {
            File f = new File(String.format(".\\src\\%s", badWordsFileName));
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while (true) {
                String s = reader.readLine();
                if (s == null) break;
                badWordArrayList.add(s);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Sorry, the badwords file is not found, please check your file name" +
                    " or the position of the badwords file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (signal) {
            System.out.println("============================");
            System.out.println("The bad words are as below: ");
            for (int i = 0; i < badWordArrayList.size(); i++) {
                System.out.print(badWordArrayList.get(i) + " ");
            }
            System.out.println();
            System.out.println("============================");
            signal = false;
        }
    }

    public String filter(String msg) {
        String filtered = msg;
        for (int i = 0; i < badWordArrayList.size(); i++) {
            //System.out.println(badWordArrayList.get(i));
            filtered = filtered.replace(badWordArrayList.get(i), "**");
        }
        return filtered;
    }
}
