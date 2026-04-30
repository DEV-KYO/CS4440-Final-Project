package quizblitz;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// GameServer handles all WebSocket connections from players and the host display.
// It parses incoming JSON messages and routes them to the scoreboard or game loop.
// It also runs a small HTTP server on port 8081 so players can load the game in a browser.
public class GameServer extends WebSocketServer {

    private static final int WS_PORT           = 8080;
    private static final int HTTP_PORT          = 8081;
    private static final int POINTS_PER_CORRECT = 10;

    // single shared scoreboard — both GameServer and GameLoop reference the same object
    private final Scoreboard scoreboard = new Scoreboard();
    private final GameLoop   gameLoop;

    // maps each open connection to the player's name
    // ConcurrentHashMap because onMessage and onClose can run on different threads at the same time
    private final Map<WebSocket, String> playerNames = new ConcurrentHashMap<>();

    // LAN IP detected at startup, used to build the game URL sent to clients
    private String localIp = "localhost";

    // constructor — creates the game loop and wires everything together
    public GameServer(InetSocketAddress address) {
        super(address);
        setReuseAddr(true); // lets the port rebind quickly if the server crashed
        this.gameLoop = new GameLoop(scoreboard, new QuestionBank());
        this.gameLoop.setBroadcaster(this);
    }

    // ── WebSocket callbacks ───────────────────────────────────────────────────
    // These are called automatically by the WebSocket library — one per event.

