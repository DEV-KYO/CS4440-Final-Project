# Question.java + QuestionBank.java

**Owner:** Person E

**What they do:** These are simple data classes — they just hold the quiz questions. No networking, no threads, no synchronization. Just the questions and a way to get them.

---

## Question.java

Represents one quiz question.

### Fields

- `id` — a number (1, 2, 3...) that identifies the question
- `text` — the question itself
- `choiceA`, `choiceB`, `choiceC`, `choiceD` — the four answer options
- `correct` — which letter is right (`"A"`, `"B"`, `"C"`, or `"D"`)

### Methods

- A constructor that takes all seven values
- Getters for each field (`getId()`, `getText()`, `getChoiceA()`, etc.)
- `toJSON()` — converts the question to a JSON string that gets sent to players

The JSON format has to look like this exactly, because the player's browser reads these field names by name:

```json
{
  "id": 1,
  "text": "What prevents race conditions?",
  "choiceA": "Sockets",
  "choiceB": "Synchronization",
  "choiceC": "Deadlock",
  "choiceD": "Paging",
  "correct": "B"
}
```

---

## QuestionBank.java

Holds the full list of questions for the game.

### Methods

- Constructor — builds the list of questions (hardcoded in the constructor)
- `getAll()` — returns all the questions. GameLoop calls this to loop through the rounds
- `size()` — returns how many questions there are

### Changing the questions

All 10 questions are hardcoded in the constructor. If you want to add, remove, or change questions, this is the only file you need to edit. Just make sure:

- Each question has a unique `id`
- The `correct` field is exactly `"A"`, `"B"`, `"C"`, or `"D"` — capital letter, one character
- The question text and choices aren't empty
