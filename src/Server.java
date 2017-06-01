import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Whois Server
 *
 * @author Ryan
 * @create 2017-05-30 12:44
 **/

public class Server {
    private ServerSocket ss;
    Server() {
        try {
            ss = new ServerSocket(43);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    public ServerSocket getServer() {
        return ss;
    }
    public static void main(String[] args) {
        ServerSocket server = new Server().getServer();
        while(true) {
            try {
                Socket client = server.accept();
                new SocketThread(client).start();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }
}

/**
 * Socket Thread
 *
 * @author Ryan
 * @create 2017-05-31 10:44
 **/

class SocketThread extends Thread {
    private Socket sock = null;
    SocketThread (Socket s) {
        sock = s;
    }
    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter pw = new PrintWriter(sock.getOutputStream());
            String line = br.readLine();
            if (line !=null) {
                pw.write(JWhois.whois(line));
                pw.flush();
                pw.close();
                sock.close();
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
}
