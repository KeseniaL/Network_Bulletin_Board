import java.net.*;
import java.io.*;

/*Setting up Thread-per-client. Responsible for the following:
    - Establish one thread per client
    - Each client will have their own input/output streams
    - only one response per request
*/

// extend Thread class for Clienthnadler
public class ClientHandler extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket){
        this.socket = socket;
    }


    @Override
    public void run(){
        try {
            in = new BufferedReader (new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            sendHandshake(); //establishing handshake

            //Dealing with client requests, server here will read request as long as its not empty and generate a response
            String line;
            while ((line = in.readLine()) != null){
                String response = ProtocolParser.handle(line);
                out.println(response);

                //client has completed their request and wishes to disconnect
                if(line.equals("DISCONNECT")){
                    break;
                }
            }

        } catch (Exception e) {
            //If client abruptly/unexpectedly disconnects
        } finally {
            try {socket.close();} catch (IOException ignored) {}
        }
    }

    private void sendHandshake(){
        out.println("BOARD " + BBoard.BOARD_WIDTH + " " + BBoard.BOARD_HEIGHT);
        out.println("NOTE " + BBoard.NOTE_WIDTH + " " + BBoard.NOTE_HEIGHT);
        out.println("COLOURS " + String.join(" ", BBoard.VALID_COLOURS));
    }
}