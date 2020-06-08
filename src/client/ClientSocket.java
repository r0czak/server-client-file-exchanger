package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;

public class ClientSocket {

    /*-------------------------------------------------*/
    // Socket client application

    private Socket clientSocket;
    public PrintWriter out;
    public BufferedReader in;

    public void startConnection(String host_name, int port) {
        try {
            clientSocket = new Socket(host_name, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String sendString(String msg) {
        try {
            out.println(msg);
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static boolean CheckTimeRange(String TimeRange) {
        LocalTime TimeRangeTime = LocalTime.parse(TimeRange);
        LocalTime presentTime = LocalTime.now();

        return true;
    }

}
