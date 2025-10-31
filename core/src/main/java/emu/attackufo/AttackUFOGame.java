package emu.attackufo;

import com.badlogic.gdx.Game;

import emu.attackufo.ui.ConfirmHandler;

/**
 * The main class for the generic part of the Attack UFO emulator.
 * 
 * @author Lance Ewing
 */
public class AttackUFOGame extends Game {

    /**
     * This is the screen that is used to show the running emulation.
     */
    private MachineScreen machineScreen;

    /**
     * Invoked by JVic whenever it would like the user to confirm an action.
     */
    private ConfirmHandler confirmHandler;

    /**
     * Constructor for JVicGdx.
     * 
     * @param confirmHandler
     */
    public AttackUFOGame(ConfirmHandler confirmHandler) {
        this.confirmHandler = confirmHandler;
    }

    @Override
    public void create() {
        machineScreen = new MachineScreen(this, confirmHandler);

        machineScreen.initMachine(MachineType.NTSC);
        setScreen(machineScreen);
    }

    /**
     * Gets the MachineScreen.
     * 
     * @return The MachineScreen.
     */
    public MachineScreen getMachineScreen() {
        return machineScreen;
    }

    @Override
    public void dispose() {
        super.dispose();

        // For now we'll dispose the MachineScreen here. As the emulator grows and
        // adds more screens, this may be managed in a different way. Note that the
        // super dispose does not call dispose on the screen.
        machineScreen.dispose();
    }
}
