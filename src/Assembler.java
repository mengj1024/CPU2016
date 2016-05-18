import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ollie on 18/05/2016.
 */
public class Assembler {

    private ArrayList<String> program = new ArrayList<>();
    private HashMap<String, Integer> labels = new HashMap<>();

    private BufferedReader reader;
    private BufferedWriter writer;

    public Assembler(String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));

        scanLabels();
        reader.close();

        reader = new BufferedReader(new FileReader(filename));
        assemble();
        reader.close();

        String[] t = filename.split(".asm");
        String outputFile = t.length == 1 ? t[0] : filename;
        writer = new BufferedWriter(new FileWriter(outputFile+".lgs"));
        writer.write("v2.0 raw\n");
        for(String op : program) {
            writer.write(op + "\n");
        }
        writer.close();
    }

    private void scanLabels() throws IOException {
        String line;
        String[] tokens;
        int index = 0;

        while((line = reader.readLine()) != null) {
            if(line.matches(".*:")) {
                tokens = line.split(":");
                labels.put(tokens[0].trim(), index);
            } else {
                if(line.trim().length() == 0)
                    continue;
                index++;
            }
        }
    }

    private void assemble() throws IOException {
        String line;
        String[] tokens;
        String op = "nop", rd = "r0", rr = "r0";

        int index = 0;
        int lineNum = 0;

        while((line = reader.readLine()) != null) {
            lineNum++;

            if(line.matches(".*:"))
                continue;

            if(line.trim().length() == 0)
                continue;

            tokens = line.split(",");
            op = tokens[0].trim().split(" ")[0].trim().toLowerCase();
            try {
                rd = tokens[0].trim().split(" ")[1].trim().toLowerCase();
            } catch(ArrayIndexOutOfBoundsException e) {}
            try {
                rr = tokens[1].trim().trim().toLowerCase();
            } catch(ArrayIndexOutOfBoundsException e) {}

            switch(op) {
                case "nop":
                    program.add("0");
                    break;
                case "ldi":
                    program.add(Integer.toHexString(1 + (toRegIndex(rd)<<6) + (Integer.parseInt(rr)<<11)));
                    break;
                case "mov":
                    program.add(Integer.toHexString(2 + (toRegIndex(rd)<<6) + (toRegIndex(rr)<<11)));
                    break;
                case "add":
                    program.add(Integer.toHexString(3 + (toRegIndex(rd)<<6) + (toRegIndex(rr)<<11)));
                    break;
                case "sub":
                    program.add(Integer.toHexString(4 + (toRegIndex(rd)<<6) + (toRegIndex(rr)<<11)));
                    break;
                case "mul":
                    program.add(Integer.toHexString(5 + (toRegIndex(rd)<<6) + (toRegIndex(rr)<<11)));
                    break;
                case "div":
                    program.add(Integer.toHexString(6 + (toRegIndex(rd)<<6) + (toRegIndex(rr)<<11)));
                    break;
                case "neg":
                    program.add(Integer.toHexString(7 + (toRegIndex(rd)<<6) + (toRegIndex(rr)<<11)));
                    break;
                //TODO: Shift
                case "out":
                    program.add(Integer.toHexString(9 + (toRegIndex(rr)<<11) + (Integer.parseInt(rd)<<16)));
                    break;
                case "rjmp":
                    program.add(Integer.toHexString(10 + getRelativeJump(rd, index, lineNum)));
                    break;
                case "breq":
                    program.add(Integer.toHexString(11 + getRelativeJump(rd, index, lineNum)));
                    break;
                case "brne":
                    program.add(Integer.toHexString(12 + getRelativeJump(rd, index, lineNum)));
                    break;
                case "in":
                    program.add(Integer.toHexString(13 + (toRegIndex(rd)<<6) + (Integer.parseInt(rr)<<16)));
                    break;
                case "tst":
                    program.add(Integer.toHexString(14 + (toRegIndex(rd)<<6) + (toRegIndex(rd)<<11)));
                    break;
                case "hlt":
                    program.add(Integer.toHexString(15));
                    break;
                case "push":
                    program.add(Integer.toHexString(16 + (toRegIndex(rd)<<11)));
                    break;
                case "pop":
                    program.add(Integer.toHexString(17));
                    break;
                default:
                    break;
            }

            index++;
        }
    }

    private int toRegIndex(String r) {
        return Integer.parseInt(r.substring(1));
    }

    private int getRelativeJump(String label, int curIndex, int lineNumber) {
        if(labels.containsKey(label)) {
            int i = labels.get(label);
            i = i - curIndex;
            if(i > 32767 || i < -32767) {
                System.err.println("ERROR: Label " + label + " to far away on line " + lineNumber);
                System.exit(-1);
            } else {
                if(i < 0) {
                    i = -i;
                    i += (1 << 15);
                }
                return i<<16;
            }
        } else {
            System.err.println("ERROR: Label " + label + " does not exist on line " + lineNumber);
            System.exit(-1);
        }
        return 0;
    }

    public static void main(String[] args) throws IOException {
        new Assembler(args[0]);
    }

}
