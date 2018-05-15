public class JDomain {
    private String domain = null;
    private String tld = null;
    private JServer whoisServer = null;
    JDomain() {
        this.domain = "doufu.ru";
        this.tld = "ru";
        this.whoisServer = new JServer("whois.iana.org", 43,  "{domain}", null);
    }
    JDomain(String domain) {
        this.domain = domain;
        this.tld = JWhois.getTldFromDomainString(domain);
        this.whoisServer = JWhois.getWhoisServerByTLD(this.tld);
    }
    public String getDomain() {
        return this.domain;
    }

    public String getTld() {
        return this.tld;
    }

    public JServer getWhoisServer() {
        return this.whoisServer;
    }

    @Override
    public String toString() {
        return getDomain();
    }
}
