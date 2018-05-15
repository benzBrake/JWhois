import net.sf.json.JSONObject;

public class JServer {
    private String server;
    private int port;
    private int type;
    private String parameter;
    private JSONObject regex;
    public static int WHOIS = 1;
    public static int WEB_WHOIS = 2;
    public static int NONE_WHOIS = 0;
    JServer() {
        this.server = "whois.iana.org";
        this.type = this.WHOIS;
        this.parameter = "{domain}";
        this.port = 43;
    }
    JServer(JDomain JDomain) {
        String domainStr = JDomain.toString();
        this.type = JServer.WHOIS;
        if (domainStr == null || domainStr.equals("")) {
            this.server = null;
            this.type = JServer.NONE_WHOIS;
        }
        // Get whois server
        this.server = JWhois.getWhoisServer(JDomain).toString();
        this.port = 43;
    }
    JServer(String server, int port, String parameter, JSONObject regex) {
        this.server = server;
        this.type = JServer.WHOIS;
        if (server.startsWith("http"))
            this.type = JServer.WEB_WHOIS;
        if (server.equals("null") || server == null)
            this.type = JServer.NONE_WHOIS;
        this.parameter = parameter;
        this.port = port;
        this.regex = regex;
    }
    public String getServer() {
        return this.server;
    }
    public int getPort() { return port; }
    public int getType() {
        return this.type;
    }
    public String getParameter() { return parameter; }
    public JSONObject getRegex() { return regex; }
    public void setPort(int port) { this.port = port; }

    @Override
    public String toString() {
        return getServer();
    }
}