//system test, reinstalled java
import java.io.*;
import java.net.*;


//quick testtt

public class tester{
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 4554);

        BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

    //read handshake
    System.out.println(in.readLine());
    System.out.println(in.readLine());
    System.out.println(in.readLine());

    //testing out a few commands
    out.println("POST 1 1 white hello");
    System.out.println(in.readLine());

    out.println("PIN 1 1");
    System.out.println(in.readLine());

    out.println("SHAKE");
    System.out.println(in.readLine());

    out.println("DISCONNECT");
    socket.close();
    }
}