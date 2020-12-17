import java.util.*;
import java.io.*;

/**
 * CS 3345 HON
 * Project 2, OPEN HASH TABLE (DOUBLE HASHING)
 * Used compiler Java 14.0.1
 * @author UGQM
 * @version date 10/30/2020
 */

class Entry {

    // Symbolic Constants
    public static final int UNUSED = 0;
    public static final int FREE = 1;
    public static final int OCCUPIED = 2;

    private String key;
    private String value;
    private int avail;

    /**
     * Creates and returns a new, empty Entry.
     */
    public Entry() {
        this.key = null;
        this.value = null;
        this.avail = UNUSED;
    }

    /**
     * Gives the key currently stored in this Entry if it contains a key/value pair.
     * @return this Entry's key if it is currently OCCUPIED, null otherwise
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Gives the value currently stored in this Entry if it contains a key/value pair.
     * @return this Entry's value if it is currently OCCUPIED, null otherwise
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Gives the current status of this Entry.
     * @return Entry.UNUSED if this Entry has never been filled, Entry.OCCUPIED if
     *         this Entry currently contains a key/value pair, Entry.FREE otherwise
     */
    public int getAvailability() {
        return this.avail;
    }

    /**
     * Assigns the specified key/value pair to this Entry.
     * @param key a String containing at least 1 and no more than 20 upper-case alphabetic
     *            characters that will serve as a unique identifier for the key/value
     *            pair to which it belongs in the MyHashTable that contains it
     * @param value a String containing at least 1 and no more than 20
     *              characters; the data to be stored in this Entry
     */
    protected void fill(String key, String value) {
        this.key = key;
        this.value = value;
        this.avail = OCCUPIED;
    }

    /**
     * Removes the key/value pair currently stored in this Entry, if there is one.
     */
    protected void empty() {
        this.key = null;
        this.value = null;
        if (this.avail != UNUSED) { // safeguard in case empty() is called before fill()
            this.avail = FREE;
        }
    }

    /**
     * Empties this Entry and changes its status to Entry.UNUSED.
     */
    protected void reset() {
        this.empty();
        this.avail = UNUSED;
    }

}

class MyHashTable {

    private final int r;
    private final Entry[] table;

    private int currentMembership;
    private int numInsertSuccesses;
    private int totalProbesFromSuccessfulInserts;
    private int numSearchSuccesses;
    private int totalProbesFromSuccessfulSearches;
    private int numSearchFailures;
    private int totalProbesFromUnsuccessfulSearches;

    /**
     * Creates and returns a new MyHashTable of a fixed size that uses Double Hashing where
     * the secondary hash function is given by h_2(y) = R - (h_1(y) mod R).
     * @param sz the (fixed) size of this MyHashTable; must be prime and less than 10,000
     * @param R the value to be used in the equation for the secondary hash function shown above
     */
    public MyHashTable(int sz, int R) {
        this.r = R;
        this.table = new Entry[sz];
        for (int i = 0; i < table.length; i++) {
            this.table[i] = new Entry();
        }
        this.currentMembership = 0;
        this.resetStatistics();
    }

    /**
     * Produces and returns a hash code in the range [0, tableSize - 1] for the key specified.
     * @param key the String to be hashed
     * @param tableSize the number of possible hash codes, i.e., the size of the destination hash table
     * @return an integer hash code in the range [0, tableSize - 1]
     */
    public static int hash(String key, int tableSize) {
        int result = 0;
        for (int i = 0; i < key.length() - 1; i++) {
            result += key.charAt(i);
            result *= 31;
        }
        result += key.charAt(key.length() - 1);
        return Integer.remainderUnsigned(result, tableSize);
    }

    /**
     * An alias for the static hash() method that uses the (fixed) size of this MyHashTable.
     * @param key the String to be hashed
     * @return an integer hash code in the range [0, tableSize - 1]
     */
    public int hash(String key) {
        return hash(key, this.table.length);
    }

    // Produces and returns a secondary hash code for use in probing.
    private int secondHash(int hash) {
        return this.r - Integer.remainderUnsigned(hash, this.r);
    }

