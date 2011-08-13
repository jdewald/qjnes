package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

import java.util.Hashtable;

/**
 * TODO: Think about better way to represent the instructions, they probably don't need to know
 * about their own opcode or even addressing mode. As long as they can get to their values they are good
 * Same as MOS6502 Instructions except ADC and SBC don't do BCD
 */
public class NESInstructionSet implements InstructionSet
{

    private static Instruction[] lookupTable = new Instruction[] {
	new BRK_Instruction(Instruction.AddressingMode.Implied), // 00
	new ORA_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 01
    new SKW_Instruction(Instruction.AddressingMode.Implied),
    new ASO_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 03
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 04
	new ORA_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 05
	new ASL_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 06
    new ASO_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 07
	new PHP_Instruction(Instruction.AddressingMode.Implied), // 08
	new ORA_Instruction(Instruction.AddressingMode.Immediate), // 09
	new ASL_Instruction(Instruction.AddressingMode.Accumulator), // 0A
    new ANC_Instruction(Instruction.AddressingMode.Immediate), // 0B
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // 0C - absolute = 2 bytes
	new ORA_Instruction(Instruction.AddressingMode.Absolute), // OD
	new ASL_Instruction(Instruction.AddressingMode.Absolute), // 0E
    new ASO_Instruction(Instruction.AddressingMode.Absolute), // 0F
	new BPL_Instruction(Instruction.AddressingMode.Relative), // 10
	new ORA_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 11
    null, // 12 - HLT
    new ASO_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 13
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 14
	new ORA_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // zero page, x 15
	new ASL_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // zero page ,x 16
    new ASO_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 17   
	new CLC_Instruction(Instruction.AddressingMode.Implied), // 18
	new ORA_Instruction(Instruction.AddressingMode.IndexedY), // 19
	new NOP_Instruction(Instruction.AddressingMode.Implied), // 1a
    new ASO_Instruction(Instruction.AddressingMode.IndexedY), // 1B
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // 1C - absolute = 2 bytes
	new ORA_Instruction(Instruction.AddressingMode.IndexedX), // 1D
	new ASL_Instruction(Instruction.AddressingMode.IndexedX), // 1E
    new ASO_Instruction(Instruction.AddressingMode.IndexedX), // 1F
	new JSR_Instruction(Instruction.AddressingMode.Absolute), // 20
	new AND_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 21
	null, // 22 - HLT
    new RLA_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 23  
	new BIT_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 24
	new AND_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 25
	new ROL_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 26
    new RLA_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 27
	new PLP_Instruction(Instruction.AddressingMode.Implied), // 28
	new AND_Instruction(Instruction.AddressingMode.Immediate), // 29
	new ROL_Instruction(Instruction.AddressingMode.Accumulator), // 2A
    new ANC_Instruction(Instruction.AddressingMode.Immediate), // 0B   
	new BIT_Instruction(Instruction.AddressingMode.Absolute), // 2C
	new AND_Instruction(Instruction.AddressingMode.Absolute), // 2D
	new ROL_Instruction(Instruction.AddressingMode.Absolute), // 2E
    new RLA_Instruction(Instruction.AddressingMode.Absolute), // 2F
	new BMI_Instruction(Instruction.AddressingMode.Relative), // 30
	new AND_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 31
	null, // 32 - HLT
    new RLA_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 33 
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 34
	new AND_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 35
	new ROL_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 36
    new RLA_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 37 
	new SEC_Instruction(Instruction.AddressingMode.Implied), // 38
	new AND_Instruction(Instruction.AddressingMode.IndexedY), // 39
	new NOP_Instruction(Instruction.AddressingMode.Implied), // 3A
    new RLA_Instruction(Instruction.AddressingMode.IndexedY), // 3B   
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // 3C - absolute = 2 bytes
	new AND_Instruction(Instruction.AddressingMode.IndexedX), // 3D
	new ROL_Instruction(Instruction.AddressingMode.IndexedX), // 3E
    new RLA_Instruction(Instruction.AddressingMode.IndexedX), // 3F
	new RTI_Instruction(Instruction.AddressingMode.Implied), // 40
	new EOR_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 41
	null, // 42 - HLT
    new LSE_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 43	
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 44
	new EOR_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 45
	new LSR_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 46
    new LSE_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 47
	new PHA_Instruction(Instruction.AddressingMode.Implied), // 48
	new EOR_Instruction(Instruction.AddressingMode.Immediate), // 49
	new LSR_Instruction(Instruction.AddressingMode.Accumulator), // 4A
	new ALR_Instruction(Instruction.AddressingMode.Immediate), // 4B
	new JMP_Instruction(Instruction.AddressingMode.Absolute), // 4C
	new EOR_Instruction(Instruction.AddressingMode.Absolute), // 4D
	new LSR_Instruction(Instruction.AddressingMode.Absolute), // 4E
    new LSE_Instruction(Instruction.AddressingMode.Absolute), // 4F
	new BVC_Instruction(Instruction.AddressingMode.Relative), // 50
	new EOR_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 51
	null, // 52 - HLT
    new LSE_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 53
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 54
	new EOR_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 55
	new LSR_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 56
    new LSE_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 57
	new CLI_Instruction(Instruction.AddressingMode.Implied), // 58
	new EOR_Instruction(Instruction.AddressingMode.IndexedY), // 59
	new NOP_Instruction(Instruction.AddressingMode.Implied), // 5A
    new LSE_Instruction(Instruction.AddressingMode.IndexedY), // 5B 
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // 5C - absolute = 2 bytes
	new EOR_Instruction(Instruction.AddressingMode.IndexedX),
	new LSR_Instruction(Instruction.AddressingMode.IndexedX),
    new LSE_Instruction(Instruction.AddressingMode.IndexedX), // 5F
  
	new RTS_Instruction(Instruction.AddressingMode.Implied), // 60
	new ADC_Instruction(Instruction.AddressingMode.PreIndexedIndirect,false), // 61
	null, // 62 - HLT
    new RRA_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 63	
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 64
	new ADC_Instruction(Instruction.AddressingMode.ZeroPageAbsolute,false), // 65
	new ROR_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 
    new RRA_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 67
	new PLA_Instruction(Instruction.AddressingMode.Implied), // 68
	new ADC_Instruction(Instruction.AddressingMode.Immediate,false), // 69
	new ROR_Instruction(Instruction.AddressingMode.Accumulator), // 6A
	new ARR_Instruction(Instruction.AddressingMode.Immediate), // 6B
	new JMP_Instruction(Instruction.AddressingMode.Indirect), // 6C
	new ADC_Instruction(Instruction.AddressingMode.Absolute,false), // 6D
	new ROR_Instruction(Instruction.AddressingMode.Absolute), // 6E
    new RRA_Instruction(Instruction.AddressingMode.Absolute), // 6F
	new BVS_Instruction(Instruction.AddressingMode.Relative), // 70
	new ADC_Instruction(Instruction.AddressingMode.PostIndexedIndirect,false), // 71
	null, // 72 - HLT
    new RRA_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 73	
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 74
	new ADC_Instruction(Instruction.AddressingMode.ZeroPageIndexedX,false), // 75
	new ROR_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 76
    new RRA_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 77   
	new SEI_Instruction(Instruction.AddressingMode.Implied), // 78
	new ADC_Instruction(Instruction.AddressingMode.IndexedY,false), // 79
	new NOP_Instruction(Instruction.AddressingMode.Implied), // 7A
    new RRA_Instruction(Instruction.AddressingMode.IndexedY), // 7B	
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // 7C - absolute = 2 bytes
	new ADC_Instruction(Instruction.AddressingMode.IndexedX,false), // 7D
	new ROR_Instruction(Instruction.AddressingMode.IndexedX), // 7E
    new RRA_Instruction(Instruction.AddressingMode.IndexedX), // 7F
	
	new SKB_Instruction(Instruction.AddressingMode.Immediate), // 80	
	new STA_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 81
	new SKB_Instruction(Instruction.AddressingMode.Immediate), // 82
	new AXS_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // 83	
	new STY_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 84
	new STA_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 85
	new STX_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 86
	new AXS_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // 87
	new DEY_Instruction(Instruction.AddressingMode.Implied), // 88
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // 89 should really do NOP with Immediate
	new TXA_Instruction(Instruction.AddressingMode.Implied), // 8A
	new XAA_Instruction(Instruction.AddressingMode.Immediate), // 8B
	new STY_Instruction(Instruction.AddressingMode.Absolute), // 8C
	new STA_Instruction(Instruction.AddressingMode.Absolute), // 8D
	new STX_Instruction(Instruction.AddressingMode.Absolute), // 8E
    new AXS_Instruction(Instruction.AddressingMode.Absolute), // 8F
	new BCC_Instruction(Instruction.AddressingMode.Relative), // 90
	new STA_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 91
	null, // 92 - HLT
	new AXA_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // 93
	new STY_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 94
	new STA_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // 95
	new STX_Instruction(Instruction.AddressingMode.ZeroPageIndexedY), // 96
	new AXS_Instruction(Instruction.AddressingMode.ZeroPageIndexedY), // 97
	new TYA_Instruction(Instruction.AddressingMode.Implied), // 98
	new STA_Instruction(Instruction.AddressingMode.IndexedY), // 99
	new TXS_Instruction(Instruction.AddressingMode.Implied), // 9A
	new TAS_Instruction(Instruction.AddressingMode.IndexedY), // 9B
	new SAY_Instruction(Instruction.AddressingMode.IndexedX), // 9C
	new STA_Instruction(Instruction.AddressingMode.IndexedX), // 9D
	new XAS_Instruction(Instruction.AddressingMode.IndexedY), // 9E
	new AXA_Instruction(Instruction.AddressingMode.IndexedY), // 9F
	new LDY_Instruction(Instruction.AddressingMode.Immediate), // A0 
	new LDA_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // A1
	new LDX_Instruction(Instruction.AddressingMode.Immediate), // A2
	new LAX_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // A3	
	new LDY_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // A4
	new LDA_Instruction(Instruction.AddressingMode.ZeroPageAbsolute),
	new LDX_Instruction(Instruction.AddressingMode.ZeroPageAbsolute),
	new LAX_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // A7  
	new TAY_Instruction(Instruction.AddressingMode.Implied), // A8
	new LDA_Instruction(Instruction.AddressingMode.Immediate), // A9
	new TAX_Instruction(Instruction.AddressingMode.Implied), // AA
	new OAL_Instruction(Instruction.AddressingMode.Immediate), // AB
	new LDY_Instruction(Instruction.AddressingMode.Absolute), // AC
	new LDA_Instruction(Instruction.AddressingMode.Absolute), // AD
	new LDX_Instruction(Instruction.AddressingMode.Absolute), // AE
	new LAX_Instruction(Instruction.AddressingMode.Absolute), // AF
	new BCS_Instruction(Instruction.AddressingMode.Relative), // B0
	new LDA_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // B1
	null, // B2 - HLT
	new LAX_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // B3	
	new LDY_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // B4
	new LDA_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // B5
	new LDX_Instruction(Instruction.AddressingMode.ZeroPageIndexedY), // B6
	new LAX_Instruction(Instruction.AddressingMode.ZeroPageIndexedY), // B7	
	new CLV_Instruction(Instruction.AddressingMode.Implied), // B8
	new LDA_Instruction(Instruction.AddressingMode.IndexedY), // B9
	new TSX_Instruction(Instruction.AddressingMode.Implied), // ba
	new LAS_Instruction(Instruction.AddressingMode.IndexedY), // bb
	new LDY_Instruction(Instruction.AddressingMode.IndexedX), // bc
	new LDA_Instruction(Instruction.AddressingMode.IndexedX), // bd
	new LDX_Instruction(Instruction.AddressingMode.IndexedY), // BE
	new LAX_Instruction(Instruction.AddressingMode.IndexedY), // BF	
	new CPY_Instruction(Instruction.AddressingMode.Immediate), // C0
	new CMP_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // C1
	new SKB_Instruction(Instruction.AddressingMode.Immediate), // C2
	new DCM_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // C3 		
	new CPY_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // C4
	new CMP_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // C5
	new DEC_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // C6
	new DCM_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // C7 	   
	new INY_Instruction(Instruction.AddressingMode.Implied), //C8
	new CMP_Instruction(Instruction.AddressingMode.Immediate), // c9
	new DEX_Instruction(Instruction.AddressingMode.Implied), // CA
	new SAX_Instruction(Instruction.AddressingMode.Immediate), // CB
	new CPY_Instruction(Instruction.AddressingMode.Absolute), // CC
	new CMP_Instruction(Instruction.AddressingMode.Absolute), // CD
	new DEC_Instruction(Instruction.AddressingMode.Absolute), // CE
	new DCM_Instruction(Instruction.AddressingMode.Absolute), // CF 
	new BNE_Instruction(Instruction.AddressingMode.Relative), // D0
	new CMP_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // D1
	null, // D2 - HLT
	new DCM_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // D3 		
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // D4
	new CMP_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // D5
	new DEC_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // D6
	new DCM_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // D7 		
	new CLD_Instruction(Instruction.AddressingMode.Implied), //D8
	new CMP_Instruction(Instruction.AddressingMode.IndexedY), //D9
	new NOP_Instruction(Instruction.AddressingMode.Implied), // DA
	new DCM_Instruction(Instruction.AddressingMode.IndexedY), // DB 	
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // DC - absolute = 2 bytes
	new CMP_Instruction(Instruction.AddressingMode.IndexedX), // DD
	new DEC_Instruction(Instruction.AddressingMode.IndexedX), // DE
	new DCM_Instruction(Instruction.AddressingMode.IndexedX), // DF	
	
	new CPX_Instruction(Instruction.AddressingMode.Immediate), // E0
	new SBC_Instruction(Instruction.AddressingMode.PreIndexedIndirect, false),
	new SKB_Instruction(Instruction.AddressingMode.Immediate), // E2
	new INS_Instruction(Instruction.AddressingMode.PreIndexedIndirect), // E3	
	new CPX_Instruction(Instruction.AddressingMode.ZeroPageAbsolute),
	new SBC_Instruction(Instruction.AddressingMode.ZeroPageAbsolute, false),
	new INC_Instruction(Instruction.AddressingMode.ZeroPageAbsolute),
	new INS_Instruction(Instruction.AddressingMode.ZeroPageAbsolute), // E7	
	new INX_Instruction(Instruction.AddressingMode.Implied),
	new SBC_Instruction(Instruction.AddressingMode.Immediate,false), // E9
	new NOP_Instruction(Instruction.AddressingMode.Implied), // EA
	new SBC_Instruction(Instruction.AddressingMode.Immediate, false), // EB
	new CPX_Instruction(Instruction.AddressingMode.Absolute), // EC
	new SBC_Instruction(Instruction.AddressingMode.Absolute, false), // ED
	new INC_Instruction(Instruction.AddressingMode.Absolute), // EE
	new INS_Instruction(Instruction.AddressingMode.Absolute), // EF	
	new BEQ_Instruction(Instruction.AddressingMode.Relative), // F0
	new SBC_Instruction(Instruction.AddressingMode.PostIndexedIndirect,false), //F1
	null, // F2 - HLT
	new INS_Instruction(Instruction.AddressingMode.PostIndexedIndirect), // F3	
    new SKB_Instruction(Instruction.AddressingMode.Immediate), // F4
	new SBC_Instruction(Instruction.AddressingMode.ZeroPageIndexedX,false),
	new INC_Instruction(Instruction.AddressingMode.ZeroPageIndexedX),
	new INS_Instruction(Instruction.AddressingMode.ZeroPageIndexedX), // F7
	new SED_Instruction(Instruction.AddressingMode.Implied), // f8
	new SBC_Instruction(Instruction.AddressingMode.IndexedY,false), // f9
	null,
	new INS_Instruction(Instruction.AddressingMode.IndexedY), // FB	
    new SKW_Instruction(Instruction.AddressingMode.Absolute), // FC - absolute = 2 bytes
	new SBC_Instruction(Instruction.AddressingMode.IndexedX,false),
	new INC_Instruction(Instruction.AddressingMode.IndexedX),
	new INS_Instruction(Instruction.AddressingMode.IndexedX) // FF
    };
 

