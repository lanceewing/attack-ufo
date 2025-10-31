package emu.attackufo.memory;

import com.badlogic.gdx.Gdx;

import emu.attackufo.MachineType;
import emu.attackufo.cpu.Cpu6502;
import emu.attackufo.io.Pia;
import emu.attackufo.video.Vic;

/**
 * This class emulators the Attack UFO machine's memory.
 * 
 * @author Lance Ewing
 */
public class Memory {

    /**
     * Holds the machines memory.
     */
    private int mem[];

    /**
     * Holds an array of references to instances of MemoryMappedChip where each
     * instance determines the behaviour of reading or writing to the given memory
     * address.
     */
    private MemoryMappedChip memoryMap[];

    /**
     * Constructor for Memory.
     * 
     * @param cpu      The CPU that will access this Memory.
     * @param vic      The VIC chip to map to memory.
     * @param pia      The PIA chip to map to memory.
     * @param snapshot Optional snapshot of the machine state to start with.
     */
    public Memory(Cpu6502 cpu, Vic vic, Pia pia, MachineType machineType) {
        this.mem = new int[0x4000];
        this.memoryMap = new MemoryMappedChip[0x4000];
        initVicMemory(vic, pia);
        cpu.setMemory(this);
    }

    /**
     * Initialise the Attack UFO machine's memory.
     * 
     * @param vic The VIC chip to map to memory.
     * @param pia The PIA chip to map to memory.
     */
    private void initVicMemory(Vic vic, Pia pia) {
        // At $0000 is 1K of 8-bit RAM. The lower half is used for the normal 6502
        // zero-page and stack. The second half from $0200-$03FF is used for the 
        // Video screen memory.
        mapChipToMemory(new RamChip(), 0x0000, 0x03FF);

        // At $0400 is 1K of 4-bit RAM. The lower half is not used, but the top half
        // from $0600 is used for the colour RAM.
        mapChipToMemory(new NibbleRamChip(), 0x0400, 0x07FF);

        mapChipToMemory(new UnconnectedMemory(), 0x0800, 0x0FFF);

        // At $1000 is the VIC chip. It has 16 addressable locations.
        mapChipToMemory(vic, 0x1000, 0x100F);

        mapChipToMemory(new UnconnectedMemory(), 0x1010, 0x13FF);

        // At $1400 is the PIA chip used for reading the controls. It only has 4
        // addressable locations.
        mapChipToMemory(pia, 0x1400, 0x1403);

        mapChipToMemory(new UnconnectedMemory(), 0x1404, 0x1BFF);

        // This 1K of 8-bit RAM is used for modifiable character data.
        mapChipToMemory(new RamChip(), 0x1C00, 0x1FFF);

        // The second half of the memory map consists of the eight ROM chips.
        mapChipToMemory(new RomChip(), 0x2000, 0x23FF, Gdx.files.internal("roms/1.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x2400, 0x27FF, Gdx.files.internal("roms/2.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x2800, 0x2BFF, Gdx.files.internal("roms/3.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x2C00, 0x2FFF, Gdx.files.internal("roms/4.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x3000, 0x33FF, Gdx.files.internal("roms/5.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x3400, 0x37FF, Gdx.files.internal("roms/6.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x3800, 0x3BFF, Gdx.files.internal("roms/7.rom").readBytes());
        mapChipToMemory(new RomChip(), 0x3C00, 0x3FFF, Gdx.files.internal("roms/8.rom").readBytes());
    }

    /**
     * Maps the given chip instance at the given address range.
     * 
     * @param chip         The chip to map at the given address range.
     * @param startAddress The start of the address range.
     * @param endAddress   The end of the address range.
     */
    private void mapChipToMemory(MemoryMappedChip chip, int startAddress, int endAddress) {
        mapChipToMemory(chip, startAddress, endAddress, null);
    }

    /**
     * Maps the given chip instance at the given address range, optionally loading
     * the given initial state data into that address range. This state data is
     * intended to be used for things such as ROM images.
     * 
     * @param chip         The chip to map at the given address range.
     * @param startAddress The start of the address range.
     * @param endAddress   The end of the address range.
     * @param state        byte array containing initial state (can be null).
     */
    private void mapChipToMemory(MemoryMappedChip chip, int startAddress, int endAddress, byte[] state) {
        int statePos = 0;

        // Load the initial state into memory if provided.
        if (state != null) {
            for (int i = startAddress; i <= endAddress; i++) {
                mem[i] = (state[statePos++] & 0xFF);
            }
        }

        // Configure the chip into the memory map between the given start and end
        // addresses.
        for (int i = startAddress; i <= endAddress; i++) {
            memoryMap[i] = chip;
        }

        chip.setMemory(this);
    }

    /**
     * Gets the int array that represents the Attack UFO machine's memory.
     * 
     * @return an int array represents the Attack UFO machine's memory.
     */
    public int[] getMemoryArray() {
        return mem;
    }

    /**
     * Reads the value of the given Attack UFO memory address.
     * 
     * @param address The address to read the byte from.
     * 
     * @return The contents of the memory address.
     */
    public int readMemory(int address) {
        return (memoryMap[address].readMemory(address));
    }

    /**
     * Writes a value to the give Attack UFO memory address.
     * 
     * @param address The address to write the value to.
     * @param value   The value to write to the given address.
     */
    public void writeMemory(int address, int value) {
        memoryMap[address].writeMemory(address, value);
    }
}