    /**
     * Inserts the specified key/value pair into an empty Entry in this MyHashTable,
     * if possible. Throws an UnsupportedOperationException on table overflow.
     * @param key the key (i.e., unique identifier) of the key/value pair to be inserted
     *            (must contain ONLY 1-20 upper-case alphabetic characters)
     * @param value the value (i.e., data) of the key/value pair to be inserted
     * @return false if a key/value pair with the specified key is already present
     *         (and DOES NOT replace the old key/value pair), true otherwise
     */
    public boolean insert(String key, String value) {

        /* this check needs to be included because without it, scenarios like the following are possible:
        1. two key/value pairs with keys (e.g., A and B) that have the same hash code (e.g., 5) are inserted;
           the one with key A is placed in Entry 5 and the one with key B is placed in, say, Entry 8 (!= 5 due
           to a collision)
        2. the key/value pair with key A is removed (Entry 5 is FREE, Entry 8 is OCCUPIED)
        3. the user attempts to insert a new key/value pair with key B, which fits in Entry 5
           (Entry 8 is never probed)
        4. there are now two Entries in the table with EXACTLY the same key (i.e., key B; this is FORBIDDEN)
         */

        // Note: to align with the project spec's definition of what constitutes a probe during an insert,
        // statistics are not considered during this find() operation

        if (!find(key, false).equals("")) {
            return false;
        }

        // ---

        int numProbes = 0;
        int hash1 = this.hash(key);
        int hash2 = this.secondHash(hash1);
        Entry target;

        for (int i = 0; i < this.table.length; i++) {

            numProbes++;
            target = this.table[Integer.remainderUnsigned(hash1 + i * hash2, this.table.length)];

            if (target.getAvailability() != Entry.OCCUPIED) {
                target.fill(key, value);
                this.currentMembership++;
                this.numInsertSuccesses++;
                this.totalProbesFromSuccessfulInserts += numProbes;
                return true;
            }

        }

        // At this point, every Entry in this.table has been checked, so insertion would cause overflow
        throw new UnsupportedOperationException("Overflow: cannot insert unknown key into full MyHashTable.");

    }

    // Performs the operation specified in documentation for the public find() method below, but
    // allows the programmer to specify whether or not search-related statistics should be modified
    private String find(String key, boolean doStatistics) {

        int numProbes = 0;
        int hash1 = this.hash(key);
        int hash2 = this.secondHash(hash1);
        Entry target;

        for (int i = 0; i < this.table.length; i++) {

            numProbes++;
            target = this.table[Integer.remainderUnsigned(hash1 + i * hash2, this.table.length)];

            if (target.getAvailability() == Entry.OCCUPIED) {
                if (target.getKey().equals(key)) {
                    if (doStatistics) {
                        this.numSearchSuccesses++;
                        this.totalProbesFromSuccessfulSearches += numProbes;
                    }
                    return target.getValue();
                }
            } else if (target.getAvailability() == Entry.UNUSED) {
                // This Entry has never been subject to insertion, so no need to probe further
                if (doStatistics) {
                    this.numSearchFailures++;
                    this.totalProbesFromUnsuccessfulSearches += numProbes;
                }
                return "";
            }

        }

        // At this point, every Entry in this.table has been checked, so search has failed
        if (doStatistics) {
            this.numSearchFailures++;
            this.totalProbesFromUnsuccessfulSearches += numProbes;
        }
        return "";

    }

    /**
     * Searches for a key/value pair in this MyHashTable with the specified key
     * and returns its contents (i.e., its value) if it is found.
     * @param key the key to search for (in this MyHashTable)
     * @return the value of the key/value pair in this MyHashTable with the specified
     *         key if that key/value pair is found; an empty String (i.e., "") otherwise
     */
    public String find(String key) {
        return this.find(key, true);
    }

    /**
     * Removes the key/value pair with the specified key from this MyHashTable, if possible.
     * @param key the key of the key/value pair to be removed
     * @return false if no key/value pair with the specified key is present, true otherwise
     */
    public boolean delete(String key) {

        int hash1 = this.hash(key);
        int hash2 = this.secondHash(hash1);
        Entry target;

        for (int i = 0; i < this.table.length; i++) {

            target = this.table[Integer.remainderUnsigned(hash1 + i * hash2, this.table.length)];

            if (target.getAvailability() == Entry.OCCUPIED) {
                if (target.getKey().equals(key)) {
                    target.empty();
                    this.currentMembership--;
                    return true;
                }
            } else if (target.getAvailability() == Entry.UNUSED) {
                // This Entry has never been subject to insertion, so no need to probe further
                return false;
            }

        }

        // At this point, every Entry in this.table has been
        // checked, so no Entry contains the specified key
        return false;

    }

