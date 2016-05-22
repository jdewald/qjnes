package org.fortytwo.c64;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.fortytwo.c64.cpu.Instruction;
import org.fortytwo.c64.cpu.InstructionSet;

/**
 * 6502/6510 Disasembler
 */
public class Disassembler
{
    InstructionSet instructionSet;
    public Disassembler(InstructionSet instructionSet){
        this.instructionSet = instructionSet;
    }
    
    public void disassemble(DataInputStream dataIn) throws IOException{
	// read byte
	while (dataIn.available() > 0){
	    int opcode = dataIn.readUnsignedByte();
	    
	    // determine opcode
	    // fetch instruction based on opcode
	    Instruction instruction = instructionSet.getByOpCode(opcode);
	    if (instruction == null){
		System.err.println("Unknown opcode: " + Integer.toHexString(opcode));
		return;
	    }
	    Instruction.AddressingMode mode = instruction.getAddressingMode();
	    if (mode != null){
		// based on addressing mode, read either 0,1, or 2 bytes
		int numBytes = mode.getByteCount();
		int[] operands = new int[numBytes];
		if (numBytes > 0){
		    for (int i = 0; i < numBytes; i++){
			operands[i] = dataIn.readUnsignedByte();
		    }
		}
		System.out.println(instruction.getFullAssemblyLine(mode,operands));
	    }
	    else {
		System.err.println("Unable to determine addressing mode");
		return;
	    }
	    // emit assembly

	}
    }
    
    public static void main(String argv[]){
	DataInputStream dataIn = null;
	try {
	    File file = new File(argv[0]);
	    dataIn = new DataInputStream(new FileInputStream(file));

	    Disassembler disassembler = new Disassembler(new org.fortytwo.c64.cpu.MOS6502InstructionSet());
	    disassembler.disassemble(dataIn);
	}
	catch (Throwable t){
	    t.printStackTrace();
	}
	finally {
	    if (dataIn != null){
		try {
		    dataIn.close();
		}
		catch (IOException e) {}
	    }
	    Runtime.getRuntime().exit(0);
	}
    }
}
