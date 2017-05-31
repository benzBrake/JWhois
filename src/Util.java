import java.io.*;

public class Util {
    public static String readFile(String fileName){
        String str = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            while (line != null && !line.equals("end")) {
                str += line;
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException fe) {
            str = null;
        } catch (IOException ie) {
            ie.printStackTrace();
            str = null;
        }
        return str;
    }
    public static void writeFile(String fileName,String data){
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(data);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
}
