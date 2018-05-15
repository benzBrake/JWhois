import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * JWhois Client
 *
 * @author Ryan
 * @create 2017-05-30 12:44
 **/

public class JWhois {
    private static JSONObject Servers = JSONObject.fromObject(Util.readFile("conf/servers.list"));
    public static void main(String[] args) {

    }
    /**
     * @author: Ryan
     * @Description:
     * @param:
     * @return:
     * @date: 20:37 2018/5/14
     */
    public static JResult whois(JDomain JDomain) {
        JServer js = JWhois.getWhoisServer(JDomain);
        if (js == null) {
            return new JResult("DO NOT SUPPORT THIS DOMAIN");
        }
        if (js.getType() == JServer.WHOIS) {
            String result = JWhois.OnceTCP(js.getServer(), js.getPort(), js.getParameter().replaceAll("\\{domain\\}", JDomain.toString()));
            return new JResult(result);
        } else if (js.getType() == JServer.WEB_WHOIS) {
            return JWhois.webWhois(JDomain);
        }
        return null;
    }
    public static JResult webWhois(JDomain JDomain) {
        JServer js = JDomain.getWhoisServer();
        if (js.getType() > 0) {
            String reqUrl = js.getServer();
            String parameter = js.getParameter();
            Boolean getFlag = false;
            if (reqUrl.contains("{domain}")) {
                getFlag = true;
                reqUrl = reqUrl.replaceAll("\\{domain\\}", JDomain.toString());
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
                    String urlParameters = parameter.replaceAll("\\{domain\\}", JDomain.getDomain());
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
                JSONObject regex = js.getRegex();
                if (regex != null) {
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
                return new JResult(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return new JResult();
            }
        }
        return new JResult("DO NOT SUPPORT THIS DOMAIN");
    }
    public static String getTldFromDomainString(String domain) {
        domain = domain.replaceAll("https?://","").replaceAll("/.*","");
        String[] _domain = domain.split("\\.");
        if (_domain.length > 1) {
            return _domain[_domain.length - 1];
        }
        return null;
    }
    /**
     * @author: Ryan
     * @Description:
     * @param: 
     * @return: 
     * @date: 20:23 2018/5/14
     */
    public static JServer getWhoisServer(JDomain JDomain) {
        return JWhois.getWhoisServerByTLD(JDomain.getTld());
    }
    /**
     * @author: Ryan
     * @Description:
     * @param: 
     * @return: 
     * @date: 20:23 2018/5/14
     */
    public static JServer getWhoisServerByTLD(String tld) {
        if (tld == null || tld.equals("null")) {
            return null;
        }
        if (!Servers.containsKey(tld)) {
            System.out.println("getWhoisServerByTLD: ." + tld + " is not found in servers.list");
            return getWhoisServerFromIANAByTld(tld);
        }
        Object serverJSON = Servers.get(tld.toString());
        JSONObject JConfig = JSONObject.fromObject(serverJSON);
        String server = JConfig.getString("server");
        int type = JServer.WHOIS;
        if (server.startsWith("http"))
            type = JServer.WEB_WHOIS;
        else if (server.equals("null"))
            type = JServer.NONE_WHOIS;
        JSONObject regex = null;
        if (JConfig.containsKey("regex"))
            regex = JConfig.getJSONObject("regex");
        JServer js = new JServer(server, 43, JConfig.getString("parameter"), regex);
        return js;
    }
    /**
     * @author: Ryan
     * @Description: Get Whois Server From IANA by JDomain
     * @param: JDomain
     * @return: JServer
     * @date: 20:22 2018/5/14
     */
    public static JServer getWhoisServerFromIANAByTld(JDomain JDomain) {
        return JWhois.getWhoisServerFromIANAByTld(JDomain.toString());
    }
    /**
     * @author: Ryan
     * @Description:
     * @param: 
     * @return: 
     * @date: 20:23 2018/5/14
     */
    public static JServer getWhoisServerFromIANAByTld(String str) {
        try {
            URL url = new URL("https://www.iana.org/whois?q=" + str);
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
                String server = line.replaceAll("^[^\\s]*\\s+", "").trim();
                JSONObject newServer = new JSONObject();
                newServer.put("server", server);
                newServer.put("parameter", "{domain}");
                synchronized (Servers) {
                    Servers.put(str, newServer);
                    Util.writeFile("conf/servers.list",Servers.toString(4));
                }
                return new JServer(server, 43, "{domain}", null);
            }
            return new JServer(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JServer(null);
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
    private static String OnceTCP(String host, int port, String parameter) {
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
        return Util.htmlSpecialChar(Result);
    }



}