package quizblitz;

import org.json.JSONObject;

public class Question {
    private int id;
    private String text;
    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;
    private String correct;

    public Question(int id, String text, String choiceA, String choiceB, String choiceC, String choiceD,
            String correct) {
        this.id = id;
        this.text = text;
        this.choiceA = choiceA;
        this.choiceB = choiceB;
        this.choiceC = choiceC;
        this.choiceD = choiceD;
        this.correct = correct;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getChoiceA() {
        return choiceA;
    }

    public String getChoiceB() {
        return choiceB;
    }

    public String getChoiceC() {
        return choiceC;
    }

    public String getChoiceD() {
        return choiceD;
    }

    public String getCorrect() {
        return correct;
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("text", text);
        json.put("choiceA", choiceA);
        json.put("choiceB", choiceB);
        json.put("choiceC", choiceC);
        json.put("choiceD", choiceD);
        return json.toString();
    }
}