package org.fortytwo.common.cpu;

import org.fortytwo.common.exceptions.InstructionException;
import org.fortytwo.common.util.Ringbuffer;
import org.fortytwo.common.util.PRGFile;
import org.fortytwo.common.util.StringUtil;

import java.io.File;

import org.fortytwo.common.memory.Memory;
import org.fortytwo.common.memory.ROM;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Emulatores a MOS6502/6510
 * TODO: Write separate CPU monitor that sits on top of CPU
 * -- can read from all registers already
 */
public class MOS6502Emulator implements CPU {
    public static final int INITIAL_ADDRESS = 0xFFFC; // pointer to reset vector in kernel
    public static final int IRQ_VECTOR_ADDRESS = 0xFFFE;
    public static final int NMI_ADDRESS = 0xFFFA;

    public static final int REGISTER_COUNT = 6;
    private boolean interruptsDisabled;
    private boolean carryFlag;
    private boolean decimalFlag;
    private boolean zeroFlag;
    private boolean signFlag;
    private boolean overflowFlag;
    private boolean breakFlag;

    public final Object mutex = new Object();
    public static final int STATUS_FLAG_CARRY = 0x1;
    public static final int STATUS_FLAG_ZERO = 0x2;
    public static final int STATUS_FLAG_INTERRUPT = 0x4;
    public static final int STATUS_FLAG_DECIMAL = 0x8;
    public static final int STATUS_FLAG_BREAK = 0x10;
    public static final int STATUS_FLAG_UNUSED = 0x20;
    public static final int STATUS_FLAG_OVERFLOW = 0x40;
    public static final int STATUS_FLAG_SIGN = 0x80;

    public static final int LOAD_RAM_ADDRESS = 0xF49E; // LOAD RAM from DEVICE
    public static final int SCROLL_SCREEN_ADDRESS = 0xE8EA;
    public static final int OPEN_DEVICE_ADDRESS = 0xF3D5;
    //    private int operands[] = new int[2]; // so we don't have to keep creating this...does it matter?
    public static final int EMULATED_CLOCK_SPEED = 1022700;
    private int[] registers = new int[REGISTER_COUNT];

    // Common addresses
    private static final int ADDRESS_CURSOR_LINE_NUMBER = 0xD6;
    private static final int ADDRESS_CURSOR_INPUT_X = 0xC9;
    private static final int ADDRESS_LINE_NUMBER_TEMP = 0x2A5;

    private int breakAddress = -1;

    private int nmiDelay = 0; // how many instructions to let run before we actually do the NMI
    boolean keepRunning = true;
    boolean inBreakPoint = false;

    double elapsedTime = 0.0;
    long fetchElapsed;
    long decodeElapsed = 0;
    long executeElapsed = 0;
    long inputElapsed = 0;
    long loopCount = 0;
    long cycles = 0;

    private boolean nmiTriggered = false;
    private boolean irqTriggered = false;

    public boolean restart = true;
    private ArrayList<CycleObserver> observers;
    boolean shouldLog = true;
    //private Hashtable<RegisterType,Integer> registers;
    private Logger logger;
    private PrintWriter instWriter;
    private Memory memory;
    private Ringbuffer<InstructionBean> instructionBuffer;
    private InstructionSet instructionSet;

