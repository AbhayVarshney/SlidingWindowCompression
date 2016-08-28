import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

public class SlidingWindowCompression {
    public static void main(String[] args) {
        FileReader inFile = null;
        FileWriter outFile = null;
        
        ArrayList<Character> origFile = new ArrayList<Character>();
        ArrayList<Character> compressedFile;
        ArrayList<Character> decompressedFile;
        
        try {
            boolean compression = false;
            if(compression) {
                inFile = new FileReader("pease_porridge.txt");
                outFile = new FileWriter("output.txt");
            } else {
                inFile = new FileReader("pease_porridge_output.txt");
                outFile = new FileWriter("pease_porridge_decompression.txt");
            }
            
            while(inFile.ready()) { // READS THE FILE
                // Reads a single character as a byte
                byte c = (byte)inFile.read();
                origFile.add((char)c); // individual stores each byte into arraylist as a character!
            }
            
            // COMPRESSION
            if(compression) {
                SlidingWindowCompression compressAlg = new SlidingWindowCompression(origFile);
                compressAlg.compress();
                compressedFile = compressAlg.getCompressed();
                
                // WRTES TO OUTPUT FILE
                for(int i = 0; i < compressedFile.size(); i++) {
                    outFile.write(compressedFile.get(i));
                }
            } else {
                // DECOMPRESSION
                SlidingWindowDecompression decompressAlg = new SlidingWindowDecompression(origFile);
                decompressAlg.decompress();
                decompressedFile = decompressAlg.getDecompressed();
                // WRTES TO OUTPUT FILE
                for(int i = 0; i < decompressedFile.size(); i++) {
                    outFile.write(decompressedFile.get(i));
                    System.out.print(decompressedFile.get(i));
                }
            }
            
            try {
                outFile.close();
            } catch (IOException e) {
                System.out.println("Error " + e.getMessage());
            }
            System.out.println();
            System.out.println("DONE");
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class SlidingWindowCompression {
    // ATTRIBUTES
    private ArrayList<Character> origFile;
    private ArrayList<Character> window;
    private ArrayList<Character> answer;
    private ArrayList<Character> remaining;
    private final int WINDOW_SIZE;      // WINDOW SIZE  
    private int startPos;       // CURRENT POSITION MARKER
    private int endPos;         // MARK WHERE THE END IS
    private int specialMarker;
    private String windowConversion;    // CONVERT WINDOW ARRAYLIST INTO A STRING
    private String remainingConversion; // CONVERT REMAINING TEXT INTO A STRING
    
    // CONSTRUCTOR
    SlidingWindowCompression(ArrayList<Character> origFile) {
        this.origFile = origFile; 
        answer = new ArrayList<Character>();
        WINDOW_SIZE = 30;
        startPos = 0;
        endPos = WINDOW_SIZE - 1;
        windowConversion = "";
        remainingConversion = "";
        specialMarker = 7;
    }
    
    void compress() {
        for(int i = 0; i < WINDOW_SIZE; i++) { // STORE FIRST x(WINDOW_SIZE) #'s INTO ANSWER ARRAYLIST
            answer.add(origFile.get(i));
        }
        
        while(origFile.size()-1 > endPos) { //can't reach end
            window = new ArrayList<Character>(origFile.subList(startPos, endPos + 1));
            windowConversion = "";
            // Convert to String
            for(int i = 0; i < window.size(); i++) {
                windowConversion += window.get(i);
            }
            
            int length = origFile.size();
            if(origFile.size() - endPos > window.size()) {
                length = endPos + window.size();
            }
            
            remaining = new ArrayList<Character>(origFile.subList(endPos+1, length));
            remainingConversion = "";
            // Convert to String
            for(int i = 0; i < remaining.size(); i++) {
                remainingConversion += remaining.get(i);
            }
            
            int size = 1;
            int location = 0;
            if(remainingConversion.length() >= 4 && 
               windowConversion.contains(remainingConversion.substring(0, 4))){  
                for(int i = remaining.size()-1; i > 0; i--) {
                    if(windowConversion.contains(remainingConversion.substring(0,i))) {
                        location = windowConversion.indexOf(remainingConversion.substring(0,i));
                        size = remainingConversion.substring(0,i).length();
                        if(size >= 4) i = 0;
                    }
                }
            }
            
            addToAnswer(size, location);
        }
    }
    
    void addToAnswer(int size, int location) {
        if(size > 1) {
            // add to the answer arraylist
            answer.add((char)specialMarker); // marker
            answer.add((char)location);      // location of where repeated text occurs
            answer.add((char)size);          // size of repeated text
        } else {
            answer.add(origFile.get(endPos+1));
        }
        endPos += size; // move the position of thw window 
        startPos += size; // change where the editor looks--should be after the window
    }
    
    // GETTER METHOD
    ArrayList<Character> getCompressed() {
        return answer;
    }
}

class SlidingWindowDecompression {
    // ATTRIBUTES
    private ArrayList<Character> answer;
    private ArrayList<Character> origFile;
    private ArrayList<Character> window;
    private final int WINDOW_SIZE;      // WINDOW SIZE  
    private int currentPos;       // CURRENT POSITION MARKER
    private int endPos;         // MARK WHERE THE END IS
    
    // CONSTRUCTOR
    SlidingWindowDecompression(ArrayList<Character> origFile) {
        answer = new ArrayList<Character>();
        window = new ArrayList<Character>();
        this.origFile = origFile;
        WINDOW_SIZE = 30;
        currentPos = 0;
        endPos = origFile.size();
    }
    
    void decompress() {
        for(int i = 0; i < WINDOW_SIZE; i++) { // STORE FIRST x(WINDOW_SIZE) #'s INTO ANSWER ARRAYLIST
            answer.add(origFile.get(i)); // ADD TO ANSWmER ARRAY LIST   
            window.add(origFile.get(i)); // ADD THE WINDOW CHARACTERS
            currentPos++;
        }
        currentPos--;
        String answerStr = "";
        while(currentPos < endPos-1) { //can't reach end
            if(origFile.get(currentPos+1) != 7) { // if the position after the special marker is not a special marker
                char nextPos = origFile.get(currentPos+1);
                currentPos++;
                answer.add(origFile.get(currentPos));
                // SLIDE WINDOW
                window.remove(0);
                window.add(origFile.get(currentPos));
            } else {
                // if the character is a special marker
                int position = origFile.get(currentPos+2);
                int length = origFile.get(currentPos+3);
                
                // ADD TO ANSWER ARRAY LIST
                for(int i = 0; i < length; i++) {
                    answer.add(window.get(position+i));
                    window.add(window.get(position+i));
                }
                
                
                // UPDATE WINDOW
                for(int i = 0; i < length; i++) {
                    window.remove(0);
                    //window.add(orig
                }
                
                currentPos+=3;
            }
            // DEBUGGING PURPOSES
            answerStr = "";
            // Convert to String
            for(int i = 0; i < answer.size(); i++) {
                answerStr += answer.get(i);
            }
        }
    }
    
    // GETTER METHOD
    ArrayList<Character> getDecompressed() {
        return answer;
    }
}