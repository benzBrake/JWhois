import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket ss;
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
class SocketThread extends Thread {
    private Socket sock = null;
    public SocketThread (Socket s) {
        sock = s;
    }
    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter pw = new PrintWriter(sock.getOutputStream());
            String line = br.readLine();
            if (line !=null) {
                System.out.println("Querying " + line);
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
