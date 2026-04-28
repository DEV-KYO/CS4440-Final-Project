package quizblitz;

import java.util.List;
import java.util.ArrayList;


public class GameLoop implements Runnable {
	
	private Scoreboard scoreboard;
	private QuestionBank questionBank;
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

	    public int getId() { return id; }
	    public String getCorrect() { return correct; }
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
	
	// Constructor
	public GameLoop(Scoreboard scoreboard, QuestionBank questionBank) {
		this.scoreboard = scoreboard;
		this.questionBank = questionBank;
	}
	
	public void setBroadcaster(GameServer server) {
		this.server = server;
	}
	
	public int getCurrentQuestionId() {
		return currentQuestionId;
	}
	
	public boolean checkAnswer(int questionId, String choice) {
		if(questionId == currentQuestionId && currentQuestion != null) {
			if(choice.equals(currentQuestion.getCorrect())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
		
	}
	
	@Override
	public void run() {
		while(scoreboard.playerCount() < MIN_PLAYERS) {
			// server.broadcastToAll("{\"type\":\"waiting\"}");
			System.out.println("BROADCAST: waiting");
            try { 
            	Thread.sleep(1000); 
            } 
            catch (InterruptedException e) {
            	return; 
            }
		}
		
		for(Question q : questionBank.getAll()) {
			currentQuestionId = q.getId();
			
			// server.broadcastToAll("{\"type\":\"question\"}");
			System.out.println("BROADCAST: question");

			
			try {
				Thread.sleep(ROUND_SECONDS * 1000); 
			} 
			catch (InterruptedException e) {
				return; 
			}
			
			currentQuestionId = -1; // reject late answers
			
			// server.broadcastToAll("{\"type\":\"round_end\"}");
			System.out.println("BROADCAST: round_end");

            try {
            	Thread.sleep(BREAK_SECONDS * 1000); 
            } 
            catch (InterruptedException e) {
            	return; 
            }
		}
		
		// server.broadcastToAll("{\"type\":\"game_over\"}");
		System.out.println("BROADCAST: game_over");
	}
	
	public static void main(String[] args) {
		Scoreboard scoreboard = new Scoreboard();
		
		scoreboard.addPlayer("Bob");
		scoreboard.addPlayer("Alice");
		scoreboard.addPlayer("Mike");
		
		QuestionBank questionBank = new QuestionBank();
		
		GameLoop gameLoop = new GameLoop(scoreboard, questionBank);
		 
		new Thread(gameLoop).start();
	}
}
