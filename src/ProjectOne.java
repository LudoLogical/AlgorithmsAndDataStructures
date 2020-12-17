import java.util.*;
import java.io.*;

/**
 * CS 3345 HON
 * Project 1, SKIP LIST
 * Used compiler Java 14.0.1
 * @author UGQM
 * @version date 10/8/2020
 */

class Node {

    private final int key;
    private final String value;
    private final Node[] forward;

    /**
     * Creates and returns a new Node with the key and value specified and of the level (height) specified.
     * @param key a number in the range [9999,99999] that serves as a unique identifier for this Node within the
     *            SkipList that contains it; cannot be modified after Node creation according to problem spec
     * @param value a string with a length in the range [4,20]; the data contained in this Node; cannot be
     *              modified after Node creation according to problem spec
     * @param level the height of (i.e., number of references to and from) this Node in the SkipList that contains it
     */
    public Node(int key, String value, int level) {
        this.key = key;
        this.value = value;
        this.forward = new Node[level + 1]; // (index 0 remains unused to be consistent with level numbers)
    }

    /**
     * Gives the unique identifier for this Node within the SkipList that contains it.
     * @return this Node's key
     */
    public int getKey() {
        return this.key;
    }

    /**
     * Gives the string (data) that this Node contains.
     * @return this Node's value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Gives the height of (i.e., number of references to and from) this Node in the SkipList that contains it.
     * @return this Node's level
     */
    public int getLevel() {
        return this.forward.length - 1;
    }

    /**
     * Gives the Node following this Node in the SkipList that contains them at the level specified.
     * @param level the level at which the "next" Node should be determined
     * @return the relevant Node, or the header of the SkipList containing this
     *         Node if there is no "next" Node at the level specified
     */
    public Node getForward(int level) {
        return this.forward[level];
    }

    /**
     * Changes the Node following this Node in the SkipList that contains
     * them at the level specified to the new target Node specified.
     * @param level the level at which this Node's forward reference is to be changed
     * @param target the Node that should now follow this Node at the level specified
     */
    protected void setForward(int level, Node target) {
        this.forward[level] = target;
    }

}

class SkipList {

    public final double p;

    private int level;
    private final int maxLevel;
    private final Node header; // does not change or contain a meaningful key/value pair, so can be final

    private int size; // number of key/value pairs in this SkipList; updated when insert() and delete() are called

    public static final int END_SENTINEL = Integer.MAX_VALUE; // larger than largest possible key

    /**
     * Creates and returns a new SkipList that will use the specified value of p when creating its Nodes
     * and that will have at most maxLevel levels (i.e., will be of a height that is no more than maxLevel).
     * @param p a value in the range [0,1] used to determine the probability that each of the Nodes in this
     *          SkipList will be of a given height; see chooseLevel() for details
     * @param maxLevel the maximum possible number of levels in (i.e., the
     *                 maximum height of) this SkipList; must be at least 1
     */
    public SkipList(double p, int maxLevel) {
        this.p = p;
        this.level = 1;
        this.maxLevel = maxLevel;
        this.header = new Node(END_SENTINEL, null, this.maxLevel);
        for (int i = 1; i <= maxLevel; i++) {
            this.header.setForward(i, this.header);
        }
        this.size = 0;
    }

    /**
     * Randomly determines a level (i.e., height) for a new member Node of this SkipList by generating random
     * numbers in the range [0,1) until one of them is at least equal to this SkipList's p value and using the
     * total number of numbers generated as the desired value. Returns the maximum possible height of this
     * SkipList instead if the total number of numbers generated begins to exceed that value.
     * @return a level (i.e., height) for a new member Node of this SkipList
     */
    protected int chooseLevel() {
        int level = 1;
        while (Math.random() < p && level < this.maxLevel) {
            level++;
        }
        return level;
    }

    /* Determines and returns an array of all Nodes in this SkipList such that the following property
    is satisfied for all i: predecessors[i].getKey() < key <= predecessors[i].getForward(i).getKey() */
    private Node[] getPredecessors(int key) {

        // index 0 remains unused to be consistent with level numbers
        Node[] predecessors = new Node[this.maxLevel + 1];

        Node now = this.header;
        for (int i = this.level; i >= 1; i--) {
            while (now.getForward(i).getKey() < key) {
                now = now.getForward(i);
            }
            predecessors[i] = now;
        }
        return predecessors;

    }

