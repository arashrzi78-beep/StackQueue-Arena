import java.util.Scanner;
import java.util.Random;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
public class Game {
    static Scanner sc  = new Scanner(System.in);
    static Random  rnd = new Random();
    static final int SET_CAPACITY  = 10;
    static final int ALPHABET_SIZE = 26;
    static final int HS_CAPACITY   = 10;
    static final int NAME_SLOT     = 21;
    static final int SCORE_DIGITS  = 6;
    static Stack set1 = new Stack(SET_CAPACITY);
    static Stack set2 = new Stack(SET_CAPACITY);
    static Stack set3 = new Stack(SET_CAPACITY);
    static Stack set4 = new Stack(SET_CAPACITY);
    static Stack set5 = new Stack(SET_CAPACITY);
    static Queue reserveQueue       = new Queue(ALPHABET_SIZE);
    static Queue supplementaryQueue = new Queue(ALPHABET_SIZE);
    static Queue hsNameQueue  = new Queue(HS_CAPACITY * NAME_SLOT);
    static Queue hsScoreQueue = new Queue(HS_CAPACITY * SCORE_DIGITS);
    static int   hsCount      = 0;
    static int score           = 0;
    static int step            = 0;
    static int maxSteps        = 0;
    static int remainingShifts = 0;
    public static void main(String[] args) {
        loadHighScores();
        System.out.print("Enter player name: ");
        Queue playerName    = readLineToQueue();
        int   playerNameLen = playerName.size();
        Queue playerNameCopy = copyQueue(playerName, playerName.size() + 5);
        initializeSets();
        initializeReserveQueue();
        initializeSupplementaryQueue();
        int totalInitialTiles = totalTileCount();
        maxSteps        = (int) (totalInitialTiles * 1.2);
        remainingShifts = 1 + rnd.nextInt(5);
        System.out.println("THE GAME STARTS NOW!...");
        boolean finished = false;
        while (!finished) {
            if (allSetsEmpty() || step >= maxSteps) break;
            displaySets();
            displayQueuesAndInfo();
            System.out.print(">> ");
            Queue cmd = readLineToQueue();
            if (equalsIgnoreCase(cmd, "F")) {
                finished = true;
                break;
            }
            if (startsWithIgnoreCase(cmd, "Match(")) {
                int[] parsed = parseMatch(cmd);
                if (parsed == null) {
                    System.out.println("Invalid command format. Use: Match(i,j)");
                    continue;
                }
                int i = parsed[0];
                int j = parsed[1];
                if (!validSetIndex(i) || !validSetIndex(j) || i == j) {
                    System.out.println("Invalid set indices. Use 1-5 and different sets.");
                    continue;
                }
                Stack first  = getSet(i);
                Stack second = getSet(j);
                if (first.isEmpty() || second.isEmpty()) {
                    System.out.println("One or both sets are empty. Cannot match.");
                    afterValidStep();
                    continue;
                }
                if (first.peek() == second.peek()) {
                    first.pop();
                    second.pop();
                    score += 5;
                    System.out.println("Match successful! +5 points.");
                    afterValidStep();
                } else {
                    System.out.println("No match! Tiles are different. Try again.");
                }
                continue;
            }
            if (startsWithIgnoreCase(cmd, "AddSet(")) {
                int idx = parseAddSet(cmd);
                if (!validSetIndex(idx)) {
                    System.out.println("Invalid command format. Use: AddSet(i)");
                    continue;
                }
                if (reserveQueue.isEmpty()) {
                    System.out.println("Reserve Queue is empty.");
                    afterValidStep();
                    continue;
                }
                if (remainingShifts <= 0) {
                    score -= 2;
                    System.out.println("No shift rights left. AddSet penalty: -2 points.");
                }
                char  tile   = reserveQueue.dequeue();
                Stack target = getSet(idx);
                if (target.isFull()) {
                    System.out.println("Set" + idx + " is full! Tile returned to Reserve Queue.");
                    reserveQueue.enqueue(tile);
                } else {
                    target.push(tile);
                }
                afterValidStep();
                continue;
            }
            if (equalsIgnoreCase(cmd, "ShiftQueue")) {
                if (remainingShifts <= 0) {
                    System.out.println("No shift rights remaining. Cannot shift.");
                } else {
                    if (!reserveQueue.isEmpty()) {
                        reserveQueue.enqueue(reserveQueue.dequeue());
                    }
                    remainingShifts--;
                    System.out.println("Reserve queue shifted.");
                }
                afterValidStep();
                continue;
            }
            System.out.println("Invalid command. Use: Match(i,j) | AddSet(i) | ShiftQueue | F");
        }
        System.out.println("END OF THE GAME!...");
        updateHighScoreTable(playerNameCopy, playerNameLen, score);
        displayHighScores();
        saveHighScores();
        System.out.print("Play again? ");
        readLineToQueue();
    }
    static void afterValidStep() {
        step++;
        if (step % 3 == 0) autoAddFromSupplementary();
    }
    static void initializeSets() {
        initializeOneSet(set1);
        initializeOneSet(set2);
        initializeOneSet(set3);
        initializeOneSet(set4);
        initializeOneSet(set5);
    }
    static void initializeOneSet(Stack set) {
        int count = 1 + rnd.nextInt(10);
        int added = 0;
        while (added < count) {
            char letter = (char) ('A' + rnd.nextInt(26));
            if (!stackContains(set, letter)) {
                set.push(letter);
                added++;
            }
        }
    }
    static void initializeReserveQueue() {
        Queue temp = new Queue(ALPHABET_SIZE);
        for (char c = 'A'; c <= 'Z'; c++) temp.enqueue(c);
        shuffleInto(temp, reserveQueue);
    }
    static void initializeSupplementaryQueue() {
        Queue temp = new Queue(ALPHABET_SIZE);
        for (char c = 'A'; c <= 'Z'; c++) temp.enqueue(c);
        shuffleInto(temp, supplementaryQueue);
    }
    static void shuffleInto(Queue source, Queue target) {
        Queue bag = new Queue(ALPHABET_SIZE);
        while (!source.isEmpty()) bag.enqueue(source.dequeue());
        while (!bag.isEmpty()) {
            int size = bag.size();
            int pick = rnd.nextInt(size);
            for (int i = 0; i < pick; i++) bag.enqueue(bag.dequeue());
            target.enqueue(bag.dequeue());
        }
    }
    static void autoAddFromSupplementary() {
        if (supplementaryQueue.isEmpty()) return;
        int   minIndex = getMinSetIndex();
        Stack target   = getSet(minIndex);
        char  tile     = supplementaryQueue.dequeue();
        if (target.isFull()) {
            supplementaryQueue.enqueue(tile);
            System.out.println("[Auto] Set" + minIndex + " is full. Tile returned to Supplementary Queue.");
        } else {
            target.push(tile);
            System.out.println("[Auto] Tile added to Set" + minIndex + " from Supplementary Queue.");
        }
    }
    static int getMinSetIndex() {
        int minIndex = 1;
        int minSize  = getSet(1).size();
        for (int i = 2; i <= 5; i++) {
            if (getSet(i).size() < minSize) {
                minSize  = getSet(i).size();
                minIndex = i;
            }
        }
        return minIndex;
    }
    static int totalTileCount() {
        return set1.size() + set2.size() + set3.size() + set4.size() + set5.size();
    }
    static boolean allSetsEmpty() {
        return set1.isEmpty() && set2.isEmpty() && set3.isEmpty()
                && set4.isEmpty() && set5.isEmpty();
    }
    static Stack getSet(int index) {
        if (index == 1) return set1;
        if (index == 2) return set2;
        if (index == 3) return set3;
        if (index == 4) return set4;
        return set5;
    }
    static boolean validSetIndex(int index) {
        return index >= 1 && index <= 5;
    }
    static boolean stackContains(Stack stack, char target) {
        Stack temp = new Stack(SET_CAPACITY);
        boolean found = false;
        while (!stack.isEmpty()) {
            char c = stack.pop();
            if (c == target) found = true;
            temp.push(c);
        }
        Stack restore = new Stack(SET_CAPACITY);
        while (!temp.isEmpty()) restore.push(temp.pop());
        while (!restore.isEmpty()) stack.push(restore.pop());
        return found;
    }
    static void displaySets() {
        printSet("Set1", set1);
        printSet("Set2", set2);
        printSet("Set3", set3);
        printSet("Set4", set4);
        printSet("Set5", set5);
    }
    static void printSet(String name, Stack stack) {
        System.out.print(name + ": Top -> ");
        if (stack.isEmpty()) {
            System.out.println("<- Bottom");
            return;
        }
        Stack temp = new Stack(SET_CAPACITY);
        while (!stack.isEmpty()) temp.push(stack.pop());
        Stack display = new Stack(SET_CAPACITY);
        while (!temp.isEmpty()) {
            char c = temp.pop();
            stack.push(c);
            display.push(c);
        }
        boolean first = true;
        while (!display.isEmpty()) {
            char c = display.pop();
            if (first) {
                System.out.println(c);
                first = false;
            } else {
                System.out.println("          " + c);
            }
        }
        System.out.println("          <- Bottom");
    }
    static void displayQueuesAndInfo() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Reserve Queue:");
        printQueue(reserveQueue);
        System.out.println("Supplementary Queue:");
        printQueue(supplementaryQueue);
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Score: " + score + " | Remaining Shifts: " + remainingShifts
                + " | Step: " + step + "/" + maxSteps);
    }
    static void printQueue(Queue q) {
        System.out.print(" Front -> ");
        int size = q.size();
        for (int i = 0; i < size; i++) {
            char c = q.dequeue();
            System.out.print(c);
            if (i != size - 1) System.out.print(" ");
            q.enqueue(c);
        }
        System.out.println(" <- Rear");
    }
    static Queue readLineToQueue() {
        String line = sc.nextLine().trim();
        Queue q = new Queue(line.length() + 5);
        for (int i = 0; i < line.length(); i++) q.enqueue(line.charAt(i));
        return q;
    }
    static Queue copyQueue(Queue original, int capacity) {
        Queue copy = new Queue(capacity);
        int size = original.size();
        for (int i = 0; i < size; i++) {
            char c = original.dequeue();
            original.enqueue(c);
            copy.enqueue(c);
        }
        return copy;
    }
    static char toLower(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(c + 32);
        return c;
    }
    static boolean equalsIgnoreCase(Queue q, String literal) {
        if (q.size() != literal.length()) return false;
        Queue temp = new Queue(q.size() + 5);
        boolean same = true;
        for (int i = 0; i < literal.length(); i++) {
            char c = q.dequeue();
            temp.enqueue(c);
            if (toLower(c) != toLower(literal.charAt(i))) same = false;
        }
        while (!temp.isEmpty()) q.enqueue(temp.dequeue());
        return same;
    }
    static boolean startsWithIgnoreCase(Queue q, String literal) {
        if (q.size() < literal.length()) return false;
        Queue temp = new Queue(q.size() + 5);
        boolean same = true;
        for (int i = 0; i < literal.length(); i++) {
            char c = q.dequeue();
            temp.enqueue(c);
            if (toLower(c) != toLower(literal.charAt(i))) same = false;
        }
        while (!q.isEmpty()) temp.enqueue(q.dequeue());
        while (!temp.isEmpty()) q.enqueue(temp.dequeue());
        return same;
    }
    static void skipSpaces(Queue q) {
        while (!q.isEmpty() && q.peek() == ' ') q.dequeue();
    }
    static int readNumber(Queue q) {
        int value = 0;
        boolean hasDigit = false;
        while (!q.isEmpty() && q.peek() >= '0' && q.peek() <= '9') {
            hasDigit = true;
            value = value * 10 + (q.dequeue() - '0');
        }
        return hasDigit ? value : -1;
    }
    static int[] parseMatch(Queue cmd) {
        Queue work = copyQueue(cmd, cmd.size() + 5);
        for (int i = 0; i < 6; i++) if (!work.isEmpty()) work.dequeue();
        skipSpaces(work);
        int first = readNumber(work);
        if (first == -1) return null;
        skipSpaces(work);
        if (work.isEmpty() || work.dequeue() != ',') return null;
        skipSpaces(work);
        int second = readNumber(work);
        if (second == -1) return null;
        skipSpaces(work);
        if (work.isEmpty() || work.dequeue() != ')') return null;
        skipSpaces(work);
        if (!work.isEmpty()) return null;
        return new int[]{first, second};
    }
    static int parseAddSet(Queue cmd) {
        Queue work = copyQueue(cmd, cmd.size() + 5);
        for (int i = 0; i < 7; i++) if (!work.isEmpty()) work.dequeue();
        skipSpaces(work);
        int idx = readNumber(work);
        if (idx == -1) return -1;
        skipSpaces(work);
        if (work.isEmpty() || work.dequeue() != ')') return -1;
        skipSpaces(work);
        if (!work.isEmpty()) return -1;
        return idx;
    }
    static Queue buildNameSlot(Queue nameQ, int length) {
        Queue slot    = new Queue(NAME_SLOT);
        int   maxChars = length <= 20 ? length : 20;
        int   count   = 0;
        while (!nameQ.isEmpty() && count < maxChars) {
            slot.enqueue(nameQ.dequeue());
            count++;
        }
        slot.enqueue('|');
        while (slot.size() < NAME_SLOT) slot.enqueue(' ');
        return slot;
    }
    static void enqueueNameSlot(Queue dest, Queue slot) {
        int size = slot.size();
        for (int i = 0; i < size; i++) dest.enqueue(slot.dequeue());
    }
    static Queue dequeueNameSlot(Queue src) {
        Queue slot = new Queue(NAME_SLOT);
        for (int i = 0; i < NAME_SLOT; i++) slot.enqueue(src.dequeue());
        return slot;
    }
    static void enqueueScore(Queue dest, int value) {
        Stack digits = new Stack(SCORE_DIGITS);
        for (int i = 0; i < SCORE_DIGITS; i++) {
            digits.push((char)('0' + (value % 10)));
            value /= 10;
        }
        while (!digits.isEmpty()) dest.enqueue(digits.pop());
    }
    static int dequeueScore(Queue src) {
        int value = 0;
        for (int i = 0; i < SCORE_DIGITS; i++) value = value * 10 + (src.dequeue() - '0');
        return value;
    }
    static void printNameFromSlot(Queue slot) {
        Queue temp  = new Queue(NAME_SLOT);
        boolean stop = false;
        int size = slot.size();
        for (int i = 0; i < size; i++) {
            char c = slot.dequeue();
            temp.enqueue(c);
            if (!stop) {
                if (c == '|') stop = true;
                else System.out.print(c);
            }
        }
        while (!temp.isEmpty()) slot.enqueue(temp.dequeue());
    }
    static void updateHighScoreTable(Queue playerNameQ, int playerLength, int playerScore) {
        Queue newNameQ  = new Queue((HS_CAPACITY + 1) * NAME_SLOT);
        Queue newScoreQ = new Queue((HS_CAPACITY + 1) * SCORE_DIGITS);
        boolean inserted  = false;
        int originalCount = hsCount;
        for (int i = 0; i < originalCount; i++) {
            Queue oldSlot  = dequeueNameSlot(hsNameQueue);
            int   oldScore = dequeueScore(hsScoreQueue);
            if (!inserted && playerScore >= oldScore) {
                Queue slot = buildNameSlot(copyQueue(playerNameQ, playerLength + 5), playerLength);
                enqueueNameSlot(newNameQ, slot);
                enqueueScore(newScoreQ, playerScore);
                inserted = true;
            }
            enqueueNameSlot(newNameQ, oldSlot);
            enqueueScore(newScoreQ, oldScore);
        }
        if (!inserted && originalCount < HS_CAPACITY) {
            Queue slot = buildNameSlot(copyQueue(playerNameQ, playerLength + 5), playerLength);
            enqueueNameSlot(newNameQ, slot);
            enqueueScore(newScoreQ, playerScore);
        }
        hsNameQueue  = new Queue(HS_CAPACITY * NAME_SLOT);
        hsScoreQueue = new Queue(HS_CAPACITY * SCORE_DIGITS);
        hsCount      = 0;
        int limit = originalCount + 1;
        if (limit > HS_CAPACITY) limit = HS_CAPACITY;
        for (int i = 0; i < limit; i++) {
            if (newScoreQ.isEmpty()) break;
            enqueueNameSlot(hsNameQueue, dequeueNameSlot(newNameQ));
            enqueueScore(hsScoreQueue, dequeueScore(newScoreQ));
            hsCount++;
        }
    }
    static void displayHighScores() {
        System.out.println("High Score Table");
        for (int i = 0; i < hsCount; i++) {
            Queue slot = dequeueNameSlot(hsNameQueue);
            int   s    = dequeueScore(hsScoreQueue);
            printNameFromSlot(slot);
            System.out.println(" " + s);
            enqueueNameSlot(hsNameQueue, slot);
            enqueueScore(hsScoreQueue, s);
        }
    }
    static void saveHighScores() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("HighScoreTable.txt"));
            pw.println("High Score Table");
            for (int i = 0; i < hsCount; i++) {
                Queue slot = dequeueNameSlot(hsNameQueue);
                int   s    = dequeueScore(hsScoreQueue);
                Queue temp  = new Queue(NAME_SLOT);
                boolean stop = false;
                while (!slot.isEmpty()) {
                    char c = slot.dequeue();
                    temp.enqueue(c);
                    if (!stop) {
                        if (c == '|') stop = true;
                        else pw.print(c);
                    }
                }
                pw.println(";" + s);
                while (!temp.isEmpty()) slot.enqueue(temp.dequeue());
                enqueueNameSlot(hsNameQueue, slot);
                enqueueScore(hsScoreQueue, s);
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("Error saving HighScoreTable.txt");
        }
    }
    static void loadHighScores() {
        hsNameQueue  = new Queue(HS_CAPACITY * NAME_SLOT);
        hsScoreQueue = new Queue(HS_CAPACITY * SCORE_DIGITS);
        hsCount      = 0;
        try {
            FileReader fr       = new FileReader("HighScoreTable.txt");
            Queue      line     = new Queue(100);
            int        ch;
            boolean    firstLine = true;
            while ((ch = fr.read()) != -1) {
                if (ch == '\r') continue;
                if (ch == '\n') {
                    if (firstLine) firstLine = false;
                    else           loadOneHighScoreLine(line);
                    line = new Queue(100);
                } else {
                    line.enqueue((char) ch);
                }
            }
            if (!line.isEmpty() && !firstLine) loadOneHighScoreLine(line);
            fr.close();
        } catch (IOException e) {
        }
    }
    static void loadOneHighScoreLine(Queue line) {
        if (line.isEmpty() || hsCount >= HS_CAPACITY) return;
        Queue   nameQ          = new Queue(30);
        Queue   scoreQ         = new Queue(20);
        boolean foundSemicolon = false;
        int     size           = line.size();
        for (int i = 0; i < size; i++) {
            char c = line.dequeue();
            if (!foundSemicolon) {
                if (c == ';') foundSemicolon = true;
                else nameQ.enqueue(c);
            } else {
                if (c >= '0' && c <= '9') scoreQ.enqueue(c);
            }
        }
        if (!foundSemicolon || nameQ.isEmpty()) return;
        int playerScore = 0;
        while (!scoreQ.isEmpty()) playerScore = playerScore * 10 + (scoreQ.dequeue() - '0');
        Queue slot = buildNameSlot(nameQ, nameQ.size());
        enqueueNameSlot(hsNameQueue, slot);
        enqueueScore(hsScoreQueue, playerScore);
        hsCount++;
    }
}