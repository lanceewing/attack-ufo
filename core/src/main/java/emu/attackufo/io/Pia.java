package emu.attackufo.io;

import emu.attackufo.memory.MemoryMappedChip;

/**
 * This class emulates the minimum 6521 PIA I/O chip functionality required by
 * Attack UFO.
 * 
 * @author Lance Ewing
 */
public class Pia extends MemoryMappedChip {

    // Constants for the 4 internal memory mapped registers.
    private static final int VIA_REG_0 = 0;
    private static final int VIA_REG_1 = 1;
    private static final int VIA_REG_2 = 2;
    private static final int VIA_REG_3 = 3;

    // Port A
    protected int outputRegisterA;
    protected int inputRegisterA;
    protected int portAPins;
    protected int dataDirectionRegisterA;
    protected int ca1Control;
    protected int ca2Control;
    protected int ddraAccess;
    protected int irqa1;
    protected int irqa2;

    // Port B
    protected int outputRegisterB;
    protected int inputRegisterB;
    protected int portBPins;
    protected int dataDirectionRegisterB;
    protected int cb1Control;
    protected int cb2Control;
    protected int ddrbAccess;
    protected int irqb1;
    protected int irqb2;

    /**
     * The Joystick from which we get the current joystick state from.
     */
    private Joystick joystick;

    /**
     * Constructor for Pia.
     * 
     * @param joystick The Joystick from which the Via gets the current joystick
     *                 state from.
     */
    public Pia(Joystick joystick) {
        this.joystick = joystick;
    }

    /**
     * Writes a byte into one of the 16 VIA registers.
     * 
     * @param address The address to write to.
     * @param value   The byte to write into the address.
     */
    public void writeMemory(int address, int value) {
        switch (address & 0x000F) {
        case VIA_REG_0: // Port A
            if (ddraAccess == 1) {
                outputRegisterA = value;
            } else {
                dataDirectionRegisterA = value;
            }
            updatePortAPins();
            break;

        case VIA_REG_1: // Control Register A
            ca1Control = (value & 0x03);
            ddraAccess = ((value & 0x04) >> 2);
            ca2Control = ((value & 0x38) >> 3);
            break;

        case VIA_REG_2: // Port B
            if (ddrbAccess == 1) {
                outputRegisterB = value;
            } else {
                dataDirectionRegisterB = value;
            }
            updatePortBPins();
            break;

        case VIA_REG_3: // Control Register B
            cb1Control = (value & 0x03);
            ddrbAccess = ((value & 0x04) >> 2);
            cb2Control = ((value & 0x38) >> 3);
            break;
        }
    }

    /**
     * Reads a value from one of the 16 VIA registers.
     * 
     * @param address The address to read the register value from.
     */
    public int readMemory(int address) {
        int value = 0;

        switch (address & 0x000F) {
        case VIA_REG_0: // ORB/IRB
            if (ddraAccess == 1) {
                value = getPortAPins() & (~dataDirectionRegisterA);
            } else {
                value = dataDirectionRegisterA;
            }
            break;

        case VIA_REG_1: // Control Register A
            value = ((this.irqa1 << 7) & 0x80) | ((this.irqa2 << 6) & 0x40) | ((this.ca2Control << 3) & 0x3F)
                    | ((this.ddraAccess << 2) & 0x04) | ((this.ca1Control & 0x03));
            break;

        case VIA_REG_2: // Port B
            if (ddrbAccess == 1) {
                value = getPortBPins() & (~dataDirectionRegisterB);
            } else {
                value = dataDirectionRegisterB;
            }
            break;

        case VIA_REG_3: // Control Register B
            value = ((this.irqb1 << 7) & 0x80) | ((this.irqb2 << 6) & 0x40) | ((this.cb2Control << 3) & 0x3F)
                    | ((this.ddrbAccess << 2) & 0x04) | ((this.cb1Control & 0x03));
            break;
        }

        return value;
    }

    /**
     * Emulates a single cycle of this VIA chip.
     */
    public void emulateCycle() {
        // Doesn't need to do anything for the purposes of Attack UFO.
    }

    /**
     * Updates the state of the Port A pins based on the current values of the ORA
     * and DDRA.
     */
    protected void updatePortAPins() {
        // Any pins that are inputs must be left untouched.
        int inputPins = (portAPins & (~dataDirectionRegisterA));

        // Pins that are outputs should be set to 1 or 0 depending on what is in the ORA.
        int outputPins = (outputRegisterA & dataDirectionRegisterA);

        portAPins = inputPins | outputPins;
    }

    /**
     * Updates the state of the Port B pins based on the current values of the ORB
     * and DDRB.
     */
    protected void updatePortBPins() {
        // Any pins that are inputs must be left untouched.
        int inputPins = (portBPins & (~dataDirectionRegisterB));

        // Pins that are outputs should be set to 1 or 0 depending on what is in the ORB.
        int outputPins = (outputRegisterB & dataDirectionRegisterB);

        portBPins = inputPins | outputPins;
    }

    /**
     * Returns the current values of the Port A pins.
     *
     * @return the current values of the Port A pins.
     */
    protected int getPortAPins() {
        // PORT_DIPNAME(0x03, 0x00, DEF_STR( Lives ))
        // PORT_DIPSETTING( 0x00, "3")
        // PORT_DIPSETTING( 0x01, "4")
        // PORT_DIPSETTING( 0x02, "5")
        // PORT_DIPSETTING( 0x03, "6")
        // PORT_DIPNAME(0x04, 0x04, DEF_STR( Bonus_Life ))
        // PORT_DIPSETTING( 0x04, "1000")
        // PORT_DIPSETTING( 0x00, "1500")
        // PORT_DIPUNUSED(0x08, IP_ACTIVE_LOW)
        // PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED)
        // PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED)
        // PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED)
        // PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1)
        int dipSwitchSettings = 0x02;
        return (joystick.getJoystickState() & 0x80) | dipSwitchSettings;
    }

    /**
     * Returns the current values of the Port B pins.
     *
     * @return the current values of the Port B pins.
     */
    protected int getPortBPins() {
        // PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1)
        // PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2)
        // PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT) PORT_PLAYER(1) PORT_2WAY
        // PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT) PORT_PLAYER(1) PORT_2WAY
        // PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1) PORT_PLAYER(1)
        // PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT) PORT_PLAYER(2) PORT_2WAY
        // PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT) PORT_PLAYER(2) PORT_2WAY
        // PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1) PORT_PLAYER(2)
        return (joystick.getJoystickState() & 0x7F);
    }
}
