package quizblitz;

import org.json.JSONObject;


public class GameLoop implements Runnable {

	// shared scoreboard and question list passed in from GameServer
	private Scoreboard   scoreboard;
	private QuestionBank questionBank;

	// the question currently being asked — volatile so all threads see the latest value
	private volatile Question currentQuestion;
	private volatile int currentQuestionId;

	// tracks how many players have answered this round
	private int answersThisRound = 0;

	// flipped to true when the host presses Start Game
	private volatile boolean gameStarted = false;

	static final int MIN_PLAYERS           = 2;  // package-accessible so GameServer can use it
	private static final int ROUND_SECONDS = 20;
	private static final int BREAK_SECONDS = 3;

	private GameServer server;

	// constructor
	public GameLoop(Scoreboard scoreboard, QuestionBank questionBank) {
		this.scoreboard   = scoreboard;
		this.questionBank = questionBank;
	}

	// called once during setup so the game loop can send broadcasts
	public void setBroadcaster(GameServer server) {
		this.server = server;
	}

	public int getCurrentQuestionId() {
		return currentQuestionId;
	}

	// called by GameServer when a player submits an answer
	// returns true only if the round is still open and the answer matches
	public boolean checkAnswer(int questionId, String choice) {
		return questionId == currentQuestionId
			&& currentQuestion != null
			&& choice.equals(currentQuestion.getCorrect());
	}

	// called by GameServer when the host presses Start Game
	public void startGame() {
		gameStarted = true;
	}

	// called by GameServer each time a player submits any answer
	public synchronized void recordAnswer(int questionId) {
		if (questionId == currentQuestionId) {
			answersThisRound++;
		}
	}

	private synchronized int getAnswersThisRound() {
		return answersThisRound;
	}

	// helper to sleep, returns false if the thread is interrupted
	private boolean sleep(long ms) {
		try {
			Thread.sleep(ms);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	// main game loop, runs on its own thread, controls all round timing
	@Override
	public void run() {

		// keep broadcasting "waiting" every second until the host presses Start
		while (!gameStarted) {
			server.broadcastToAll(new JSONObject()
				.put("type", "waiting")
				.put("data", new JSONObject()
					.put("count",  scoreboard.playerCount())
					.put("needed", MIN_PLAYERS))
				.toString());
			System.out.println("BROADCAST: waiting");
			if (!sleep(1000)) return;
		}

		// loop through every question in the bank
		for (Question q : questionBank.getAll()) {

			// set the current question so checkAnswer and recordAnswer know what round we're in
			currentQuestion   = q;
			currentQuestionId = q.getId();
			answersThisRound  = 0;

			// send the question to all players
			server.broadcastToAll(new JSONObject()
				.put("type", "question")
				.put("data", new JSONObject(currentQuestion.toJSON()))
				.toString());
			System.out.println("BROADCAST: question " + currentQuestionId);

			// wait up to ROUND_SECONDS, but exit early if everyone has answered
			long deadline = System.currentTimeMillis() + ROUND_SECONDS * 1000L;
			while (System.currentTimeMillis() < deadline
				   && getAnswersThisRound() < scoreboard.playerCount()) {
				if (!sleep(200)) return;
			}

			// set to -1 so any late answers from this round get rejected
			currentQuestionId = -1;

			// send the correct answer and updated scores to everyone
			server.broadcastToAll(new JSONObject()
				.put("type", "round_end")
				.put("data", new JSONObject()
					.put("correct", currentQuestion.getCorrect())
					.put("scores",  new JSONObject(scoreboard.snapshot())))
				.toString());
			System.out.println("BROADCAST: round_end");

			// short break before the next question
			if (!sleep(BREAK_SECONDS * 1000L)) return;
		}

		// all questions done — send final results
		server.broadcastToAll(new JSONObject()
			.put("type", "game_over")
			.put("data", new JSONObject()
				.put("winner",      scoreboard.winner())
				.put("finalScores", new JSONObject(scoreboard.snapshot())))
			.toString());
		System.out.println("BROADCAST: game_over");
	}

	// quick test runs the loop with fake players, no real server needed
	public static void main(String[] args) {
		Scoreboard   scoreboard   = new Scoreboard();
		scoreboard.addPlayer("Bob");
		scoreboard.addPlayer("Alice");
		scoreboard.addPlayer("Charlie");

		QuestionBank questionBank = new QuestionBank();
		GameLoop     gameLoop     = new GameLoop(scoreboard, questionBank);

		new Thread(gameLoop).start();
	}

}
