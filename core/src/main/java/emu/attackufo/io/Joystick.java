package emu.attackufo.io;

import java.util.HashMap;
import com.badlogic.gdx.Input.Keys;

/**
 * This class emulates the Attack UFO controllers by listening to key events and
 * translating the relevant key codes in to controller signals.
 * 
 * @author Lance Ewing
 */
public class Joystick {

    /**
     * Data used to convert Java keypresses into Joystick signals.
     */
    private static int keyToJoystickData[][] = { { Keys.NUMPAD_0, 0x10 }, // Fire button
            { Keys.SPACE, 0x10 },       // Fire button
            { Keys.NUMPAD_8, 0x10 },    // Fire button
            { Keys.UP, 0x10 },          // Fire button
            { Keys.W, 0x10 },           // Fire button
            { Keys.P, 0x10 },           // Fire button
            { Keys.INSERT, 0x10 },      // Fire button
            { Keys.NUMPAD_1, 0x10 },    // Fire button
            { Keys.NUM_1, 0x10 },       // Fire button
            { Keys.NUM_8, 0x10 },       // Fire button

            { Keys.NUMPAD_5, 0x80 },    // Coin
            { Keys.C, 0x80 },           // Coin
            { Keys.NUMPAD_3, 0x80 },    // Coin
            { Keys.NUM_3, 0x80 },       // Coin
            { Keys.NUM_5, 0x80 },       // Coin
            { Keys.PAGE_DOWN, 0x80 },   // Coin
            { Keys.PLUS, 0x80 },        // Coin
            { Keys.F1, 0x80 },          // Coin
            { Keys.ALT_RIGHT, 0x80 },   // Coin
            { Keys.ALT_LEFT, 0x80 },    // Coin

            { Keys.NUMPAD_4, 0x04 },    // Left
            { Keys.A, 0x04 },           // Left
            { Keys.L, 0x04 },           // Left
            { Keys.LEFT, 0x04 },        // Left

            { Keys.NUMPAD_6, 0x08 },    // Right
            { Keys.D, 0x08 },           // Right
            { Keys.APOSTROPHE, 0x08 },  // Right
            { Keys.RIGHT, 0x08 },       // Right

            { Keys.ENTER, 0x01 },       // Start
            { Keys.S, 0x01 },           // Start
            { Keys.DOWN, 0x01 },        // Start
            { Keys.NUMPAD_2, 0x01 },    // Start
            { Keys.NUM_2, 0x01 },       // Start
    };

    /**
     * HashMap used to store mappings between Java key events and joystick signals.
     */
    private HashMap<Integer, Integer> keyToJoystickMap;

    /**
     * The current state of the joystick signals.
     */
    private int joystickState;

    /**
     * Constructor for Joystick.
     */
    public Joystick() {
        // Create the hash map for fast lookup.
        keyToJoystickMap = new HashMap<Integer, Integer>();

        // Initialise the key to joystick signal HashMap.
        for (int i = 0; i < keyToJoystickData.length; i++) {
            keyToJoystickMap.put(keyToJoystickData[i][0], keyToJoystickData[i][1]);
        }
    }

    /**
     * Gets the current joystick state.
     * 
     * @return The current joystick state.
     */
    public int getJoystickState() {
        return ((~joystickState) & 0xFF);
    }

    /**
     * Invoked when a key has been pressed.
     *
     * @param keycode The keycode of the key that has been pressed.
     */
    public void keyPressed(int keycode) {
        Integer joystickSignal = keyToJoystickMap.get(keycode);
        if (joystickSignal != null) {
            joystickState |= joystickSignal;
        }
    }

    /**
     * Invoked when a key has been released.
     *
     * @param keycode The keycode of the key that has been released.
     */
    public void keyReleased(int keycode) {
        Integer joystickSignal = keyToJoystickMap.get(keycode);
        if (joystickSignal != null) {
            joystickState &= (~joystickSignal);
        }
    }
}
