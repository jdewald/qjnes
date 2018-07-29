package org.fortytwo.c64.cpu;

import org.fortytwo.c64.memory.Memory;

/**
 * Representation of a
 */
public abstract class Instruction {
    //public abstract int getCycles();
    public enum AddressingMode {
        Immediate(1), /* value is contained in instruction */
        Absolute(2), /* address of value in instruction */
        ZeroPageAbsolute(1), /* high byte of address is 00 */
        Implied(0),
        Accumulator(0),
        IndexedX(2), /* adds address to value in X or Y */
        IndexedY(2),
        ZeroPageIndexedX(1), /* same as previous but high byte is 00 */
        ZeroPageIndexedY(1),
        Indirect(2), /* only for JMP, points to address containing value */
        PreIndexedIndirect(1), /* adds 0-page address to contents of X to get value */
        PostIndexedIndirect(1), /* adds contents of 0-page address to Y to get value */
        Relative(1); /* similar to immediate */

        private final int bytes;

        AddressingMode(int bytes) {
            this.bytes = bytes;
        }

        public int getByteCount() {
            return bytes;
        }
    }

    ;

    //private int opcode;
    protected final AddressingMode addressMode;
    private String assembly;
    protected int cycles;
    /*    protected Instruction(int opcode, AddressingMode addressMode, String assembly){
    this.opcode = opcode;
	this.addressMode = addressMode;
	this.assembly = assembly;

	lookupTable.put(opcode, this);
    }
    */

    public Instruction(AddressingMode mode, String assembly) {
        this.addressMode = mode;
        this.assembly = assembly;
    }

    public Instruction(String assembly) {
        this(AddressingMode.Implied, assembly);
    }


    public String getAssembly() {
        return assembly;
    }

    public AddressingMode getAddressingMode() {
        return addressMode;
    }

    public abstract int execute(int[] operandData, Memory memory, CPU cpu);

    public int execute(int[] operandData, Memory memory, CPU cpu, boolean pageCrossed) {
        return this.execute(operandData, memory, cpu);
    }


    /*
     * True if this instruction is assumed to have no operands, defaults to false
     */
    public boolean isSingleByte() {
        return false;
    }

    public String getFullAssemblyLine(AddressingMode addressMode, int[] operandData) {
        StringBuffer buf = new StringBuffer();
        buf.append(getAssembly());
        buf.append(" ");

        switch (addressMode) {
            case Immediate:
                buf.append("#").append(hex(operandData));
                break;
            case Absolute:
            case ZeroPageAbsolute:
            case Relative:
                buf.append(hex(operandData));
                break;
            case ZeroPageIndexedX:
            case IndexedX:
                buf.append(hex(operandData)).append(", X");
                break;
            case IndexedY:
                buf.append(hex(operandData)).append(", Y");
                break;
            case Indirect:
                buf.append("(").append(hex(operandData)).append(")");
                break;
            case PreIndexedIndirect:
                buf.append("(").append(hex(operandData)).append(", X)");
                break;
            case PostIndexedIndirect:
                buf.append("(").append(hex(operandData)).append("), Y");
                break;
            case Implied: // do nothing
            case Accumulator:
                break;
        }
        return buf.toString();
    }

    void pushStack(CPU cpu, Memory memory, int val) {
        cpu.pushStack((byte) val);
        /*
        int sp = 0xFF & cpu.readRegister(RegisterType.stackPointer);
        memory.write(sp | 0x100, (val & 0xFF));
        sp--;
        cpu.writeRegister(RegisterType.stackPointer, sp);
        */
    }

    int popStack(CPU cpu, Memory memory) {
        return cpu.popStack();
        /*	int sp = cpu.readRegister(RegisterType.stackPointer);
    sp = (sp + 1) % 256;
	cpu.writeRegister(RegisterType.stackPointer, sp);
	return 0xFF & memory.read(sp | 0x100);
        */
    }

    public static int toInt(int[] lsb) {
        if (lsb.length == 1) {
            return lsb[0];
        } else {
            return lsb[0] | (lsb[1] << 8);
        }
    }

    public static String hex(int[] operandData) {
        if (operandData.length == 2) {
            return "$" + hex(operandData[1]) + hex(operandData[0]);
        } else {
            return "$" + hex(operandData[0]);
        }
    }


    public static String hex(int val) {
        if (Integer.toHexString(val).length() % 2 == 1) {
            return "0" + Integer.toHexString(val);
        } else {
            return Integer.toHexString(val);
        }
    }

}
