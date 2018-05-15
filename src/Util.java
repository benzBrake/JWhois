import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JWhois Util
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
    /**
     * @author: Ryan
     * @Description: Convert special characters to HTML entities
     * @param: html
     * @return: String
     * @date: 17:17 2018/5/14
     */
    public static String htmlSpecialChar(String html) {
        return html.replaceAll("&gt;?", "<").replaceAll("&nbsp;?", " ");
    }
    /**
     * @author: Ryan
     * @Description: Check if IPv4 avaliable
     * @param: ip
     * @return: boolean
     * @date: 19:09 2018/5/14
     */
    public static boolean isIPv4(String ip) {
        int count=0;
        Pattern p = Pattern.compile("(^\\d{1,3})(\\.)(\\d{1,3})(\\.)(\\d{1,3})(\\.)(\\d{1,3}$)");
        Matcher m = p.matcher(ip);
        while(m.find()){
            for(int i=1;i<8;i+=2){
                if(Integer.parseInt(m.group(i)) <= 255 && Integer.parseInt(m.group(i)) >= 0){
                    count++;
                }
            }
        }
        if(count==4)
            return true;
        else
            return false;
    }
}
