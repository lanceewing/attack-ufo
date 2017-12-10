package emu.attackufo;

import com.badlogic.gdx.Gdx;

import emu.attackufo.cpu.Cpu6502;
import emu.attackufo.io.Joystick;
import emu.attackufo.io.Pia;
import emu.attackufo.memory.Memory;
import emu.attackufo.video.Vic;

/**
 * Represents the Attack UFO machine.
 * 
 * @author Lance Ewing
 */
public class Machine {

  // Machine components.
  private Memory memory;
  private Vic vic;
  private Pia pia;
  private Cpu6502 cpu;
  
  // Peripherals.
  private Joystick joystick;

  private boolean paused = true;
  
  private MachineType machineType;
  
  // These control what part of the generate pixel data is rendered to the screen. 
  private int screenLeft;
  private int screenRight;
  private int screenTop;
  private int screenBottom;
  private int screenWidth;
  private int screenHeight;
  
  /**
   * Constructor for Machine.
   */
  public Machine() {
  }
  
  /**
   * Initialises the machine.
   * 
   * @param machineType The type of Attack UFO machine, i.e. PAL or NTSC.
   */
  public void init(MachineType machineType) {
    this.machineType = machineType;
    
    // Create the microprocessor.
    cpu = new Cpu6502();
    
    // Create the VIC chip and configure it as per the current TV type.
    vic = new Vic(machineType);
    
    // Create the peripherals.
    joystick = new Joystick();
    
    // Create the PIA chip
    pia = new Pia(joystick);
    
    // Now we create the memory, which will include mapping the VIC chip,
    // the VIA chips, and the creation of RAM chips and ROM chips.
    memory = new Memory(cpu, vic, pia, machineType);
    
    // Set up the screen dimensions based on the VIC chip settings. Aspect ratio of 4:3.
    screenWidth = (machineType.getVisibleScreenHeight() / 3) * 4;
    screenHeight = machineType.getVisibleScreenHeight();
    screenLeft = machineType.getHorizontalOffset();
    screenRight = screenLeft + machineType.getVisibleScreenWidth();
    screenTop = machineType.getVerticalOffset();
    screenBottom = screenTop + machineType.getVisibleScreenHeight();
    
    cpu.reset();
  }

  /**
   * Updates the state of the machine of the machine until a frame is complete
   * 
   * @param skipRender true if the VIC chip emulation should skip rendering.
   */
  public void update(boolean skipRender) {
    boolean frameComplete = false;
    if (skipRender) {
      do {
        frameComplete |= vic.emulateSkipCycle();
        cpu.emulateCycle();
        pia.emulateCycle();
      } while (!frameComplete);
    } else {
      do {
        frameComplete |= vic.emulateCycle();
        cpu.emulateCycle();
        pia.emulateCycle();
      } while (!frameComplete);
    }
  }
  
  /**
   * @return the screenLeft
   */
  public int getScreenLeft() {
    return screenLeft;
  }

  /**
   * @return the screenRight
   */
  public int getScreenRight() {
    return screenRight;
  }

  /**
   * @return the screenTop
   */
  public int getScreenTop() {
    return screenTop;
  }

  /**
   * @return the screenBottom
   */
  public int getScreenBottom() {
    return screenBottom;
  }

  /**
   * @return the screenWidth
   */
  public int getScreenWidth() {
    return screenWidth;
  }

  /**
   * @return the screenHeight
   */
  public int getScreenHeight() {
    return screenHeight;
  }

  /**
   * Gets the pixels for the current frame from the VIC chip.
   * 
   * @return The pixels for the current frame. Returns null if there isn't one that is ready.
   */
  public short[] getFramePixels() {
    return vic.getFramePixels();
  }

  /**
   * Emulates a single machine cycle.
   * 
   * @return true If the VIC chip has indicated that a frame should be rendered.
   */
  public boolean emulateCycle() {
    boolean render = vic.emulateCycle();
    cpu.emulateCycle();
    pia.emulateCycle();
    return render;
  }
  
  /**
   * Pauses and resumes the Machine.
   * 
   * @param paused true to pause the machine, false to resume.
   */
  public void setPaused(boolean paused) {
    this.paused = paused;
  }
  
  /**
   * Returns whether the Machine is paused or not.
   * 
   * @return true if the machine is paused; otherwise false.
   */
  public boolean isPaused() {
    return paused;
  }
  
  /**
   * Gets the MachineType of this Machine, i.e. either PAL or NTSC.
   * 
   * @return The MachineType of this Machine, i.e. either PAL or NTSC.
   */
  public MachineType getMachineType() {
    return machineType;
  }
  
  /**
   * Gets the Joystick of this Machine.
   * 
   * @return The Joystick of this Machine.
   */
  public Joystick getJoystick() {
    return joystick;
  }
}
