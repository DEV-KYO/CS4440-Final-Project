# Question.java + QuestionBank.java

**Owner:** Person C (same person as GameLoop)

**What they are:** Simple data classes. No threading, no synchronization, no sockets. Just containers for quiz data.

---

## Question.java

A single quiz question.

### Fields

- `int id` — unique identifier for this question (1, 2, 3...)
- `String text` — the question itself
- `String choiceA` — first answer choice
- `String choiceB` — second answer choice
- `String choiceC` — third answer choice
- `String choiceD` — fourth answer choice
- `String correct` — the letter of the right answer ("A", "B", "C", or "D")

### Methods

- Constructor that takes all seven fields
- A getter for each field: `getId()`, `getText()`, `getChoiceA()`, etc.
- `getCorrect()` — returns the correct answer letter
- `toJSON()` — returns this question as a JSON string matching the protocol format:

```json
{
  "id": 1,
  "text": "What prevents race conditions?",
  "choiceA": "Sockets",
  "choiceB": "Synchronization",
  "choiceC": "Deadlock",
  "choiceD": "Paging"
}
```

---

## QuestionBank.java

Holds all the questions for one game session.

### Fields

- `List<Question> questions` — the full list of questions

### Methods

- Constructor — builds the question list. Two options:
    - **Hardcoded (recommended for demo):** Create each `Question` object manually in the constructor
    - **File-based (stretch goal):** Read from a text file or CSV
- `getAll()` — returns the full list
- `size()` — returns how many questions there are

### Starter questions (OS-themed for the presentation)

Here are some questions to use. Add or change as you like — aim for 5-10 total.

1. What OS mechanism prevents race conditions? → Synchronization
2. What does `volatile` guarantee in Java? → Visibility across threads
3. What is a deadlock? → Two threads waiting on each other forever
4. What data structure does a HashMap use internally? → Hash table with buckets
5. What port does our QuizBlitz server run on? → 8080
6. What Java keyword locks a method to one thread at a time? → synchronized
7. Which scheduling algorithm gives every process equal time slices? → Round Robin
8. What is virtual memory? → Disk space used as an extension of RAM
