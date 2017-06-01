import java.io.*;

/**
 * Whois Util
 *
 * @author Ryan
 * @create 2017-05-30 12:44
 **/

public class Util {
    /*
     * Reads file to String
     *
     * @param fileName the file need to read
     * @return String
     *
     * @author Ryan
     * @date 2017/6/1 10:22
     */
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
    /*
     * Write string to file
     *
     * @param fileName file name
     * @param data the string will be written to the file
     * @return void
     *
     * @author Ryan
     * @date 2017/6/1 10:22
     */
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
    public static String htmlSpecialChar(String html) {
        return html.replaceAll("&gt;?","<").replaceAll("&nbsp;?"," ");
    }
}
