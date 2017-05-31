import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class JWhois {
    public static JSONObject Servers = JSONObject.fromObject(Util.readFile("conf/servers.list"));
    public static void main(String[] args) throws Exception {
        System.out.println(new JWhois().getWhoisServerFromIANA("t.tt"));
    }
    JWhois()
    {
//        System.setProperty("http.proxySet", "true");
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "1080");
    }
    public static String whois(String url) {
        if (url == null) {
            return "Domain Not Exists!";
        }
        url = url.replaceAll("https?://","").replaceAll("/.*","");
        String tld = getTLD(url);
        if (!Servers.containsKey(tld)) {
            System.out.println("No Found in servers.list");
            String server = getWhoisServerFromIANA(tld);
            if (server.equals("DO NOT SUPPORT THIS DOMAIN!"))
                return "DO NOT SUPPORT THIS DOMAIN!";
            JSONObject newServer = new JSONObject();
            newServer.put("server",server);
            newServer.put("parameter","{domain}");
            Servers.put(tld,newServer);
            Util.writeFile("conf/servers.list",Servers.toString());
        }
        Object WConfig = Servers.get(tld);
        JSONObject JConfig = JSONObject.fromObject(WConfig);
        String server = JConfig.get("server").toString();
        if (server.startsWith("http"))
            return webWhois(url);
        String parameter = JConfig.get("parameter").toString().replaceAll("\\{domain\\}",url);
        return OnceTCP(server,43,parameter);
    }
    public static String webWhois(String domain) {
        String tld = getTLD(domain);
        if (Servers != null) {
            if (Servers.containsKey(tld)) {
                JSONObject serverConfig = JSONObject.fromObject(Servers.get(tld));
                String reqUrl = serverConfig.getString("server");
                String parameter = serverConfig.getString("parameter");
                if (reqUrl.contains("{domain}")) {
                    //METHOD:GET
                    reqUrl.replaceAll("\\{domain\\}",domain);
                    try {
                        URL url = new URL(reqUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line = in.readLine();
                        while(line != null && !line.equals("end")) {
                            result.append(line);
                            line = in.readLine();
                        }
                        return result.toString();
                    } catch (MalformedURLException mue) {
                        mue.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                } else {
                    //METHOD:POST
                    try {
                        URL url = new URL(reqUrl);
                        String urlParameters = parameter.replaceAll("\\{domain\\}",domain);
                        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                        if (reqUrl.startsWith("https")) {
                            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                        }
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line = in.readLine();
                        while(line != null && !line.equals("end")) {
                            result.append(line);
                            line = in.readLine();
                        }
                        if (serverConfig.containsKey("regex")) {
                            JSONObject regex = JSONObject.fromObject(serverConfig.get("regex"));
                            if (regex.containsKey("data")) {
                                String pattern = regex.get("data").toString();
                                Pattern r = Pattern.compile(pattern);
                                Matcher m = r.matcher(result);
                                if (m.find()) {
                                    result = new StringBuilder();
                                    result.append(m.group());
                                }
                            }
                        }
                        return result.toString();
                    } catch (MalformedURLException mue) {
                        mue.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return "";
    }
    public static String getWhoisServerFromIANA(String tld) {
        // Auto querying whois server from iana.org and save to config.
        try {
            URL url = new URL("https://www.iana.org/whois?q=" + tld);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = br.readLine();
            while(line !=null && !line.equals("end")) {
                if (line.contains("whois:")) {
                    break;
                }
                line = br.readLine();
            }
            return line.replaceAll("^[^\\s]*\\s+","");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DO NOT SUPPORT THIS DOMAIN!";
    }
    private static String OnceTCP(String host,Integer port,String parameter) {
        // Connect Whois server through port 43
        String Result = "";
        try {
            Socket socket = new Socket(host, port);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));;
            PrintWriter write = new PrintWriter(socket.getOutputStream());
            write.write(parameter + "\r\n");
            write.flush();
            String readline;
            readline = br.readLine();
            while (readline!= null && !readline.equals("end")) {
                Result += readline + "\r\n";
                readline = br.readLine();
            }
            write.close();
            socket.close();
        } catch (Exception e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement ste:stackTrace) {
                Result += ste.toString();
            }
        }
        return Result;
    }
    public static String getTLD(String domain) {
        String[] _domain = domain.split("\\.");
        if (_domain.length > 1) {
            return _domain[_domain.length - 1];
        }
        return null;
    }
}