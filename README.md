# StackQueue-Arena
📌A console-based Java matching game built using custom Stack and Queue data structures. The game involves strategic tile matching, score tracking, and a persistent high-score system using file handling.
🎮 Stack & Queue Based Matching Game

This project is a console-based game written in Java that demonstrates the use of custom Stack and Queue data structures in a practical and interactive way.

The player interacts with multiple stacks of characters and performs operations like matching tiles, adding elements, and managing queues to maximize their score.

🚀 Features
🔤 5 Stack Sets initialized with random unique letters
🔄 Reserve Queue & Supplementary Queue for dynamic gameplay
🎯 Matching Mechanism (Match(i, j)) to score points
➕ AddSet Operation to push elements from queue to stacks
🔁 ShiftQueue Feature with limited usage
🤖 Auto-fill System every 3 steps
🏆 High Score System (Top 10 players)
💾 File Handling for saving/loading scores
🧠 Fully implemented using custom data structures (no Java Collections)
🧱 Data Structures Used
Stack (Custom Implementation)
Array-based
Operations:
push()
pop()
peek()
isEmpty()
isFull()

📄 See implementation:

Queue (Custom Implementation)
Circular array-based
Operations:
enqueue()
dequeue()
peek()
isEmpty()
isFull()

📄 See implementation:

🕹️ How the Game Works
Player enters their name
5 stacks are initialized with random letters
Two queues are prepared:
Reserve Queue
Supplementary Queue
Player interacts using commands:
📥 Commands
Command	Description
Match(i,j)	Match top elements of two stacks
AddSet(i)	Add element from reserve queue to stack
ShiftQueue	Rotate reserve queue
F	Finish game
🎯 Scoring System
✅ Match success → +5 points
❌ Invalid AddSet (no shifts left) → -2 points
🤖 Auto-add every 3 steps
🏁 Game Ending Conditions
All stacks become empty
Maximum steps reached
Player exits manually
🏆 High Score System
Stores top 10 scores
Uses file: HighScoreTable.txt

Each entry:

PlayerName;Score

📄 Managed in:

📂 Project Structure
├── Game.java          # Main game logic
├── Stack.java         # Custom stack implementation
├── Queue.java         # Custom queue implementation
├── HighScoreTable.txt # Saved scores
⚙️ How to Run
Compile:
javac Game.java Stack.java Queue.java
Run:
java Game
💡 Concepts Demonstrated
Data Structures (Stack & Queue)
Circular Queue logic
File I/O (Read/Write)
Game loop design
Command parsing
Algorithmic thinking
📌 Notes
No built-in Java collections are used
All logic is implemented manually for learning purposes
Designed for educational use in Data Structures courses

👤 Author

Arash Rezaei
