import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Emulator {

    // Default 256
    static Instructions[] operations = new Instructions[256];
    static int[] values = new int[256];
    static boolean[] listening = new boolean[256];
    static Dictionary<String, Integer> finishedMRI;
    static boolean acReadout = true;

    public static ArrayList<String> loadFile(String file) {
        ArrayList<String> initialFile = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                initialFile.add(line);
            }
            br.close();
            return initialFile;
        } catch (Exception e) {
            System.out.println("File not found");
            e.printStackTrace();
            return null;
        }
    }

    public static Dictionary<String, Integer> firstPass(ArrayList<String> initialFile) {
        String line = initialFile.get(0); 

        Dictionary<String, Integer> MRI = new Hashtable<>();

        int LC = 0;
        int i = 0;

        System.out.println("FIRST PASS ------");
        while (!line.equals("END")) {
            line = initialFile.get(i);
            if (line.contains(",")) {
                // ADD to MRI
                System.out.println("Add to MRI \"" + line.split(",")[0] + "\" with Value " + LC);
                MRI.put(line.split(",")[0], LC);
            } if (line.contains("ORG")) {
                // ORG NUMBER so split at index 1
                System.out.println("LC NOW EQUALS " + line.split(" ")[1]);
                LC = Integer.parseInt(line.split(" ")[1]);
                i++;
                continue;
            }

            i ++;
            LC++;

            if(i > operations.length) {
                System.err.println("Out of memory (Maybe no END?)");
                break;
            }
        }
        return MRI;
    }

    public static void secondPass(Dictionary<String, Integer> MRI, ArrayList<String> initialFile) {
        String line = initialFile.get(0); 
        int LC = 0;
        int i = 0;

        System.out.println("SECOND PASS ----");


        while (!line.equals("END")) {

            line = initialFile.get(i);
            if (line.contains("ORG")) {
                // ORG NUMBER so split at index 1
                LC = Integer.parseInt(line.split(" ")[1]);
                i++;
                continue;
            } if (line.contains("DEC")) {
                if (line.contains(",")) {
                    System.out.println(line.split(" ")[2] + " " + LC);
                    values[LC] = Integer.parseInt(line.split(" ")[2]);
                } else {
                    values[LC] = Integer.parseInt(line.split(" ")[1]);
                }
                
            } if (line.contains("HEX")) {
                if (line.contains(",")) {
                    values[LC] = Integer.parseInt(line.split(" ")[2], 16);
                } else {
                    values[LC] = Integer.parseInt(line.split(" ")[1], 16);
                }
            }

            // Remove Comment
            String noComment[] = line.split("/");
            String toUse = noComment[0];

            // Remove Label if Exists
            if(toUse.contains(",")) {
                toUse = toUse.split(",")[1];
                toUse = toUse.substring(1);
            }

            // Now just instruction and value
            System.out.println(toUse);
            String split[] = toUse.split(" ");

            // Get Indirect Addressing var
            boolean indirect = false;
            if (split.length == 3 && split[2].equals("I")) {
                indirect = true;
            }

            // Log the instruction
            try {
                System.out.println("Add Instruction \"" + split[0] + "\" at LC " + LC + " with value " + MRI.get(split[1]));
            } catch (Exception e) {
                System.out.println("Add Instruction \"" + split[0] + "\" at LC " + LC);
            }
            switch(split[0]) {
                // case "AND":
                //     operations[LC] = indirect ? Instructions.ANDI : Instructions.AND;
                //     break;
                case "AND":
                    operations[LC] = indirect ? Instructions.ANDI : Instructions.AND ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "ADD":
                    operations[LC] = indirect ? Instructions.ADDI : Instructions.ADD ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "LDA":
                    operations[LC] = indirect ? Instructions.LDAI : Instructions.LDA ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "STA":
                    operations[LC] = indirect ? Instructions.STAI : Instructions.STA ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "BUN":
                    operations[LC] = indirect ? Instructions.BUNI : Instructions.BUN ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "BSA":
                    operations[LC] = indirect ? Instructions.BSAI : Instructions.BSA ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "ISZ":
                    operations[LC] = indirect ? Instructions.ISZI : Instructions.ISZ ;
                    values[LC] = MRI.get(split[1]);
                    break;
                case "CLA":
                    operations[LC] = Instructions.CLA;
                    break;
                case "CLE":
                    operations[LC] = Instructions.CLE;
                    break;
                case "CMA":
                    operations[LC] = Instructions.CMA;
                    break;
                case "CIL":
                    operations[LC] = Instructions.CIL;
                    break;
                case "INC":
                    operations[LC] = Instructions.INC;
                    break;
                case "SZA":
                    operations[LC] = Instructions.SZA;
                    break;
                case "HLT":
                    operations[LC] = Instructions.HLT;
                    break;
                default:
                    break;

            
            }

            i++;
            LC ++;
        }

        printMemory();
    }

    public static void run() {
        int max16bit = 65535;

        int AC = 0;
        int AR = 0;
        int PC = 0;
        int TR = 0;
        int DR = 0;
        int OUTR = 0;

        boolean E = false;

        Instructions IR = operations[PC];
        boolean stop = false;
        while (!stop) {
            IR = operations[PC];
            if(IR == null) {
                PC++;
                continue;
            }
            System.out.println("Executing Instruction " + IR + " at location " + PC + " with value " + values[PC]);
            
            switch (IR) {
                case AND:
                    System.out.println(AC + " AND " + values[values[PC]] + " EQUALS " + (AC & values[values[PC]]));
                    AC = (AC & values[values[PC]]);
                    break;

                case ADD:
                    AC = AC + values[values[PC]];
                    if (AC > max16bit) {
                        E = true;
                    }
                    break;

                case ADDI:
                    AC = AC + values[values[values[PC]]];
                    if (AC > max16bit) {
                        E = true;
                    }
                    break;
                
                case LDA:
                    AC = values[values[PC]];
                    break;
                
                case LDAI:
                    System.out.println("ADDRESS OF POINTER To LOAD = " + values[PC]);
                    AC = values[PC];
                    System.out.println("ADDRESS OF VALUE To LOAD = " + values[AC]);
                    AC = values[AC];
                    System.out.println("VALUE TO LOAD = " + values[AC]);
                    AC = values[AC] - 1;
                    break;

                case STA:
                    AR = values[PC];
                    values[AR] = AC;
                    broadcastChange(AR);
                    break;
                
                case STAI:
                    AR = values[PC];
                    values[values[AR]] = AC;
                    broadcastChange(AR);
                    break;

                case BUN:
                    PC = values[PC];
                    continue;
                
                case BUNI:
                    System.out.println("GOTO " + values[values[PC]]);
                    PC = values[values[PC]];
                    continue;
                
                case BSA:
                    System.out.println(values[PC]);
                    System.out.println(values[values[PC]] + " = " + PC);

                    values[values[PC]] = PC + 1;
                    PC = values[PC];
                    

                case ISZ:
                    values[values[PC]] ++;
                    if (values[values[PC]] == 0) {
                        PC ++;
                    }
                    break;

                case CLA:
                    AC = 0;
                    break;

                case CLE:
                    E = false;
                    break;
                
                case CMA:
                    AC = ~AC;
                    break;
                
                case CIL:
                    AC = AC << 1;
                    if (E) { AC++; }
                    break;

                case INC:
                    AC++;
                    break;

                case SZA:
                    if (AC == 0) {
                        PC ++;
                    }
                    break;

                case HLT:
                    stop = true;
                    break;

                default:
                    System.err.println("UNSUPPORTED INSTRUCTION");
                    break;
            }

            
            if (acReadout) {
                System.out.println("AC " + AC);
            }

            PC ++;
        }

        System.out.println("HALTED, AC IS " + AC);
    }

    public static void printMemory() {
        System.out.println("-- BEGIN MEMORY DUMP --");
        for (int i = 0; i < values.length; i++) {
            if(!(operations[i] == null)  || !(values[i] == 0)) {
                System.out.println(i + " Instruction " + operations[i] + " with Value " + values[i]);
            }

        }
        System.out.println("-- END MEMORY DUMP --");
    }

    public static void addListener(String var) {
        listening[finishedMRI.get(var)] = true;
        System.out.println("Listening on address " + finishedMRI.get(var));
    }

    public static void broadcastChange(int address) {
        //System.out.println("S " + address);
        if (listening[address]) {
            System.out.println("Address " + address + " = " + values[address]);
        }
    }

    public static void main(String[] args) {
        // ARG 0 = File
        // ARG 1 = Mem Size
        //String file = args[0];
        //int memSize = Integer.parseInt(args[1]);
        //memory = new int[memSize];

        //ArrayList<String> initialFile = loadFile("multiply.asm");
        //ArrayList<String> initialFile = loadFile("subtractdoubleprecision.asm");
        //ArrayList<String> initialFile = loadFile("xor.asm");
        ArrayList<String> initialFile = loadFile("subtractwithsubroutine.asm");

        System.out.println(initialFile.get(3));

        
        Dictionary<String, Integer> fp = firstPass(initialFile);
        finishedMRI = fp;
        secondPass(fp, initialFile);

        run();

    }
}
