package emu.attackufo;

/**
 * An enum that represents the two types of VIC chip machine, i.e. PAL and NTSC.
 *  
 * @author Lance Ewing
 */
public enum MachineType {
  
  PAL(1108405, 284, 312, 208, 272, 32, 24, 50),
  NTSC(1022727, 260, 261, 204, 252, 8, 8, 60);
  
  private int cyclesPerSecond;
  private int totalScreenWidth;
  private int totalScreenHeight;
  private int visibleScreenWidth;
  private int visibleScreenHeight;
  private int horizontalOffset;
  private int verticalOffset;
  private int framesPerSecond;
  private int frameDuration;
  private int cyclesPerFrame;
  
  /**
   * Constructor for MachineType.
   * 
   * @param cyclesPerSecond
   * @param totalScreenWidth
   * @param totalScreenHeight
   * @param visibleScreenWidth
   * @param visibleScreenHeight
   * @param horizontalOffset
   * @param verticalOffset
   * @param framesPerSecond
   */
  MachineType(int cyclesPerSecond, int totalScreenWidth, int totalScreenHeight, int visibleScreenWidth, int visibleScreenHeight, 
      int horizontalOffset, int verticalOffset, int framesPerSecond) {
    this.cyclesPerSecond = cyclesPerSecond;
    this.totalScreenWidth = totalScreenWidth;
    this.totalScreenHeight = totalScreenHeight;
    this.visibleScreenWidth = visibleScreenWidth;
    this.visibleScreenHeight = visibleScreenHeight;
    this.horizontalOffset = horizontalOffset;
    this.verticalOffset = verticalOffset;
    this.framesPerSecond = framesPerSecond;
    this.frameDuration = 1000/framesPerSecond;
    this.cyclesPerFrame = cyclesPerSecond/framesPerSecond;
  }
  
  /**
   * @return the cyclesPerSecond
   */
  public int getCyclesPerSecond() {
    return cyclesPerSecond;
  }

  /**
   * @return the totalScreenWidth
   */
  public int getTotalScreenWidth() {
    return totalScreenWidth;
  }

  /**
   * @return the totalScreenHeight
   */
  public int getTotalScreenHeight() {
    return totalScreenHeight;
  }

  /**
   * @return the visibleScreenWidth
   */
  public int getVisibleScreenWidth() {
    return visibleScreenWidth;
  }

  /**
   * @return the visibleScreenHeight
   */
  public int getVisibleScreenHeight() {
    return visibleScreenHeight;
  }

  /**
   * @return the horizontalOffset
   */
  public int getHorizontalOffset() {
    return horizontalOffset;
  }

  /**
   * @return the verticalOffset
   */
  public int getVerticalOffset() {
    return verticalOffset;
  }
  
  /**
   * @return the framesPerSecond
   */
  public int getFramesPerSecond() {
    return framesPerSecond;
  }

  /**
   * @return the frameDuration
   */
  public int getFrameDuration() {
    return frameDuration;
  }

  /**
   * @return the cyclesPerFrame
   */
  public int getCyclesPerFrame() {
    return cyclesPerFrame;
  }
}
