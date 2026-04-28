package quizblitz;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/*
 * Phase 1 skeleton: an echo server.
 *
 * Listens on port 8080 for WebSocket connections. Whatever a client sends,
 * it sends right back. This is just to prove the connection works end-to-end
 * before we layer on the real game logic.
 *
 * The Java-WebSocket library handles the low-level socket details for us.
 * Each client connection runs on its own thread internally (managed by the
 * library) -- same idea as ClientHandler in the Mailbox assignment, but the
 * library does the thread bookkeeping.
 */
public class GameServer extends WebSocketServer {

    private static final int PORT = 8080;

    public GameServer(InetSocketAddress address) {
        super(address);
    }

    // Called when a new client connects
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
        conn.send("Connected to QuizBlitz server. Send a message and I'll echo it back.");
    }

    // Called when a client disconnects
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress() + " (" + reason + ")");
    }

    // Called when a client sends a message
    // Phase 1: just echo it back. Phase 3 will replace this with JSON parsing.
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received from " + conn.getRemoteSocketAddress() + ": " + message);
        conn.send("Echo: " + message);
    }

    // Called when something goes wrong
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
    // Called once the server is fully up and listening
    @Override
    public void onStart() {
        System.out.println("Server started on port " + PORT);
        System.out.println("Connect from a browser at: ws://localhost:" + PORT);
    }
    
    // Temporary web server broadcast function
    public void broadcastToAll(String message) {
        for (WebSocket conn : getConnections()) {
            conn.send(message);
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(new InetSocketAddress(PORT));
        server.start();
    }
}