    /**
     * Adds a new key/value pair to this SkipList.
     * @param key the key of the key/value pair to be inserted
     * @param s the value of the key/value pair to be inserted
     * @return false if key is already present, true otherwise
     */
    public boolean insert(int key, String s) {

        Node[] predecessors = this.getPredecessors(key);
        Node subject = predecessors[1].getForward(1);

        if (subject.getKey() == key) {

            // subject.setValue(s); could be used here in other implementations
            return false;

        } else { // key does not currently exist in this SkipList

            int newLevel = this.chooseLevel();
            if (newLevel > this.level) {
                newLevel = ++this.level;
                predecessors[newLevel] = this.header;
            }

            // reusing variable subject to reduce clutter
            subject = new Node(key, s, newLevel);
            for (int i = 1; i <= newLevel; i++) {
                subject.setForward(i, predecessors[i].getForward(i));
                predecessors[i].setForward(i, subject);
            }

            this.size++;
            return true;

        }
    }

    /**
     * Determines whether or not a given key exists in this SkipList.
     * @param key the key whose presence is to be checked
     * @return true if key is present, false otherwise
     */
    public boolean isPresent(int key) {
        return !this.find(key).equals("");
    }

    /**
     * Removes the specified key/value pair from this SkipList.
     * @param key the key of the key/value pair to be removed
     * @return false if key is not present, true otherwise
     */
    public boolean delete(int key) {

        Node[] predecessors = this.getPredecessors(key);
        Node subject = predecessors[1].getForward(1);

        if (subject.getKey() == key) {

            for (int i = 1; i <= this.level; i++) {
                if (predecessors[i].getForward(i).getKey() != subject.getKey()) {
                    break; // stops when height of deleted node is exceeded (so no more replacements are necessary)
                }
                predecessors[i].setForward(i, subject.getForward(i));
            }

            while (this.level > 1 && this.header.getForward(this.level).getKey() == END_SENTINEL) {
                this.level--;
            }

            this.size--;
            return true;

        } else { // key does not currently exist in this SkipList

            return false;

        }
    }

    /**
     * Searches for a Node in this SkipList with the specified key and returns its contents (i.e., its value).
     * @param key the Node key to search for (in this SkipList)
     * @return the String element if a Node with the specified key is present
     *         in this SkipList; an empty String (i.e., "") otherwise
     */
    public String find(int key) {
        Node now = this.header;
        for (int i = this.level; i >= 1; i--) {
            while (now.getForward(i).getKey() < key) {
                now = now.getForward(i);
            }
        }
        // Guaranteed that now.getKey() < key <= now.getForward(1).getKey()
        now = now.getForward(1);
        if (now.getKey() == key) {
            return now.getValue();
        } else {
            return "";
        }
    }

    /**
     * Gives the number of Nodes in this SkipList. Note that the header is NOT counted as a Node because it
     * does not contain a usable key/value pair in this implementation (based on the pink note on eLearning).
     * @return this SkipList's size
     */
    public int membership() {
        return this.size;
    }

    /**
     * Prints all members of this SkipList in increasing key order (one key/value pair per line) to System.out.
     */
    public void listAll() {
        Node now = this.header.getForward(1);
        while (now.getKey() != END_SENTINEL) {
            System.out.println(now.getKey() + " " + now.getValue());
            now = now.getForward(1);
        }
    }

    /**
     * Prints the keys and heights (i.e., levels) of all Nodes in this SkipList
     * in increasing key order (one key/height pair per line) to System.out.
     */
    public void debugList() {
        Node now = this.header.getForward(1);
        while (now.getKey() != END_SENTINEL) {
            System.out.println(now.getKey() + " " + now.getLevel());
            now = now.getForward(1);
        }
    }

}

public class ProjectOne {

    public static void main(String[] args) throws IOException {

        SkipList sl = new SkipList(0.25, 5);
        Scanner input = new Scanner(new File("SkipListData.txt"));

        String now;
        while (input.hasNextLine()) {

            now = input.nextLine();
            String[] parameters = now.split(" ");

            switch (now.charAt(0)) {
                case 'A':
                    if (sl.insert(Integer.parseInt(parameters[1]), parameters[2])) {
                        System.out.println(parameters[2] + " inserted");
                    } else {
                        System.out.println("Key " + parameters[1] + " already exists");
                    }
                    break;
                case 'D':
                    if (sl.delete(Integer.parseInt(parameters[1]))) {
                        System.out.println("Key " + parameters[1] + " deleted");
                    } else {
                        System.out.println("Key " + parameters[1] + " not found");
                    }
                    break;
                case 'S':
                    String value = sl.find(Integer.parseInt(parameters[1]));
                    if (!value.equals("")) {
                        System.out.println("Key " + parameters[1] + " found, value " + value);
                    } else {
                        System.out.println("Key " + parameters[1] + " not found");
                    }
                    break;
                case 'M':
                    System.out.println("Membership is " + sl.membership());
                    break;
                case 'L':
                    sl.listAll();
                    break;
                case 'T':
                    sl.debugList();
                    break;
                case 'E':
                    return;
                default:
                    System.out.println("Invalid input line: " + now);
                    break;
            }

        }

    }

}