    // fired when a new client connects — send them the game URL and current player list
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
        conn.send(buildMessage("server_info", new JSONObject()
                .put("gameUrl", "http://" + localIp + ":" + HTTP_PORT + "/")));
        conn.send(buildPlayerListJson()); // catches up any display page that joined late
    }

    // fired when a client disconnects — remove them and update everyone's player list
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String name = playerNames.remove(conn);
        if (name != null) {
            scoreboard.removePlayer(name);
            System.out.println("Player disconnected: " + name);
            broadcastToAll(buildPlayerListJson());
        } else {
            System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
        }
    }

    // fired when any client sends a message — parse the JSON and route by type
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received from " + conn.getRemoteSocketAddress() + ": " + message);
        try {
            JSONObject json = new JSONObject(message);
            String     type = json.getString("type");
            JSONObject data = json.optJSONObject("data");

            switch (type) {
                case "join":       handleJoin(conn, data);   break;
                case "answer":     handleAnswer(conn, data); break;
                case "start_game": handleStartGame(conn);    break;
                default: sendError(conn, "Unknown message type: " + type);
            }
        } catch (JSONException e) {
            sendError(conn, "Invalid JSON. Expected: {\"type\":\"...\",\"data\":{...}}");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }

    // fired once when the server is ready — detect IP, start HTTP server, start game loop thread
    @Override
    public void onStart() {
        this.localIp = getLocalIp();
        System.out.println("─────────────────────────────────────────");
        System.out.println("  QuizBlitz server running!");
        System.out.println("  Player page:  http://" + localIp + ":" + HTTP_PORT + "/");
        System.out.println("  Display page: http://" + localIp + ":" + HTTP_PORT + "/display.html");
        System.out.println("─────────────────────────────────────────");
        startHttpServer();
        new Thread(gameLoop).start();
    }

    // ── Message handlers ──────────────────────────────────────────────────────

    // player wants to join — validate their name, register them, and tell everyone
    private void handleJoin(WebSocket conn, JSONObject data) {
        if (data == null || !data.has("name")) {
            sendError(conn, "join requires: {\"name\":\"...\"}");
            return;
        }

        String name = data.getString("name").trim();
        if (name.isEmpty()) {
            sendError(conn, "Name cannot be empty");
            return;
        }

        playerNames.put(conn, name);
        scoreboard.addPlayer(name);
        System.out.println("Player joined: " + name + " (total: " + scoreboard.playerCount() + ")");

        conn.send(buildMessage("welcome", new JSONObject().put("name", name)));
        broadcastToAll(buildPlayerListJson()); // update player list on all screens
    }

    // player submitted an answer — check if it's correct, update score, notify the game loop
    private void handleAnswer(WebSocket conn, JSONObject data) {
        String playerName = playerNames.get(conn);
        if (playerName == null) {
            sendError(conn, "You must join before answering");
            return;
        }

        if (data == null || !data.has("questionId") || !data.has("choice")) {
            sendError(conn, "answer requires: {\"questionId\":1, \"choice\":\"A\"}");
            return;
        }

        int    questionId = data.getInt("questionId");
        String choice     = data.getString("choice").trim().toUpperCase();

        boolean correct = gameLoop.checkAnswer(questionId, choice);
        if (correct) {
            scoreboard.addPoints(playerName, POINTS_PER_CORRECT);
            System.out.println(playerName + " answered Q" + questionId + " correctly.");
        } else {
            System.out.println(playerName + " answered Q" + questionId + " incorrectly.");
        }

        gameLoop.recordAnswer(questionId); // lets the game loop end the round early if everyone answered
        conn.send(buildMessage("answer_result", new JSONObject()
                .put("correct", correct)
                .put("score",   scoreboard.getScore(playerName))));
    }

    // host pressed Start Game — check player count then release the game loop
    private void handleStartGame(WebSocket conn) {
        int count = scoreboard.playerCount();
        if (count < GameLoop.MIN_PLAYERS) {
            sendError(conn, "Need at least " + GameLoop.MIN_PLAYERS + " players to start. Currently: " + count);
            return;
        }
        gameLoop.startGame();
        System.out.println("Game started by host with " + count + " players.");
        broadcastToAll(buildMessage("game_starting", new JSONObject().put("playerCount", count)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // builds the player_list message from whoever is currently connected
    private String buildPlayerListJson() {
        List<String> names = new ArrayList<>(playerNames.values());
        return new JSONObject()
                .put("type", "player_list")
                .put("data", new JSONObject()
                        .put("players", new JSONArray(names))
                        .put("count",   names.size()))
                .toString();
    }

    // wraps any payload into the standard {"type":..., "data":...} format
    private static String buildMessage(String type, JSONObject data) {
        return new JSONObject().put("type", type).put("data", data).toString();
    }

    // sends an error message back to one specific client
    private void sendError(WebSocket conn, String reason) {
        conn.send(new JSONObject().put("type", "error").put("message", reason).toString());
    }

    // sends a message to every connected client — used by GameLoop for broadcasts
    public void broadcastToAll(String message) {
        for (WebSocket conn : getConnections()) {
            conn.send(message);
        }
    }

    // ── HTTP server (serves index.html and display.html on port 8081) ─────────

    // starts a basic HTTP server so players can load the game from a URL instead of a file path
    private void startHttpServer() {
        try {
            HttpServer http = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
            http.createContext("/", this::serveFile);
            http.setExecutor(null);
            http.start();
        } catch (Exception e) {
            System.err.println("HTTP server failed to start: " + e.getMessage());
        }
    }

    // maps URL paths to files bundled inside the jar, then sends the file back
    private void serveFile(HttpExchange exchange) throws IOException {
        String path     = exchange.getRequestURI().getPath();
        String resource = switch (path) {
            case "/", "/index.html" -> "/web/index.html";
            case "/display.html"    -> "/web/display.html";
            default                 -> null;
        };

        if (resource == null) {
            send404(exchange);
            return;
        }

        try (InputStream is = getClass().getResourceAsStream(resource)) {
            if (is == null) {
                send404(exchange);
                return;
            }
            byte[] body = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }

    private void send404(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    // loops through network interfaces to find the machine's local IP address
    private String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) {}
        return "localhost";
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        GameServer server = new GameServer(new InetSocketAddress(WS_PORT));
        server.start();
    }
}
