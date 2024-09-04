import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class InstructionConverter {

    HashMap<String, String> InstructionCodeMap;
    HashMap<String, Byte> OpcodeMap;
    HashMap<String, Byte> RegisterMap;
    HashMap<String, Byte> LabelMap;

    ArrayList<String> Rformat;
    ArrayList<String> Sformat;
    ArrayList<String> Iformat;
    ArrayList<String> Jformat;
    ArrayList<String> StackFormat;

    InstructionConverter() {

        InstructionCodeMap = new HashMap<String, String>();
        OpcodeMap = new HashMap<String, Byte>();
        RegisterMap = new HashMap<String, Byte>();
        LabelMap = new HashMap<String, Byte>();

        Rformat = new ArrayList<String>();
        Sformat = new ArrayList<String>();
        Iformat = new ArrayList<String>();
        Jformat = new ArrayList<String>();
        StackFormat = new ArrayList<String>();

    }

    public void SpecificationLoader(){
        RegisterLoader();
        InstructionCodeLoader();
        InstructionOpcodeLoader();
        InstructionFormatLoader();
        LabelMapLoader();
    } 

    public void RegisterLoader(){

        byte registerCode = 0b0000; RegisterMap.put("$zero", registerCode);
        registerCode++; RegisterMap.put("$t0", registerCode);
        registerCode++; RegisterMap.put("$t1", registerCode);
        registerCode++; RegisterMap.put("$t2", registerCode);
        registerCode++; RegisterMap.put("$t3", registerCode);
        registerCode++; RegisterMap.put("$t4", registerCode);
        registerCode++; RegisterMap.put("$sp", registerCode);

    }

    public void InstructionCodeLoader(){
        try {
            File file = new File("instructionOpcode.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String commandLine;
            while((commandLine=br.readLine())!=null){
                String[] instructionCodeParams = this.commandTokenizer(commandLine);
                InstructionCodeMap.put(instructionCodeParams[0], instructionCodeParams[1]);
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void InstructionOpcodeLoader(){

        String SerialOpcode = "HCAGNMIJDBOFEPKL";
        int length = SerialOpcode.length();

        String instruction;
        byte instructionOpcode = 0b0000;
        for(int i=0; i<length; i++){

            instruction = InstructionCodeMap.get( String.valueOf(SerialOpcode.charAt(i)) );
            OpcodeMap.put(instruction, instructionOpcode);
            instructionOpcode++;

        }

    }

    public void InstructionFormatLoader(){

        Rformat.add("add"); Rformat.add("sub");
        Rformat.add("and"); Rformat.add("or");
        Rformat.add("nor");

        Sformat.add("sll"); Sformat.add("srl");

        Iformat.add("addi"); Iformat.add("subi");
        Iformat.add("andi"); Iformat.add("ori");

        Iformat.add("sw"); Iformat.add("lw");
        Iformat.add("beq"); Iformat.add("bneq");

        Jformat.add("j");

        StackFormat.add("push");
        StackFormat.add("pop");

    }

    public void LabelMapLoader(){

        try {

            File file = new File("MIPS_instructions.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            String commandLine;
            Byte line_count = 0;
            while((commandLine=br.readLine())!=null){

                line_count++;
                String[] instructionParams = this.commandTokenizer(commandLine);
                if(instructionParams[0].endsWith(":")){
                    line_count++;
                    LabelMap.put(instructionParams[0].substring(0, instructionParams[0].length()-1), line_count);
                    line_count--;
                }

            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }  

    }

    //Handling different types of instruction formats
    //Reading MIPS instructions
    public void convertMIPSinstructions(BufferedWriter bufferedWriter){

        try {

            File file = new File("MIPS_instructions.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            String commandLine;
            int count = 0;
            while((commandLine=br.readLine())!=null){

                //System.out.println(commandLine);
                String[] instructionParams = this.commandTokenizer(commandLine); count++;
                
                if(Rformat.contains(instructionParams[0])) convertRformat(instructionParams, bufferedWriter);
                else if (Iformat.contains(instructionParams[0])) convertIformat(instructionParams, bufferedWriter);
                else if (Sformat.contains(instructionParams[0])) convertSformat(instructionParams, bufferedWriter);
                else if (Jformat.contains(instructionParams[0])) convertJformat(instructionParams, bufferedWriter);
                else if(StackFormat.contains(instructionParams[0])) convertStackFormat(instructionParams, bufferedWriter);
                else if(instructionParams[0].endsWith(":")) {}
                else System.out.println("Instruction " + count + " is invalid");
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }        

    }

    //R-type Instructions
    public void convertRformat(String[] instructionParams, BufferedWriter bufferedWriter){

        //System.out.println("Rformat");
        //for(String s : instructionParams) System.out.println(s);
        Byte opcode = OpcodeMap.get(instructionParams[0]);
        Byte src1 = RegisterMap.get(instructionParams[1].substring(0, instructionParams[1].length()-1));
        Byte src2 = RegisterMap.get(instructionParams[2].substring(0, instructionParams[2].length()-1));
        Byte dst = RegisterMap.get(instructionParams[3]);

        System.out.println("0x"+makeHexFourBits(opcode, src1, src2, dst)+", ");

    }

    //Stack Instructions
    public void convertStackFormat(String[] instructionParams, BufferedWriter bufferedWriter){

        //System.out.println("Stack Operation");
        if( instructionParams[0].equalsIgnoreCase("push") ){
            
            String[] stackParameters = instructionParams[1].split("\\(");
            
            if(stackParameters.length == 1)
            {
                String src = instructionParams[1];
                System.out.println(src);
                Byte baseRegister = RegisterMap.get("$sp");
                Byte dst = RegisterMap.get(src);
                Byte opcode = OpcodeMap.get("sw");
                System.out.println("0x"+makeHexFourBits(opcode, baseRegister, dst, Byte.valueOf((byte)0))+", ");

                Byte opcode2 = OpcodeMap.get("subi");
                Byte dst2 = RegisterMap.get("$sp");
                Byte src2 = dst2;
                Byte immediate = Byte.valueOf((byte)1);
                System.out.println("0x"+makeHexFourBits(opcode2, src2, dst2, immediate)+", ");

            }
            else if(stackParameters.length == 2)
            {
                Byte offset = Byte.parseByte(stackParameters[0]);
                String src = instructionParams[1].substring(2, instructionParams[1].length()-1);
                Byte baseRegister = RegisterMap.get(src);
                Byte dst = RegisterMap.get(src);
                Byte opcode = OpcodeMap.get("lw");
                System.out.println("0x"+makeHexFourBits(opcode, baseRegister, dst,offset)+", ");
                Byte baseRegister2 = RegisterMap.get("$sp");
                Byte opcode2=OpcodeMap.get("sw");
                System.out.println("0x"+makeHexFourBits(opcode2, baseRegister2, dst,Byte.valueOf((byte)0))+", ");

                Byte opcode3 = OpcodeMap.get("subi");
                Byte dst3 = RegisterMap.get("$sp");
                Byte src3 = dst3;
                Byte immediate = Byte.valueOf((byte)1);
                System.out.println("0x"+makeHexFourBits(opcode3, src3, dst3, immediate)+", ");
            }
        }
        else if( instructionParams[0].equalsIgnoreCase("pop") ){
            String src = instructionParams[1];
            System.out.println(src);

            Byte opcode2 = OpcodeMap.get("addi");
            Byte dst2 = RegisterMap.get("$sp");
            Byte src2 = dst2;
            Byte immediate = Byte.valueOf((byte)1);
            System.out.println("0x"+makeHexFourBits(opcode2, src2, dst2, immediate)+", ");

            Byte baseRegister = RegisterMap.get("$sp");
            Byte dst = RegisterMap.get(src);
            Byte opcode = OpcodeMap.get("lw");
            System.out.println("0x"+makeHexFourBits(opcode, baseRegister, dst, Byte.valueOf((byte)0))+", ");
        }

    }

    //I-type Instructions
    public void convertIformat(String[] instructionParams, BufferedWriter bufferedWriter){

        //System.out.println("Iformat");
        Byte opcode = OpcodeMap.get(instructionParams[0]);
        Byte src1 = RegisterMap.get(instructionParams[1].substring(0, instructionParams[1].length()-1));
        Byte src2 = RegisterMap.get(instructionParams[2].substring(0, instructionParams[2].length()-1));

        if( instructionParams[0].equalsIgnoreCase("sw") ){
            //store word
            Byte dst = src1;
            String[] storeWordParams = instructionParams[2].split("\\(");
            Byte offset = Byte.parseByte(storeWordParams[0]);
            Byte baseRegister = RegisterMap.get(storeWordParams[1].substring(0, storeWordParams[1].length()-1));
            
            System.out.println("0x"+makeHexFourBits(opcode, baseRegister, dst, offset)+", ");
        }
        else if( instructionParams[0].equalsIgnoreCase("lw") ){
            //load word
            Byte dst = src1;
            String[] loadWordParams = instructionParams[2].split("\\(");
            Byte offset = Byte.parseByte(loadWordParams[0]);
            Byte baseRegister = RegisterMap.get(loadWordParams[1].substring(0, loadWordParams[1].length()-1));

            System.out.println("0x"+makeHexFourBits(opcode, baseRegister, dst, offset)+", ");
        }
        else if( instructionParams[0].equalsIgnoreCase("beq") ){
            //store word, load word
            Byte address = Byte.parseByte(instructionParams[3]);

            System.out.println("0x"+makeHexFourBits(opcode, src1, src2, address)+", ");
        }
        else if( instructionParams[0].equalsIgnoreCase("bneq") ){
            //store word, load word
            Byte address = Byte.parseByte(instructionParams[3]);

            System.out.println("0x"+makeHexFourBits(opcode, src1, src2, address)+", ");
        }
        else{
            //Operations with constants  addi, subi, andi, ori
            Byte dst = src1;
            Byte address = Byte.parseByte(instructionParams[3]);
            Byte immediate = address;

            System.out.println("0x"+makeHexFourBits(opcode, src2, dst, immediate)+", ");
        }

    }

    //S-type Instructions
    public void convertSformat(String[] instructionParams, BufferedWriter bufferedWriter){

        //System.out.println("Sformat");
        //for(String s : instructionParams) System.out.println(s);

        Byte opcode = OpcodeMap.get(instructionParams[0]);
        Byte src1 = RegisterMap.get(instructionParams[1].substring(0, instructionParams[1].length()-1));
        Byte dst = RegisterMap.get(instructionParams[2].substring(0, instructionParams[2].length()-1));
        Byte shamt = Byte.parseByte(instructionParams[3]);

        System.out.println("0x"+makeHexFourBits(opcode, src1, dst, shamt)+", ");

    }

    //J-type Instructions
    public void convertJformat(String[] instructionParams, BufferedWriter bufferedWriter){
        
        //System.out.println("Jformat");
        Byte opcode = OpcodeMap.get(instructionParams[0]);

        Byte zeroByte = 0b00000000;
        Byte address = LabelMap.get(instructionParams[1]);
        if(address < 16) System.out.println("0x"+makeHexFourBits(opcode, zeroByte, address, zeroByte)+", ");
        else System.out.println("0x"+makeHexOneBit(opcode)+makeHexOneBit(address)+makeHexOneBit(zeroByte)+", ");

    }


    //Binary and Hex Converter codes
    public String makeBinaryFourBits(byte binaryNumber){
        return String.format("%4s", Integer.toBinaryString(binaryNumber)).replace(' ', '0');
    }
    public String makeHexOneBit(byte binaryNumber){
        return String.format("%X", binaryNumber);
    }
    public String makeHexFourBits(byte binaryNumber1, byte binaryNumber2, byte binaryNumber3, byte binaryNumber4){

        String hexCode1 = String.format("%X", binaryNumber1);
        String hexCode2 = String.format("%X", binaryNumber2);
        String hexCode3 = String.format("%X", binaryNumber3);
        String hexCode4 = String.format("%X", binaryNumber4);

        return hexCode1+hexCode2+hexCode3+hexCode4;

    }

    //Commad tokenizer function
    public String[] commandTokenizer(String commandLine){
        String[] commandParams = commandLine.split(" ");  
        return commandParams;
    }

    public static void main(String[] args) {

        String fileName = "instructions_output.txt"; // Name of the file to write
        
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            
            InstructionConverter converter = new InstructionConverter();
            converter.SpecificationLoader();
            converter.convertMIPSinstructions(bufferedWriter);

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
}

//javac InstructionConverter.java
//java InstructionConverter