    public static final int CLASS_MASK = 0x3; // determines our "class"
    public static final int MODE_MASK = 0x1C;
    public static final int INSTRUCTION_MASK = 0xE0;

    public static final int OPCODE_MASK = 0xFF0000;
    public static final int LOW_BYTE = 0x00FF00;
    public static final int HIGH_BYTE = 0x0000FF;

    public Instruction getByOpCode(int opcode){
	return lookupTable[opcode];
    }

    private static int[] operands = new int[2];
    public int executeInstruction(Instruction ins, int fullInstruction, Memory memory, CPU cpu){
	operands[0] = (fullInstruction & LOW_BYTE) >> 8;
	operands[1] = (fullInstruction & HIGH_BYTE);
	return ins.execute(operands, memory, cpu);
    }
    public static void loadInstructions(){
	// 
    }
    public static Instruction.AddressingMode getAddressingMode(int opcode){
	int mode = (opcode & MODE_MASK) >> 2;
	if (opcode == 0x20){ // JSR
	    return Instruction.AddressingMode.Absolute;
	}
	else if (opcode == 0x60){ // RTS
	    return Instruction.AddressingMode.Implied;
	}
	else if (opcode == 0x6C){ // JMP (indirect)
	    return Instruction.AddressingMode.Indirect;
	}
	switch (opcode & CLASS_MASK){
	case 2:
	    switch (mode){
	    case 0: return Instruction.AddressingMode.Immediate;
	    case 1: return Instruction.AddressingMode.ZeroPageAbsolute;
	    case 2: return Instruction.AddressingMode.Accumulator;
	    case 3: return Instruction.AddressingMode.Absolute;
	    case 5: return Instruction.AddressingMode.ZeroPageIndexedX;
	    case 7: return Instruction.AddressingMode.IndexedX;
	    } 
	case 1:
	    switch (mode){
	    case 0: return Instruction.AddressingMode.PreIndexedIndirect;
	    case 1: return Instruction.AddressingMode.ZeroPageAbsolute;
	    case 2: return Instruction.AddressingMode.Immediate;
	    case 3: return Instruction.AddressingMode.Absolute;
	    case 4: return Instruction.AddressingMode.PostIndexedIndirect;
	    case 5: return Instruction.AddressingMode.ZeroPageIndexedX;
	    case 6: return Instruction.AddressingMode.IndexedY;
	    case 7: return Instruction.AddressingMode.IndexedX;
	    }
	case 0:
	    switch (mode){
	    case 0: return Instruction.AddressingMode.Immediate;
	    case 1: return Instruction.AddressingMode.ZeroPageAbsolute;
	    case 3: return Instruction.AddressingMode.Absolute;
	    case 5: return Instruction.AddressingMode.ZeroPageIndexedX;
	    case 7: return Instruction.AddressingMode.IndexedX;

	    case 4:return Instruction.AddressingMode.Relative;
	    case 2:return Instruction.AddressingMode.Implied;
	    case 6:return Instruction.AddressingMode.Implied;
	    }
	}
   
	return null;
    }

}
