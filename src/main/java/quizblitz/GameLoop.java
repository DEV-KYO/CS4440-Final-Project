package quizblitz;

import java.util.List;
import java.util.ArrayList;


public class GameLoop implements Runnable {
	
	// A variable to track player count & individual scores 
	private Scoreboard scoreboard;
	
	// A variable of holding all quiz questions
	private QuestionBank questionBank;
	
	// A variable to current
	private Question currentQuestion;
	private volatile int currentQuestionId;
	
	private static int MIN_PLAYERS = 2;
	private static int ROUND_SECONDS = 20;
	private static int BREAK_SECONDS = 3;
	
	private GameServer server;
	
	// Temporary class for scoreboard file
	public static class Scoreboard {
		
		private int count = 0;
		
		public void addPlayer(String name) {
			count++;
		}
		
		public int playerCount() {
			return count;
		}

		public String snapshot() {
			return "{}";
		}

		public String winner() {
			return "Alice";
		}
	}
	
	// Temporary class for question file
	public static class Question {
		private int id;
		private String correct;

		public Question(int id, String correct) {
			this.id = id;
		    this.correct = correct;
		}

		public int getId() {
			return id; 
		}
		public String getCorrect() {
			return correct;
		}
	}
		
	// Temporary file for questionBank file
	public static class QuestionBank {
		public List<Question> getAll() {
			List<Question> questions = new ArrayList<>();
		    questions.add(new Question(1, "A"));
		    questions.add(new Question(2, "B"));
		    questions.add(new Question(3, "C"));
		    return questions;
		}
	}
	
	// GameLoop Constructor
	public GameLoop(Scoreboard scoreboard, QuestionBank questionBank) {
		this.scoreboard = scoreboard;
		this.questionBank = questionBank;
	}
	
	// A function to 
	public void setBroadcaster(GameServer server) {
		this.server = server;
	}
	
	// A function to return the current active question ID
	// Volatile read - always returns the most up-to-date value
	public int getCurrentQuestionId() {
		return currentQuestionId;
	}
	
	// A function to be called by GameServer.onMessages when a player submits an answer
	public boolean checkAnswer(int questionId, String choice) {
		
		// if statement to determine if the round is still going on and the quiz question still exists
		if(questionId == currentQuestionId && currentQuestion != null) {
			
			// If/else statements to determine if the answer matches the correct quiz answer
			if(choice.equals(currentQuestion.getCorrect())) {
				return true; // correct answer = true
			} else {
				return false; // wrong answer = false
			}
		} else {
			return false; // The round is already over or there isn't anymore quiz questions
		}
		
	}
	
	// The main game loop running function where all the threading will start
	@Override
	public void run() {
		
		// Step 1: Wait until enough players have joined
		while(scoreboard.playerCount() < MIN_PLAYERS) {
			// server.broadcastToAll("{\"type\":\"waiting\"}");
			System.out.println("BROADCAST: waiting");
			
            try { 
            	Thread.sleep(1000); // check again every second
            } 
            catch (InterruptedException e) {
            	return;  // Stopping the thread if it's interrupted
            }
		}
		
		// Step 2: Play through each question in question bank
		for(Question q : questionBank.getAll()) {
			
			// Set the current question so checkAnswer() can validate the answers
			currentQuestion = q;
			currentQuestionId = q.getId();
			
			// Broadcasting the question round to all players
			// server.broadcastToAll("{\"type\":\"question\"}");
			System.out.println("BROADCAST: question");

			// Waiting for the round time duration
			try {
				Thread.sleep(ROUND_SECONDS * 1000); 
			} 
			catch (InterruptedException e) {
				return;  // Stopping the thread if it's interrupted
			}
			
			// Set the currentQuestionId to reject late answers or that arrive after time runs out
			currentQuestionId = -1;
			
			
			// Broadcasting the results of the round to all players
			// server.broadcastToAll("{\"type\":\"round_end\"}");
			System.out.println("BROADCAST: round_end");

			// Creating the short time break before transitioning to the next question
            try {
            	Thread.sleep(BREAK_SECONDS * 1000); 
            } 
            catch (InterruptedException e) {
            	return; // Stopping the thread if it's interrupted
            }
		}
		
		// Step 3: Broadcasting the final results of each player
		// server.broadcastToAll("{\"type\":\"game_over\"}");
		System.out.println("BROADCAST: game_over");
	}
	
	public static void main(String[] args) {
	    Scoreboard scoreboard = new Scoreboard();
	    scoreboard.addPlayer("Bob");
	    scoreboard.addPlayer("Alice");
	    scoreboard.addPlayer("Charlie");

	    QuestionBank questionBank = new QuestionBank();
	    GameLoop gameLoop = new GameLoop(scoreboard, questionBank);

	    new Thread(gameLoop).start();
	}
	
}