    public MOS6502Emulator(InstructionSet instructionSet) {
        logger = Logger.getLogger(this.getClass().getName());
        try {
            instWriter = new PrintWriter(new java.io.File("./instructions.log"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //registers = new Hashtable<RegisterType,Integer>(RegisterType.values().length);
        interruptsDisabled = false;
        carryFlag = false;
        decimalFlag = false;
        zeroFlag = false;
        signFlag = false;
        overflowFlag = false;
        observers = new ArrayList<>();
        instructionBuffer = new Ringbuffer(20);
        this.instructionSet = instructionSet;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void registerCycleObserver(CycleObserver observer) {
        observers.add(observer);
    }

    public void setBreak(int address) {
        if (address == -1) {
            inBreakPoint = true;
        } else {
            this.breakAddress = address;
        }
    }

    public void handleNMI(int cycleOffset) {
        if (cycleOffset == 0) {
            nmiDelay = 1;
        }
        //System.out.println("Cycle Offset: " + cycleOffset);
        //setSignFlag(true);
        //        inBreakPoint = true;
        nmiTriggered = true;
    }

    public void restart() {
        restart = true;
        keepRunning = false;
    }

    public void run() {
        if (memory == null) {
            throw new RuntimeException("Memory must be initialized!");
        }

        //        InstructionSet.loadInstructions(); // [REMOVE] can take a bit to instantiate 255 objects
        while (restart) {
            restart = false;
            logger.info("*** RESTARTING");
            writeRegister(RegisterType.programCounter, memory.readWord(INITIAL_ADDRESS));
            //writeRegister(RegisterType.programCounter, 0xC000);  // For NESTEST
            writeRegister(RegisterType.status, 0x24);
            logger.info("Restarting at: " + Integer.toHexString(readRegister(RegisterType.programCounter)));
            var operands = new int[2];
            long start = 0;
            long end = 0;
            long fetchStart = 0;
            long fetchEnd = 0;
            boolean skipped = false;
            int cyclesUntilInterrupt = 0;
            int skippedCycles = 0;
            boolean interrupted = false;
            keepRunning = true;
            while (keepRunning) {
                if (inBreakPoint) {
                    System.err.println("[-------- START CYCLE -------------]");
                }
                start = 1;

                int pc = readRegister(RegisterType.programCounter);

                if (pc == breakAddress) {
                    inBreakPoint = true;
                }

                /** FETCH INSTRUCTION **/
                fetchStart = 1;
                int opcode = 0xFF & memory.read(pc);
                //* [REMOVE]
                Instruction instruction = instructionSet.getByOpCode(opcode);
                fetchEnd = 1;
                fetchElapsed += (fetchEnd - fetchStart);
                if (instruction == null) {
                    System.err.println("Unknown opcode: " + Integer.toHexString(opcode));
                    instruction = instructionSet.getByOpCode(0xEA); // NOP
                    //opcode = 0xEA;

                    InstructionBean trace = (InstructionBean) instructionBuffer.getFirst();
                    while (trace != null) {
                        trace.display();
                        trace = (InstructionBean) instructionBuffer.getNext();
                    }
                    //inBreakPoint = true;
                    //return;

                }
                //* [/REMOVE]
                //if (shouldLog && logger.isLoggable(Level.FINEST))
                //logger.finest(instruction.getAssembly());

                /** "DEOODE" OPERANDS **/
                Instruction.AddressingMode mode = instruction.getAddressingMode(); // [REPLACE]:$mode

                //if (shouldLog && logger.isLoggable(Level.FINEST))
                //logger.finest(mode.toString());

                // we always use the same operand data
                operands[0] = 0;
                operands[1] = 0;
                //int instructionData = opcode << 16;
                int numBytes = mode.getByteCount();
                //	    int[] operands = new int[numBytes];
                if (numBytes > 0) {
                    if (numBytes == 1) {
                        operands[0] = memory.read(pc + 1);
                    } else if (numBytes == 2) {
                        int val = memory.readWord(pc + 1);
                        operands[0] = val & 0xFF;
                        operands[1] = (val & 0xFF00) >> 8;
                    }
                /*
                  for (int i = 1; i <= numBytes; i++){
                  //instructionData |= ((0xFF & memory.read(pc + i)) << (8 * (2- i)));
                  operands[i-1] = 0xFF & memory.read(pc + i);
                  }
                */

                }

                // REMOVE]
                var instructionBean = new InstructionBean(pc, opcode, instruction, operands); // [REMOVE]
			/*instWriter.print(instructionBean.toString());
			instWriter.print("\t");
			instWriter.print(nesTestRegisters());
			instWriter.println();
			instWriter.flush();
			*/
                //instLogger.info(instructionBean.toString());
            /*            if (pc == LOAD_RAM_ADDRESS){ // intercept BASIC load's call to LOAD RAM
                System.out.println("Intercepting LOADRAM");
                handleLoadRAMFromDevice();
                //                inBreakPoint = true;
                instructionBean = new InstructionBean(pc, instructionSet.getByOpCode(0x60), new int[2]); // [REMOVE]
                instruction = instructionBean.instruction; // [REMOVE]
                opcode = 0x60;
                //	    return bean;
            }
            */
            /*
            else if (pc == SCROLL_SCREEN_ADDRESS){
                System.out.println("Intercepted Scroll");
                handleScrollScreen();
                
                instructionBean = new InstructionBean(pc, instructionSet.getByOpCode(0x60), new int[2]); // [REMOVE]
                instruction = instructionBean.instruction; // [REMOVE]
                opcode = 0x60;
            }
            */
                //* [REMOVE
                //	InstructionBean instructionBean = fetchAndDecodeInstruction(pc);

                pc += 1 + numBytes; // move our PC to the next instruction
                writeRegister(RegisterType.programCounter, pc); // this can get overwritten by the instruction

                long decodeStart = 1;
                boolean crossed = false;
                // now, we apply the addressing mode (basically , follow any indirects or indexes)
                if (numBytes > 0) {
                    crossed = applyIndexing(mode, operands, pc);
                /*skippedCycles++;
                cyclesUntilInterrupt = notifyObservers(skippedCycles);
                
                if (nmiTriggered){
                    nmiTriggered = false;

                    setupNMI();
                }*/

                }

                long decodeEnd = 1;
                decodeElapsed += (decodeEnd - decodeStart);

                //	    InstructionBean instructionBean = fetchAndDecodeInstruction(pc);
                instructionBuffer.add(instructionBean); // [REMOVE]

                if (inBreakPoint) {
                    end = 1;
                    elapsedTime += (end - start);

                    instructionBean.display(); // [REMOVE]
                    //		System.out.println(Integer.toHexString(pc) + " " + instructionBean.instruction.getFullAssemblyLine(mode,instructionBean.operands));

                    skipped = handleDebuggingBreakPoint();
                    start = 1;
                }


                if (!skipped) {
                    try {
                        long executeStart = 1;
                        //int cycles_ = instructionBean.instruction.execute(operands, memory, this);

                        boolean _interruptsDisabled = interruptsDisabled;
                        int cycles_ = instruction.execute(operands, memory, this, crossed); // [REPLACE]:$execute
                        if (nmiDelay > 0) {
                            nmiDelay--;
                        }
                        cycles += cycles_;

                        //                    if (instruction instanceof BRK_Instruction){
                        //  inBreakPoint = true;
                        //}

                        executeElapsed += (1 - executeStart);

                        skippedCycles += cycles_;
                        if (numBytes > 1) {
                            //	skippedCycles--;
                        }

                        if (!nmiTriggered) cyclesUntilInterrupt = notifyObservers(skippedCycles);
                        if (nmiTriggered && nmiDelay <= 0) {
                            nmiTriggered = false;
                            nmiDelay = 0;

                            setupNMI();
                        } else if ((!interruptsDisabled || !_interruptsDisabled) && irqTriggered) { // basically doing a JSR to the jump vector
                            if (inBreakPoint) {
                                System.out.println("[--- START IRQ ---]");
                            }
                            setupInterrupt();

                            if (inBreakPoint) {
                                System.out.println("[--- END IRQ   ---]");
                            }

                        }
                        irqTriggered = false;
                        //interrupted = false;

                        //if (skippedCycles >= cyclesUntilInterrupt){

                        //                        cyclesUntilInterrupt = notifyObservers(skippedCycles);
                        //    logger.info("Cycles Until Interrupt: " + cyclesUntilInterrupt);
                        //                        if (cyclesUntilInterrupt == 0){
                        //    interrupted = true;
                        //}
                        skippedCycles = 0;

                        //}

                    } catch (InstructionException e) {
                        e.printStackTrace();

                        //* [REMOVE]
                        InstructionBean trace = (InstructionBean) instructionBuffer.getFirst();
                        while (trace != null) {
                            trace.display();
                            trace = (InstructionBean) instructionBuffer.getNext();
                        }
                        //* [/REMOVE]
                        return;
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        handleDebuggingBreakPoint();
                    }

                }
                skipped = false;
                end = 1;
                elapsedTime += (end - start);

                // OLD IRQ LOCATION
                loopCount = 1;

                if (inBreakPoint) {
                    System.err.println("[---------- END CYCLE ---------------]");
                }
            }
            logger.info("** EXITED MAIN LOOP");
        }
    }

    //* [INSERT]:$instructions
    public void handleInterrupt() {
        irqTriggered = true;
    }

    public void setupInterrupt() {
        //        setInterruptsDisabled(true);

        int irqAddress = memory.readWord(IRQ_VECTOR_ADDRESS);
        int progCounter = readRegister(RegisterType.programCounter);

        pushStack((byte) ((progCounter & 0xFF00) >> 8));
        pushStack((byte) ((progCounter & 0xFF)));
        int status = getCarryFlag() ? STATUS_FLAG_CARRY : 0x00;
        status |= getZeroFlag() ? STATUS_FLAG_ZERO : 0x00;
        status |= getDecimalFlag() ? STATUS_FLAG_DECIMAL : 0x00;
        status |= getOverflowFlag() ? STATUS_FLAG_OVERFLOW : 0x00;
        status |= getSignFlag() ? STATUS_FLAG_SIGN : 0x00;
        //status |= getBreakFlag() ? STATUS_FLAG_BREAK : 0x00;
        //status |= STATUS_FLAG_INTERRUPT;
        pushStack((byte) status);

        writeRegister(RegisterType.programCounter, irqAddress);

        //inBreakPoint = true;
        //handleDebuggingBreakPoint();

    }

    public void setupNMI() {
        //        setInterruptsDisabled(true);

        int irqAddress = memory.readWord(NMI_ADDRESS);
        int progCounter = readRegister(RegisterType.programCounter);

        pushStack((byte) ((progCounter & 0xFF00) >> 8));
        pushStack((byte) ((progCounter & 0xFF)));
        int status = getCarryFlag() ? STATUS_FLAG_CARRY : 0x00;
        status |= getZeroFlag() ? STATUS_FLAG_ZERO : 0x00;
        status |= getDecimalFlag() ? STATUS_FLAG_DECIMAL : 0x00;
        status |= getOverflowFlag() ? STATUS_FLAG_OVERFLOW : 0x00;
        status |= getSignFlag() ? STATUS_FLAG_SIGN : 0x00;
        status |= getInterruptsDisabled() ? STATUS_FLAG_INTERRUPT : 0x00;
        //status |= getBreakFlag() ? STATUS_FLAG_BREAK : 0x00;
        // |= STATUS_FLAG_INTERRUPT;
        pushStack((byte) status);

        writeRegister(RegisterType.programCounter, irqAddress);

        //inBreakPoint = true;
        //handleDebuggingBreakPoint();

    }

    private int notifyObservers(int cycles) {
        //	logger.info("Notifying observers of " + cycles + " cycles" );
        int untilInterrupt = 9999999;
        for (CycleObserver observer : observers) {
            int tmp = observer.tick(cycles, this);
            //  logger.info(observer.getClass().getName() + " cycles: " + tmp);
            if (tmp != -1 && tmp < untilInterrupt) {
                untilInterrupt = tmp;
            }
            /*            if (tmp == 0 && ! interruptsDisabled){
                System.out.println("Interrupted by: " + observer.getClass().getName());
            }
            */
        }
        return untilInterrupt;
    }

    private void clearRegisters() {
        for (int i = 0; i < REGISTER_COUNT; i++) {
            registers[i] = 0;
        }
    }

    private String nesTestRegisters() {
        //buf.append("CYC:").append(StringUtil.leftPad(""+cycles, 3));
        String buf = "A:" + String.format("%02X", registers[RegisterType.accumulator]) + " " +
                "X:" + String.format("%02X", registers[RegisterType.X]) + " " +
                "Y:" + String.format("%02X", registers[RegisterType.Y]) + " " +
                "P:" + String.format("%02X", registers[RegisterType.status]) + " " +
                "SP:" + String.format("%02X", registers[RegisterType.stackPointer]) + " " +
                "CYC:" + StringUtil.leftPad("" + ((cycles * 3) % 341), 3) +
                "SL:" + memory.read(0x2000);
        return buf // scanLine
                ;
    }

    private void displayRegisters() {
        var buf = new StringBuffer();
        buf.append("PC:").append(Integer.toHexString(registers[RegisterType.programCounter])).append("\t");
        buf.append("A:").append(Integer.toHexString(registers[RegisterType.accumulator])).append(" ");
        buf.append("X:").append(Integer.toHexString(registers[RegisterType.X])).append(" ");
        buf.append("Y:").append(Integer.toHexString(registers[RegisterType.Y])).append(" ");
        buf.append("P:").append(Integer.toHexString(registers[RegisterType.status])).append(" ");
        buf.append("SP:").append(Integer.toHexString(registers[RegisterType.stackPointer])).append(" ");
        buf.append("Z=").append(getZeroFlag()).append(",");
        buf.append("I=").append(getInterruptsDisabled()).append(",");
        buf.append("C=").append(getCarryFlag()).append(",");
        buf.append("S=").append(getSignFlag()).append(",");
        buf.append("O=").append(getOverflowFlag()).append(",");
        buf.append("D=").append(getDecimalFlag());
        buf.append("CYC=").append(cycles);
        buf.append("PCYC=").append((cycles * 3) % 341);
        logger.info("REGISTERS: " + buf);
    }

    protected boolean applyIndexing(Instruction.AddressingMode mode, int[] operands, int pc) {
        boolean boundaryCrossed = false;
        switch (mode) {
            case IndexedX:
            case ZeroPageIndexedX: {
                int addr = toInt(operands);
                addr = (addr + (0xFF & readRegister(RegisterType.X)));
                if (mode == Instruction.AddressingMode.ZeroPageIndexedX) {
                    addr &= 0xFF;
                }
                operands[0] = (addr & 0xFF);
                operands[1] = (addr & 0xFF00) >> 8;
            }
            break;
            case IndexedY:
            case ZeroPageIndexedY: {
                int addr = toInt(operands);
                int oldAddr = addr;
                addr += readRegister(RegisterType.Y);
                if ((addr & 0x100) != (oldAddr & 0x100)) { // check page boundary crossing
                    boundaryCrossed = true;
                }
                if (mode == Instruction.AddressingMode.ZeroPageIndexedY) {
                    addr &= 0xFF;
                }
                operands[0] = (addr & 0xFF);
                operands[1] = (addr & 0xFF00) >> 8;

            }
            break;
            case Relative: // signed
                int newval = (byte) operands[0] + pc;

                operands[0] = newval & 0xFF;
                operands[1] = (newval & 0xFF00) >> 8;
                if ((newval & 0x100) != (pc & 0x100)) { // check page boundary crossing
                    boundaryCrossed = true;
                }
                break;
            case Indirect: {
                int addr = toInt(operands);

                int val = 0;
                if ((addr & 0xFF) != 0xFF) {
                    val = memory.readWord(addr);
                } else { // deals with xxFF bug
                    val = memory.read(addr) | (memory.read(addr & 0xFF00) << 8);
                }
                //                System.out.println("Indirect address: " + Integer.toHexString(val));
                operands[0] = val & 0xFF;
                operands[1] = ((val & 0xFF00) >> 8);
            }
            break;
            case PreIndexedIndirect: // is more effectively called ZeroPagePreIndexedIndirect...
            {

                int addr = 0xFF & (operands[0] + (0xFF & readRegister(RegisterType.X))) & 0xFF;

                int val = 0;
                if (addr == 0xFF) {
                    val = 0xFFFF & (memory.read(addr) | (memory.read(0) << 8));
                } else {
                    val = memory.readWord(addr);
                }
                operands[0] = val & 0xFF;
                operands[1] = (val & 0xFF00) >> 8;
            }
            break;
            case PostIndexedIndirect: {
                int addr = operands[0];
                int val = 0;
                if (addr == 0xFF) {
                    val = 0xFFFF & (memory.read(addr) | (memory.read(0) << 8));
                } else {
                    val = memory.readWord(addr);
                }
                val = val + (0xFF & readRegister(RegisterType.Y));
                operands[0] = val & 0xFF;
                operands[1] = (val & 0xFF00) >> 8;
            }
            break;
        }
        return boundaryCrossed;
    }


    /**
     * NOTE: This method will update the ProgramCounter
     */
    protected InstructionBean fetchAndDecodeInstruction(int pc) {
        if (pc == LOAD_RAM_ADDRESS) { // intercept BASIC load's call to LOAD RAM
            System.out.println("Intercepting LOADRAM");
            handleLoadRAMFromDevice();
            return new InstructionBean(pc, 0x60, instructionSet.getByOpCode(0x60), new int[0]);
        } else if (pc == OPEN_DEVICE_ADDRESS) { // intercept serial open
            System.out.println("Intercepting serial open");
            return new InstructionBean(pc, 0x60, instructionSet.getByOpCode(0x60), new int[0]);
        }

        var operands = new int[2];
        long fetchStart = 1;
        int opcode = 0xFF & memory.read(pc);
        Instruction instruction = instructionSet.getByOpCode(opcode);

        long fetchEnd = 1;
        fetchElapsed += (fetchEnd - fetchStart);
        if (instruction == null) {
            //        	    System.err.println("Unknown opcode: " + Integer.toHexString(opcode));
            //    return null;
            System.out.println("Undefied opcode: " + Integer.toHexString(opcode) + " switching to NOP");
            instruction = instructionSet.getByOpCode(0xEA); // NOP
            opcode = 0xEA;

        }

        //if (shouldLog && logger.isLoggable(Level.FINEST))
        //logger.finest(instruction.getAssembly());

        /** "DEOODE" OPERANDS **/
        Instruction.AddressingMode mode = instruction.getAddressingMode();
        //if (shouldLog && logger.isLoggable(Level.FINEST))
        //logger.finest(mode.toString());

        // we always use the same operand data
        operands[0] = 0;
        operands[1] = 0;
        //int instructionData = opcode << 16;
        int numBytes = mode.getByteCount();
        //	    int[] operands = new int[numBytes];
        if (numBytes > 0) {
            if (numBytes == 1) {
                operands[0] = memory.read(pc + 1);
            } else if (numBytes == 2) {
                int val = memory.readWord(pc + 1);
                operands[0] = val & 0xFF;
                operands[1] = (val & 0xFF00) >> 8;
            }
            /*
              for (int i = 1; i <= numBytes; i++){
              //instructionData |= ((0xFF & memory.read(pc + i)) << (8 * (2- i)));
              operands[i-1] = 0xFF & memory.read(pc + i);
              }
            */

        }

        var bean = new InstructionBean(pc, opcode, instruction, operands);
        pc = pc + 1 + numBytes; // move our PC to the next instruction
        writeRegister(RegisterType.programCounter, pc); // this can get overwritten by the instruction

        long decodeStart = 1;
        // now, we apply the addressing mode (basically , follow any indirects or indexes)
        if (numBytes > 0) {
            switch (mode) {
                case IndexedX:
                case ZeroPageIndexedX: {
                    int addr = toInt(operands);
                    addr += readRegister(RegisterType.X);
                    operands[0] = (addr & 0xFF);
                    operands[1] = (addr & 0xFF00) >> 8;
                }
                break;
                case IndexedY:
                case ZeroPageIndexedY: {
                    int addr = toInt(operands);
                    addr += readRegister(RegisterType.Y);
                    operands[0] = (addr & 0xFF);
                    operands[1] = (addr & 0xFF00) >> 8;

                }
                break;
                case Relative: // signed
                    int newval = (byte) operands[0] + pc;
                    operands[0] = newval & 0xFF;
                    operands[1] = (newval & 0xFF00) >> 8;
                    break;
                case Indirect: {
                    int addr = toInt(operands);

                    int val = memory.readWord(addr);
                    operands[0] = val & 0xFF;
                    operands[1] = ((val & 0xFF00) >> 8);
                }
                break;
                case PreIndexedIndirect: {
                    int addr = (operands[0] + readRegister(RegisterType.X)) % 0xFF;
                    int val = memory.readWord(addr);

                    operands[0] = val & 0xFF;
                    operands[1] = (val & 0xFF00) >> 8;
                }
                break;
                case PostIndexedIndirect: {
                    int addr = operands[0];
                    int val = 0xFFFF & memory.readWord(addr);
                    val = val + (0xFF & readRegister(RegisterType.Y));
                    operands[0] = val & 0xFF;
                    operands[1] = (val & 0xFF00) >> 8;
                }
                break;
            }
        }

        long decodeEnd = 1;
        decodeElapsed += (decodeEnd - decodeStart);

        return bean;
    }

    public int readRegister(int rt) {
        //if (shouldLog && logger.isLoggable(Level.FINEST))
        //  logger.finest("Reading from register: " + rt);
        if (rt == RegisterType.status) {
            return registers[rt] | STATUS_FLAG_UNUSED; // unused flag
        } else return registers[rt];
    }

    public void writeRegister(int rt, int value) {
        //	if (shouldLog && logger.isLoggable(Level.FINEST))
        //  logger.finest("Writing to register " + rt + ": " + value + "(" + Integer.toHexString(value));
        registers[rt] = value;
        if (rt == RegisterType.status) {
            setStatusFlags();
        }
    }

    protected void setStatusFlags() {
        int value = registers[RegisterType.status];
        setCarryFlag((value & STATUS_FLAG_CARRY) != 0);
        setZeroFlag((value & STATUS_FLAG_ZERO) != 0);
        setInterruptsDisabled((value & STATUS_FLAG_INTERRUPT) != 0);
        setSignFlag((value & STATUS_FLAG_SIGN) != 0);
        setDecimalFlag((value & STATUS_FLAG_DECIMAL) != 0);
        setOverflowFlag((value & STATUS_FLAG_OVERFLOW) != 0);
        setBreakFlag((value & STATUS_FLAG_BREAK) != 0);
    }

    public void setInterruptsDisabled(boolean disabled) {
        if (disabled) {
            registers[RegisterType.status] |= STATUS_FLAG_INTERRUPT;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_INTERRUPT);
        }
        interruptsDisabled = disabled;
    }

    public boolean getInterruptsDisabled() {
        return interruptsDisabled;
    }

    public void setCarryFlag(boolean carry) {
        this.carryFlag = carry;
        if (carry) {
            registers[RegisterType.status] |= STATUS_FLAG_CARRY;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_CARRY);
        }
    }

    public boolean getCarryFlag() {
        return carryFlag;
    }

    public void setDecimalFlag(boolean decimal) {
        if (decimal) {
            registers[RegisterType.status] |= STATUS_FLAG_DECIMAL;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_DECIMAL);
        }
        this.decimalFlag = decimal;
    }

    public boolean getDecimalFlag() {
        return decimalFlag;
    }

    public void setZeroFlag(boolean zero) {
        if (zero) {
            registers[RegisterType.status] |= STATUS_FLAG_ZERO;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_ZERO);
        }
        this.zeroFlag = zero;
    }

