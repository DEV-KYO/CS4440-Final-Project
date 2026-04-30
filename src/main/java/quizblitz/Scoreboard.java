package quizblitz;

import java.util.HashMap;
import java.util.Map;

public class Scoreboard {

    private final Map<String, Integer> scores = new HashMap<>();

    // Add player if not exists
    public synchronized void addPlayer(String name) {
        scores.putIfAbsent(name, 0);
    }

    // Add points to a player
    public synchronized void addPoints(String name, int points) {
        int current = scores.getOrDefault(name, 0);
        scores.put(name, current + points);
    }

    // Get one player's score
    public synchronized int getScore(String name) {
        return scores.getOrDefault(name, 0);
    }

    // Number of players
    public synchronized int playerCount() {
        return scores.size();
    }

    // Return a copy of all scores
    public synchronized Map<String, Integer> snapshot() {
        return new HashMap<>(scores);
    }

    // Find the winner (highest score)
    public synchronized String winner() {
        String topPlayer = null;
        int maxScore = -1;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                topPlayer = entry.getKey();
            }
        }

        if (topPlayer == null) {
            return "No players yet";
        }

        return topPlayer + " (" + maxScore + " points)";
    }
    
    public static void main(String[] args) throws InterruptedException {
        Scoreboard sb = new Scoreboard();

        // Step 1: Add 4 players
        sb.addPlayer("Alice");
        sb.addPlayer("Bob");
        sb.addPlayer("Charlie");
        sb.addPlayer("Diana");

        // Step 2: Create threads
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sb.addPoints("Alice", 10);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sb.addPoints("Bob", 10);
            }
        });

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sb.addPoints("Charlie", 10);
            }
        });

        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sb.addPoints("Diana", 10);
            }
        });

        // Step 3: Start threads
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        // Step 4: Wait for all to finish
        t1.join();
        t2.join();
        t3.join();
        t4.join();

        // Step 5: Print results
        System.out.println("Final Scores: " + sb.snapshot());

        // Step 6: Verify total
        int total = 0;
        for (int score : sb.snapshot().values()) {
            total += score;
        }

        System.out.println("Total score = " + total);
    }
}