    /**
     * Gives the number of key/value pairs in this MyHashTable.
     * @return the number of records (i.e., key/value pairs) in this MyHashTable
     */
    public int membership() {
        return this.currentMembership;
    }

    /**
     * Prints all key/value pairs in this MyHashTable (each on its own line and preceded with
     * an integer giving its index in the table) to System.out in the order that they are stored.
     */
    public void listAll() {
        for (int i = 0; i < this.table.length; i++) {
            Entry target = this.table[i];
            if (target.getAvailability() == Entry.OCCUPIED) {
                System.out.println(i + " " + target.getKey() + ":" + target.getValue());
            }
        }
    }

    /**
     * Prints the following statistics for this MyHashTable: the total number of successful inserts,
     * the total number of probes used on successful inserts, the total number of successful searches,
     * the total number of probes used on successful searches, the total number of unsuccessful searches,
     * and the total number of probes used on unsuccessful searches.
     */
    public void printStatistics() {
        System.out.println("Total Number of Successful Inserts = " + this.numInsertSuccesses);
        System.out.println("Total Number of Probes on Successful Inserts = "
                            + this.totalProbesFromSuccessfulInserts);
        System.out.println("Total Number of Successful Searches = " + this.numSearchSuccesses);
        System.out.println("Total Number of Probes on Successful Searches = "
                            + this.totalProbesFromSuccessfulSearches);
        System.out.println("Total Number of Unsuccessful Searches = " + this.numSearchFailures);
        System.out.println("Total Number of Probes on Unsuccessful Searches = "
                            + this.totalProbesFromUnsuccessfulSearches);
    }

    // Resets the statistics for this MyHashTable.
    private void resetStatistics() {
        this.numInsertSuccesses = 0;
        this.totalProbesFromSuccessfulInserts = 0;
        this.numSearchSuccesses = 0;
        this.totalProbesFromSuccessfulSearches = 0;
        this.numSearchFailures = 0;
        this.totalProbesFromUnsuccessfulSearches = 0;
    }

    /**
     * Empties this MyHashTable and resets its statistics.
     */
    public void clear() {
        for (Entry entry : this.table) {
            entry.reset();
        }
        this.currentMembership = 0;
        this.resetStatistics();
    }

}

public class ProjectTwo {

    public static void main(String[] args) throws IOException {

        MyHashTable hashTable;
        Scanner input = new Scanner(new File("HashTableData.txt"));

        String now;
        now = input.nextLine();
        String[] parameters = now.split("[ :]");

        if (now.charAt(0) == 'D') {
            hashTable = new MyHashTable(Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2]));
        } else {
            throw new IllegalArgumentException("Invalid data file: first line must contain a D command.");
        }

        while (input.hasNextLine()) {

            now = input.nextLine();
            parameters = now.split("[ :]");

            switch (now.charAt(0)) {
                case 'C':
                    hashTable.clear();
                    break;
                case 'H':
                    System.out.println(parameters[1] + ' ' + hashTable.hash(parameters[1]));
                    break;
                case 'A':
                    if (hashTable.insert(parameters[1], parameters[2])) {
                        System.out.println("Key " + parameters[1] + " inserted");
                    } else {
                        System.out.println("Key " + parameters[1] + " already exists");
                    }
                    break;
                case 'R':
                    if (hashTable.delete(parameters[1])) {
                        System.out.println("Key " + parameters[1] + " deleted");
                    } else {
                        System.out.println("Key " + parameters[1] + " not found");
                    }
                    break;
                case 'S':
                    String value = hashTable.find(parameters[1]);
                    if (!value.equals("")) {
                        System.out.println("Key " + parameters[1] + ":" + value);
                    } else {
                        System.out.println("Key " + parameters[1] + " not found");
                    }
                    break;
                case 'M':
                    System.out.println("Membership is " + hashTable.membership());
                    break;
                case 'P':
                    hashTable.listAll();
                    break;
                case 'T':
                    hashTable.printStatistics();
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