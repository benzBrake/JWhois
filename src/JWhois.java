import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * Whois Client
 *
 * @author Ryan
 * @create 2017-05-30 12:44
 **/

public class JWhois {
    private static JSONObject Servers = JSONObject.fromObject(Util.readFile("conf/servers.list"));
    public static void main(String[] args) throws Exception {
        System.out.println(JWhois.whois(args[0]));
    }
    public static String whois(String url) {
        if (url == null) {
            return "Domain Not Exists!";
        }
        url = url.replaceAll("https?://","").replaceAll("/.*","");
        String tld = getTLD(url);
        if (!Servers.containsKey(tld)) {
            System.out.println("Not Found in servers.list");
            String server = getWhoisServerFromIANA(tld);
            if (server == null || server.equals("DO NOT SUPPORT THIS DOMAIN!"))
                return "DO NOT SUPPORT THIS DOMAIN!";
            JSONObject newServer = new JSONObject();
            newServer.put("server",server);
            newServer.put("parameter","{domain}");
            if (tld !=null) {
                Servers.put(tld,newServer);
                Util.writeFile("conf/servers.list",Servers.toString());
            }
        }
        Object WConfig = Servers.get(tld);
        JSONObject JConfig = JSONObject.fromObject(WConfig);
        String server = JConfig.get("server").toString();
        if (server.startsWith("http"))
            return webWhois(url);
        String parameter = JConfig.get("parameter").toString().replaceAll("\\{domain\\}",url);
        return OnceTCP(server,43,parameter);
    }
    /*
     * Use web interface to whois a domain
     *
     * @param domain The domain name need to whois
     * @return String Domain whois record
     *
     * @author Ryan
     * @date 2017/6/1 10:18
     */
    private static String webWhois(String domain) {
        String tld = getTLD(domain);
        if (Servers != null) {
            if (Servers.containsKey(tld)) {
                JSONObject serverConfig = JSONObject.fromObject(Servers.get(tld));
                String reqUrl = serverConfig.getString("server");
                String parameter = serverConfig.getString("parameter");
                Boolean getFlag = false;
                if (reqUrl.contains("{domain}")) {
                    getFlag = true;
                    reqUrl = reqUrl.replaceAll("\\{domain\\}", domain);
                }
                try {
                    URL url = new URL(reqUrl);
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    if (reqUrl.startsWith("https")) {
                        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                    }
                    if (!getFlag) {
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        String urlParameters = parameter.replaceAll("\\{domain\\}",domain);
                        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();
                    }
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
                                String _result = "";
                                result = new StringBuilder();
                                if (regex.containsKey("group")) {
                                    _result = m.group(regex.getInt("group"));
                                } else {
                                    _result = m.group();
                                }
                                if (regex.containsKey("format")) {
                                    JSONObject _format = JSONObject.fromObject(regex.get("format"));
                                    Iterator<JSONObject.Entry<String,String>> it = _format.entrySet().iterator();
                                    while(it.hasNext()) {
                                        JSONObject.Entry<String,String> _entry = it.next();
                                        _result = _result.replaceAll(_entry.getKey(),_entry.getValue());
                                    }
                                }
                                result.append(_result);
                            }
                        }
                    }
                    return Util.htmlSpecialChar(result.toString());
                } catch (MalformedURLException mue) {
                    mue.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return null;
            }
        }
        return null;
    }
    /*
     * Auto querying whois server from iana.org and save to config.
     * 
     * @param tld
     * @return String whois server
     * 
     * @author Ryan
     * @date 2017/5/31 10:20
     */
    public static String getWhoisServerFromIANA(String tld) {
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
            if (line !=null) {
                return line.replaceAll("^[^\\s]*\\s+","").trim();
            }
            return line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DO NOT SUPPORT THIS DOMAIN!";
    }
    /*
     * Connect to Whois server through custom port
     *
     * @param host
     * @param port
     * @param parameter Once connected to the server, the parameter will send to the server before disconect.
     * @return String the output of remote server
     *
     * @author Ryan
     * @date 2017/5/31 10:20
     */
    private static String OnceTCP(String host,int port,String parameter) {
        String Result = "";
        try {
            Socket socket = new Socket(host, port);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
    /*
     * Split tld from a domain name
     *
     * @param domain
     * @return String
     *
     * @author Ryan
     * @date 2017/6/1 10:21
     */
    public static String getTLD(String domain) {
        String[] _domain = domain.split("\\.");
        if (_domain.length > 1) {
            return _domain[_domain.length - 1];
        }
        return null;
    }
}