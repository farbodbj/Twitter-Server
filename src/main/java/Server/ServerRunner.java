package Server;

public class ServerRunner {
    public static void main(String[] args) {
        //here is written the driver code for starting the server
        //and also monitoring server events
        ServerRequestHandler requestHandler = new ServerRequestHandler();
        requestHandler.run();
    }
}