    public boolean getZeroFlag() {
        return zeroFlag;
    }

    public void setSignFlag(boolean signed) {
        if (signed) {
            registers[RegisterType.status] |= STATUS_FLAG_SIGN;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_SIGN);
        }
        this.signFlag = signed;
    }

    public boolean getSignFlag() {
        return signFlag;
    }

    public void setOverflowFlag(boolean overflow) {
        if (overflow) {
            registers[RegisterType.status] |= STATUS_FLAG_OVERFLOW;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_OVERFLOW);
        }
        this.overflowFlag = overflow;
    }

    public boolean getOverflowFlag() {
        return overflowFlag;
    }

    public void setBreakFlag(boolean breakFlag) {
        if (breakFlag) {
            registers[RegisterType.status] |= STATUS_FLAG_BREAK;
        } else {
            registers[RegisterType.status] &= (~STATUS_FLAG_BREAK);
        }
        this.breakFlag = breakFlag;
    }


    public boolean getBreakFlag() {
        return breakFlag;
    }

    private boolean handleDebuggingBreakPoint() {
        displayRegisters();
        try {
            boolean done = true;
            while (true) {
                System.err.print(">");
                var bufferedIn = new BufferedReader(new java.io.InputStreamReader(System.in));
                String line = null;
                while ((line = bufferedIn.readLine()) == null) {
                }
                if (line.startsWith("callstack")) {
                    var trace = (InstructionBean) instructionBuffer.getFirst();
                    while (trace != null) {
                        trace.display();
                        trace = (InstructionBean) instructionBuffer.getNext();
                    }

                } else if (line.startsWith("getvectors")) {
                    System.out.println("NMI: " + Integer.toHexString(memory.readWord(NMI_ADDRESS)));
                    System.out.println("IRQ: " + Integer.toHexString(memory.readWord(IRQ_VECTOR_ADDRESS)));
                    return false;
                } else if (line.startsWith("c")) {
                    inBreakPoint = false;
                    breakAddress = -1;
                    return false;
                } else if (line.startsWith("g")) { // run until new address
                    inBreakPoint = false;
                    breakAddress = Integer.parseInt(line.substring(1), 16);
                    return false;
                } else if (line.contains("stack")) {
                    System.out.println("-- STACK --");
                    int i = registers[RegisterType.stackPointer];
                    i++;
                    while (i <= 255) {
                        System.out.println(Integer.toHexString(memory.read(i | 0x100)));
                        i++;
                    }
                    System.out.println("[--------]");

                } else if (line.contains("dumpram")) {
                    String[] params = line.split(" ");
                    int start = Integer.parseInt(params[1], 16);
                    int end = Integer.parseInt(params[2], 16);
                    String filename = params[3];
                    memory.dump(start, end, filename);
                }
                /* loads a raw RAM file, assumes that first 2 bytes specify location*/
                else if (line.contains("loadraw")) {
                    String[] params = line.split(" ");
                    String filename = params[1];
                    var rom = new ROM(filename, new File(filename));
                    byte[] data = rom.getRaw();
                    for (int i = 0; i < data.length - 2; i++) {
                        memory.write(0x801 + i, 0xFF & data[i + 2]);
                    }
                    memory.write(0xBA, 8); // set device number
                    System.out.println("Loaded " + filename + " at " + Integer.toHexString(0x801));
                } else if (line.contains("loadprg")) {
                    var params = line.split(" ");
                    String filename = params[1];
                    var prgFile = new PRGFile(new File(filename));
                    memory.write(prgFile.getStartAddress(), prgFile.getData());
                    memory.write(0xBA, 8); // set device number
                    System.out.println("Loaded " + prgFile.getFilename() + " at " + Integer.toHexString(prgFile.getStartAddress()));
                } else if (line.contains("read")) {
                    var params = line.split(" ");
                    int val = memory.read(Integer.parseInt(params[1], 16));
                    System.out.println(val + " " + Integer.toHexString(val));
                } else if (line.contains("write")) {
                    String[] params = line.split(" ");
                    try {
                        memory.write(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } else if (line.startsWith("skipped")) { // skip current instruction
                    return true;
                } else if (line.startsWith("setreg")) {
                    var params = line.split(" ");
                    var reg = params[1];
                    int val = Integer.parseInt(params[2]);
                    if (("X").equalsIgnoreCase(reg)) {
                        registers[RegisterType.X] = val;
                    } else if (("Y").equalsIgnoreCase(reg)) {
                        registers[RegisterType.Y] = val;
                    } else if (("C").equalsIgnoreCase(reg)) {
                        setCarryFlag(1 == val);
                    } else if (("B").equalsIgnoreCase(reg)) {
                        setBreakFlag(1 == val);
                    } else if (("A").equalsIgnoreCase(reg)) {
                        registers[RegisterType.accumulator] = val;
                    } else if (("Z").equalsIgnoreCase(reg)) {
                        setZeroFlag(1 == val);
                    } else if (("S").equalsIgnoreCase(reg)) {
                        setSignFlag(1 == val);
                    }
                } else if (line.startsWith("s")) { // stop
                    keepRunning = false;
                    return false;
                } else if (line.contains("nolog")) {
                    shouldLog = false;
                    if (line.trim().length() > "dolog".length()) {
                        memory.disableLogging(line.substring(5));
                    } else {
                        memory.disableLogging();
                    }
                } else if (line.contains("dolog")) {
                    shouldLog = true;
                    if (line.trim().length() > "dolog".length()) {
                        memory.enableLogging(line.substring(5));
                    } else {
                        memory.enableLogging();
                    }
                } else if (line.contains("time")) {
                    double micros = elapsedTime / 1000.0;
                    double decodeMicros = decodeElapsed / 1000.0;

                    double fetchMicros = fetchElapsed / 1000.0;

                    double executeMicros = executeElapsed / 1000.0;
                    double loopFreq = loopCount / (micros / 1000000.0);

                    double inputMicros = inputElapsed / 1000.0;
                    //			cpuLine.append(" Average Memory Read(us): ").append(memory.getAverageReadTime());
                    String cpuLine = "Elapsed: " + micros +
                            " Average(ms): " + micros / loopCount +
                            " Avg FetchDecode(ms): " + decodeMicros / loopCount +
                            " Avg Fetch(ms): " + fetchMicros / loopCount +
                            " Avg Execute(ms): " + executeMicros / loopCount +
                            " Avg Input(ms): " + inputMicros / loopCount +
                            " Loop Freq: " + loopFreq +
                            " Cycle FReq: " + cycles / (micros / 1000000);
                    System.out.println(cpuLine);
                } else if (line.startsWith("n") || line.trim().equals("")) { // run next instruction
                    return false;
                }
            }

        } catch (java.io.IOException e) {
        }
        return false;

    }

    public void pushStack(byte val) {
        int sp = 0xFF & readRegister(RegisterType.stackPointer);
        memory.write(sp | 0x100, (byte) (val & 0xFF));
        sp = (sp - 1) & 0xFF;
        writeRegister(RegisterType.stackPointer, sp & 0xFF);
    }

    public int popStack() {
        int sp = readRegister(RegisterType.stackPointer);
        sp = (sp + 1) & 0xFF;

        writeRegister(RegisterType.stackPointer, sp & 0xFF);
        return 0xFF & memory.read(sp | 0x100);
    }

    // currently assumes "RAW", but could just go off the extension...
    private void handleLoadRAMFromDevice() {
        int start = registers[RegisterType.X] | (registers[RegisterType.Y] << 8);
        int filenameLen = memory.read(0xB7);
        System.out.println("Filename len: " + filenameLen);
        var fileBytes = new byte[filenameLen];
        int fileStart = memory.readWord(0xBB);
        System.out.println("File start: " + Integer.toHexString(fileStart));
        for (int i = 0; i < filenameLen; i++) {
            fileBytes[i] = (byte) memory.read(fileStart + i);
            System.out.println("Read " + Integer.toHexString(memory.read(fileStart + i)));
        }
        var filename = new String(fileBytes);
        var file = new File("roms/tsuite/Testsuite/" + filename);
        System.out.println("Start = " + Integer.toHexString(start) + " name = " + filename);
        try {
            var rom = new ROM(filename, file);
            byte[] data = rom.getRaw();
            for (int i = 0; i < data.length - 2; i++) {
                memory.write(start + i, 0xFF & data[i + 2]);
            }
            //writeRegister(RegisterType.X,(start + (data.length -2)) & 0xFF);
            //writeRegister(RegisterType.Y,(start + (data.length -2)) >> 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int toInt(int[] lowHigh) {
        return (0xFF & lowHigh[0]) | ((0xFF & lowHigh[1]) << 8);
    }

    public Memory getMemory() {
        return memory;
    }

    class InstructionBean {
        int address;
        int opcode;
        Instruction instruction;
        int[] operands;
        int[] actual;

        public InstructionBean(int address, int opcode, Instruction instruction, int[] operands) {
            this.address = address;
            this.instruction = instruction;
            this.operands = new int[operands.length];
            this.actual = new int[instruction.getAddressingMode().getByteCount()];
            this.opcode = opcode;
            System.arraycopy(operands, 0, this.operands, 0, 2);
            System.arraycopy(operands, 0, this.actual, 0, actual.length);
        }

        void display() {
            System.err.println(Integer.toHexString(address) + " " + instruction.getFullAssemblyLine(instruction.getAddressingMode(), operands));
        }

        public String toLogOutput() {
            return "Hello world";
        }

        public String toString() {
            var out = new StringBuffer(Integer.toHexString(address)).append("  ");
            out.append(Integer.toHexString(opcode));
            if (actual.length > 0) {
                out.append(" ");
                out.append(String.format("%02X", actual[0]));
            } else {
                out.append("   ");
            }
            if (actual.length > 1) {
                out.append(" ").append(String.format("%02X", actual[1]));
            } else {
                out.append("   ");
            }
            out.append("  ");
            int len = out.length();
            out.append(instruction.getAssembly());
            if (actual.length > 0) {
                out.append(" ");
                if (instruction.getAddressingMode() == Instruction.AddressingMode.Immediate) {
                    out.append("#");
                }
                out.append(Instruction.hex(actual));
            }
            int newlen = out.length() - len;
            out.append(StringUtil.leftPad("", 12 - newlen));
            return out.toString().toUpperCase();
        }
    }
}

