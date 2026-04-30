package quizblitz;

import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    private List<Question> questions;

    public QuestionBank() {
        questions = new ArrayList<>();

        questions.add(new Question(1,
                "Which of the following is a Pokemon?",
                "Duosion",
                "Ramelteon",
                "Zydelig",
                "Evenity",
                "A"));

        questions.add(new Question(2,
                "Which of the following is a drug?",
                "Amaura",
                "Lokix",
                "Fiasp",
                "Braixen",
                "C"));

        questions.add(new Question(3,
                "Which of the following is a Pokemon?",
                "Veluza",
                "Xeljanz",
                "Ozempic",
                "Januvia",
                "A"));

        questions.add(new Question(4,
                "Which of the following is a drug?",
                "Tinkaton",
                "Hatterene",
                "Xarelto",
                "Indeedee",
                "C"));

        questions.add(new Question(5,
                "Which of the following is a Pokemon?",
                "Bruxish",
                "Eliquis",
                "Farxiga",
                "Jardiance",
                "A"));

        questions.add(new Question(6,
                "Which of the following is a drug?",
                "Dragapult",
                "Ceruledge",
                "Skyrizi",
                "Annihilape",
                "C"));

        questions.add(new Question(7,
                "Which of the following is a Pokemon?",
                "Clodsire",
                "Humira",
                "Dupixent",
                "Rinvoq",
                "A"));

        questions.add(new Question(8,
                "Which of the following is a drug?",
                "Garchomp",
                "Lucario",
                "Keytruda",
                "Greninja",
                "C"));

        questions.add(new Question(9,
                "Which of the following is a Pokemon?",
                "Sylveon",
                "Lipitor",
                "Nexium",
                "Plavix",
                "A"));

        questions.add(new Question(10,
                "Which of the following is a drug?",
                "Charizard",
                "Pikachu",
                "Blastoise",
                "Ibuprofen",
                "D"));
    }

    public List<Question> getAll() {
        return questions;
    }

    public int size() {
        return questions.size();
    }
}