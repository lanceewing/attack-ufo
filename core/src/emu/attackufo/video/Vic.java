package emu.attackufo.video;

import com.badlogic.gdx.Gdx;

import emu.attackufo.MachineType;
import emu.attackufo.memory.MemoryMappedChip;

/**
 * This class emulates the VIC chip. The emulation is cycle based.
 * 
 * @author Lance Ewing
 */
public class Vic extends MemoryMappedChip {
  
  /**
   * This is the memory location that the VIC chip reads from when outside the
   * video matrix (technically speaking it reads from 0x3814, but for speed
   * reasons, the VIC chip memory mapping is not emulated exactly).
   */
  private static final int DEFAULT_FETCH_ADDRESS = 0x1814;

  /**
   * Constant for fetch toggle to indicate that screen code should be fetched.
   */
  private static final int FETCH_SCREEN_CODE = 0;

  /**
   * Constant for fetch toggle to indicate that character data should be fetched.
   */
  private static final int FETCH_CHAR_DATA = 1;

  // VIC chip memory mapped registers.
  private static final int VIC_REG_0 = 0x1000;
  private static final int VIC_REG_1 = 0x1001;
  private static final int VIC_REG_2 = 0x1002;
  private static final int VIC_REG_3 = 0x1003;
  private static final int VIC_REG_4 = 0x1004;
  private static final int VIC_REG_5 = 0x1005;
  private static final int VIC_REG_6 = 0x1006;
  private static final int VIC_REG_7 = 0x1007;
  private static final int VIC_REG_8 = 0x1008;
  private static final int VIC_REG_9 = 0x1009;
  private static final int VIC_REG_10 = 0x100A;
  private static final int VIC_REG_11 = 0x100B;
  private static final int VIC_REG_12 = 0x100C;
  private static final int VIC_REG_13 = 0x100D;
  private static final int VIC_REG_14 = 0x100E;
  private static final int VIC_REG_15 = 0x100F;
  
  private final static short palRGB565Colours[] = {
    (short)0x0000,         // BLACK
    (short)0xFFFF,         // WHITE
    (short)0xB0E4,         // RED
    (short)0x4F9F,         // CYAN
    (short)0xB1FF,         // PURPLE
    (short)0x4706,         // GREEN
    (short)0x19BF,         // BLUE
    (short)0xDEA3,         // YELLOW
    (short)0xCAA0,         // ORANGE
    (short)0xED8E,         // LIGHT ORANGE
    (short)0xE492,         // PINK
    (short)0x9FBF,         // LIGHT CYAN
    (short)0xE4FF,         // LIGHT PURPLE
    (short)0x8F32,         // LIGHT GREEN
    (short)0x849F,         // LIGHT BLUE
    (short)0xE6F0          // LIGHT YELLOW
  };
  
  private final static short vicColours[] = palRGB565Colours;
  
  /**
   * A lookup table for determining the start of video memory.
   */
  private final static int videoMemoryTable[] = { 
    0x0000, 0x0200, 0x0400, 0x0600, 0x0800, 0x0A00, 0x0C00, 0x0E00, 
    0x1000, 0x1200, 0x1400, 0x1600, 0x1800, 0x1A00, 0x1C00, 0x1E00, 
    0x2000, 0x2200, 0x2400, 0x2600, 0x2800, 0x2A00, 0x2C00, 0x2E00, 
    0x3000, 0x3200, 0x3400, 0x3600, 0x3800, 0x3A00, 0x3C00, 0x3E00 
  };

  /**
   * A lookup table for determining the start of character memory.
   */
  private final static int charMemoryTable[] = { 
    0x0000, 0x0400, 0x0800, 0x0C00, 0x1000, 0x1400, 0x1800, 0x1C00,
    0x2000, 0x2400, 0x2800, 0x2C00, 0x3000, 0x3400, 0x3800, 0x3C00 
  };

  /**
   * The type of machine that this Vic chip is in, i.e. either PAL or NTSC.
   */
  private MachineType machineType;
  
  /**
   * Last byte fetched by the VIC chip. This could be the cell index or cell bitmap.
   * 
   * TODO: It should have a separate variable for the char bitmap, since that is how it is done at the silicon level.
   */
  private int cellData;

  /**
   * Last fetched cell colour.
   */
  private short cellColour;

  /**
   * Index of the cell colour into the colours array.
   */
  private int cellColourIndex;

  /**
   * Current start of video memory.
   */
  private int videoMemoryStart;

  /**
   * Current start of colour memory.
   */
  private int colourMemoryStart;

  /**
   * Current start of character memory.
   */
  private int charMemoryStart;

  /**
   * Video matrix counter value.
   */
  private int videoMatrixCounter;

  /**
   * The video matrix location of the start of the current row.
   */
  private int rowStart;

  /**
   * Horizontal counter (measured in pixels).
   */
  private int horizontalCounter;

  /**
   * Vertical counter (measured in pixels). Aka. raster line
   */
  private int verticalCounter;

  /**
   * Pixel counter. Current offset into TV frame array.
   */
  private int pixelCounter;

  /**
   * Cell depth counter. This will be the current offset into the character
   * table and will be a value between 0 and the character size (8 or 16).
   */
  private int cellDepthCounter;

  /**
   * The actual start position inside character table after adding the current
   * cell depth offset.
   */
  private int charMemoryCellDepthStart;

  /**
   * The number of rows in the video matrix.
   */
  private int numOfRows;

  /**
   * The number of columns in the video matrix.
   */
  private int numOfColumns;

  /**
   * The current character size.
   */
  private int characterSize;

  /**
   * The number of bits to shift a cell index left by to get the true index into
   * the character table.
   */
  private int characterSizeShift;

  /**
   * The current horizontal screen origin.
   */
  private int horizontalScreenOrigin;

  /**
   * The current vertical screen origin.
   */
  private int verticalScreenOrigin;

  /**
   * Toggle used to keep track of whether to fetch a screen code or character
   * data (0 = fetch screen code, 1 = fetch character data)
   */
  private int fetchToggle;

  /**
   * The current background colour.
   */
  private short backgroundColour;

  /**
   * The index of the current background colour into the colours array.
   */
  private int backgroundColourIndex;

  /**
   * The current border colour.
   */
  private short borderColour;

  /**
   * The current auxiliary colour;
   */
  private short auxiliaryColour;

  /**
   * Whether the characters are reversed at present or not.
   */
  private int reverse;

  /**
   * Holds the current colour values of each of the multi-colour colours.
   */
  private short multiColourTable[] = new short[4];

  /**
   * The left hand side of th text screen.
   */
  private int textScreenLeft;

  /**
   * The right hand side of the text screen.
   */
  private int textScreenRight;

  /**
   * The top line of the text screen.
   */
  private int textScreenTop;

  /**
   * The bottom line of the text screen.
   */
  private int textScreenBottom;

  /**
   * The width in pixels of the text screen.
   */
  private int textScreenWidth;

  /**
   * The height in pixels of the text screen.
   */
  private int textScreenHeight;

  /**
   * Master volume for all of the VIC chip voices.
   */
  private int masterVolume;
  
  /**
   * Represents the data for one VIC frame.
   */
  class Frame {
    
    /**
     * Holds the pixel data for the TV frame screen.
     */
    short framePixels[];
    
    /**
     * Says whether this frame is ready to be blitted to the GPU.
     */
    boolean ready;
  }
  
  /**
   * An array of two Frames, one being the one that the VIC is currently writing to,
   * the other being the last one that was completed and ready to blit.
   */
  private Frame[] frames;
  
  /**
   * The index of the active frame within the frames. This will toggle between 0 and 1.
   */
  private int activeFrame;
  
  /**
   * Constructor for VIC.
   * 
   * @param machineType The type of machine, PAL or NTSC.
   */
  public Vic(MachineType machineType) {
    this.machineType = machineType;
    
    frames = new Frame[2];
    frames[0] = new Frame();
    frames[0].framePixels = new short[(machineType.getTotalScreenWidth() * machineType.getTotalScreenHeight())];
    frames[0].ready = false;
    frames[1] = new Frame();
    frames[1].framePixels = new short[(machineType.getTotalScreenWidth() * machineType.getTotalScreenHeight())];
    frames[1].ready = false;
    
    reset();
  }

  /**
   * Resets the VIC chip to an initial state.
   * 
   * @param machineType The type of VIC chip machine that is being emulated.
   */
  public void reset() {
    horizontalCounter = 0;
    verticalCounter = 0;
    pixelCounter = 0;
    cellDepthCounter = 0;
    videoMatrixCounter = 0;
    rowStart = 0;
    cellData = 0;
    cellColour = 0;
    fetchToggle = FETCH_SCREEN_CODE;
    charMemoryCellDepthStart = charMemoryStart;
  }
  
  /**
   * Reads a value from VIC memory.
   * 
   * @param address The address to read from.
   * 
   * @return The byte at the specified address.
   */
  public int readMemory(int address) {
    int value = 0;

    // Handle all VIC chip memory address ranges, including undocumented ones.
    address = (address & 0xFF0F);

    switch (address) {
      case VIC_REG_0:
        value = mem[address];
        break;
  
      case VIC_REG_1:
        value = mem[address];
        break;
  
      case VIC_REG_2:
        value = mem[address];
        break;
  
      case VIC_REG_3:
        value = mem[address];
        break;
  
      case VIC_REG_4:
        value = mem[address];
        break;
  
      case VIC_REG_5:
        value = mem[address];
        break;
  
      case VIC_REG_6:
        value = mem[address];
        break;
  
      case VIC_REG_7:
        value = mem[address];
        break;
  
      case VIC_REG_8:
        value = mem[address];
        break;
  
      case VIC_REG_9:
        value = mem[address];
        break;
  
      case VIC_REG_10:
        value = mem[address];
        break;
  
      case VIC_REG_11:
        value = mem[address];
        break;
  
      case VIC_REG_12:
        value = mem[address];
        break;
  
      case VIC_REG_13:
        value = mem[address];
        break;
  
      case VIC_REG_14:
        value = mem[address];
        break;
  
      case VIC_REG_15:
        value = mem[address];
        break;
  
      default:
        value = cellData & 0xFF;
    }

    return value;
  }

  /**
   * Writes a value to VIC memory.
   * 
   * @param address The address to write the value to.
   * @param value The value to write into the address.
   */
  public void writeMemory(int address, int value) {
    // This is how the VIC chip is mapped, i.e. each register to multiple addresses.
    address = address & 0xFF0F;
    
    switch (address) {
      case VIC_REG_0: // $9000 Left margin, or horizontal origin (4 pixel granularity)
        mem[address] = value;
        horizontalScreenOrigin = (value & 0x7F);
        textScreenLeft = (horizontalScreenOrigin << 2);
        textScreenRight = textScreenLeft + textScreenWidth;
        break;
  
      case VIC_REG_1: // $9001 Top margin, or vertical origin (2 pixel granularity)
        mem[address] = value;
        verticalScreenOrigin = value;
        textScreenTop = (value << 1);
        textScreenBottom = textScreenTop + textScreenHeight;
        break;
  
      case VIC_REG_2: // $9002 Video Matrix Columns, Video and colour memory
        mem[VIC_REG_2] = value;
        numOfColumns = (value & 0x7f);
        textScreenWidth = (numOfColumns << 3);
        textScreenRight = textScreenLeft + textScreenWidth;
        colourMemoryStart = ((value > 0x80) ? 0x0600 : 0x0400);
        videoMemoryStart = videoMemoryTable[((mem[VIC_REG_5] & 0xF0) >> 3) | ((value & 0x80) >> 7)];
        break;
  
      case VIC_REG_3: // $9003 Video Matrix Rows, Character size
        mem[address] = value;
        switch (value & 0x01) {
        case 0:
          characterSize = 8;
          characterSizeShift = 3;
          break;
  
        case 1:
          characterSize = 16;
          characterSizeShift = 4;
          break;
        }
        numOfRows = (value & 0x7e) >> 1;
        textScreenHeight = characterSize * numOfRows;
        textScreenBottom = textScreenTop + textScreenHeight;
        break;
  
      case VIC_REG_4: // $9004 Raster line counter (READ ONLY)
        break;
  
      case VIC_REG_5: // $9005 Video matrix and char generator base address control
        mem[address] = value;
        videoMemoryStart = videoMemoryTable[((value & 0xF0) >> 3) | ((mem[VIC_REG_2] & 0x80) >> 7)];
        charMemoryStart = charMemoryTable[value & 0x0F];
        charMemoryCellDepthStart = charMemoryStart + cellDepthCounter;
        break;
  
      case VIC_REG_6: // $9006 Light pen X (READ ONLY)
        break;
  
      case VIC_REG_7: // $9007 Light pen Y (READ ONLY)
        break;
  
      case VIC_REG_8: // $9008 Paddle X (READ ONLY)
        break;
  
      case VIC_REG_9: // $9009 Paddle Y (READ ONLY)
        break;
  
      case VIC_REG_10: // $900A Bass sound switch and frequency
        mem[address] = value;
        break;
  
      case VIC_REG_11: // $900B Alto sound switch and frequency
        mem[address] = value;
        break;
  
      case VIC_REG_12: // $900C Soprano sound switch and frequency
        mem[address] = value;
        break;
  
      case VIC_REG_13: // $900D Noise sound switch and frequency
        mem[address] = value;
        break;
  
      case VIC_REG_14: // $900E Auxiliary Colour, Master Volume
        mem[address] = value;
        auxiliaryColour = vicColours[(value & 0xF0) >> 4];
        multiColourTable[3] = auxiliaryColour;
        masterVolume = (15 - (value & 0x0F));
        break;
  
      case VIC_REG_15: // $900F Screen and Border Colours, Reverse Video
        mem[address] = value;
        // Not sure if border colour was support in Attack UFO. No need for it.
        borderColour = vicColours[0];   // vicColours[value & 0x07];
        backgroundColourIndex = (value & 0xF0) >> 4;
        backgroundColour = vicColours[backgroundColourIndex];
        multiColourTable[0] = backgroundColour;
        multiColourTable[1] = borderColour;
        // No reverse mode in Attack UFO.  reverse = ((value & 0x08) == 0x08 ? 0 : 1);
        break;
    }
  }
  
  /**
   * Emulates a cycle where rendering is skipped. This is intended to be used by every cycle
   * in a frame whose rendering is being skipped. All this method does is make sure that the
   * vertical counter register is updated. Everything else is hidden from the CPU, so doesn't
   * need to be updated for a skip frame.
   * 
   * @return true if the frame was completed by the cycle that was emulated.
   */
  public boolean emulateSkipCycle() {
    boolean frameComplete = false;
    
    // Increment the horizontal counter.
    horizontalCounter = horizontalCounter + 4;

    // If end of line is reached, reset horiz counter and increment vert counter.
    if (horizontalCounter >= machineType.getTotalScreenWidth()) {
      horizontalCounter = 0;
      verticalCounter++;

      // If last line has been reached, reset all counters.
      if (verticalCounter >= machineType.getTotalScreenHeight()) {
        verticalCounter = 0;
        frameComplete = true;
      } 

      // Update raster line in VIC registers.
      mem[VIC_REG_4] = (verticalCounter >> 1);
      if ((verticalCounter & 0x01) == 0) {
        mem[VIC_REG_3] &= 0x7F;
      } else {
        mem[VIC_REG_3] |= 0x80;
      }
    }
    
    return frameComplete;
  }

  /**
   * Emulates a single machine cycle. The VIC chip alternates its function
   * between fetching the screen code for a character from the video matrix and
   * fetching the bitmap of the character line from character memory on alternate
   * cycles. Four pixels are output every cycle. Note that the VIC starts fetching
   * the data for character it needs to render during the 2 cycles prior to the dots
   * being sent to the TV. So the border column immediately preceding the left 
   * edge of the video matrix area is when it is fetching the data required for 
   * the first column of the video matrix area.
   * 
   * @return true If a screen repaint is required due to the frame render having completed. 
   */
  public boolean emulateCycle() {
    boolean frameRenderComplete = false;
    int charDataOffset = 0;
    
    // Get a local reference to the current Frame's pixel array.
    short[] framePixels = frames[activeFrame].framePixels;

    // TODO: Verify that this is correct, for both PAL and NTSC. It almost certainly isn't.
    if (verticalCounter > 9) {
      
    // Check that we are inside the text screen.
    if ((verticalCounter >= textScreenTop) && (verticalCounter < textScreenBottom) && (horizontalCounter >= textScreenLeft) && (horizontalCounter < textScreenRight)) {

      // Determine whether we are fetching screen code or char data.
      if (fetchToggle == FETCH_SCREEN_CODE) {
        
        // Calculate address within video memory and fetch cell index.
        cellData = mem[videoMemoryStart + videoMatrixCounter];

        // Due to the way the colour memory is wired up, the above fetch of the cell index
        // also happens to automatically fetch the foreground colour from the Colour Matrix
        // via the top 4 lines of the data bus (DB8-DB11), which are wired directly from 
        // colour RAM in to the VIC chip.
        cellColourIndex = mem[colourMemoryStart + videoMatrixCounter] & 0x0F;
        cellColour = vicColours[cellColourIndex];

        // Increment the video matrix counter.
        videoMatrixCounter++;

        // Toggle fetch toggle.
        fetchToggle = FETCH_CHAR_DATA;
        
        return frameRenderComplete;
        
      } else {
        // Calculate offset of data.
        charDataOffset = charMemoryCellDepthStart + (cellData << characterSizeShift);

        // Fetch cell data.
        cellData = mem[charDataOffset];

        // Plot pixels.
        framePixels[pixelCounter++] = ((cellData & 0x80) == 0 ? backgroundColour : cellColour);
        framePixels[pixelCounter++] = ((cellData & 0x40) == 0 ? backgroundColour : cellColour);
        framePixels[pixelCounter++] = ((cellData & 0x20) == 0 ? backgroundColour : cellColour);
        framePixels[pixelCounter++] = ((cellData & 0x10) == 0 ? backgroundColour : cellColour);

        horizontalCounter = horizontalCounter + 4;

        if (horizontalCounter < machineType.getTotalScreenWidth()) {
          framePixels[pixelCounter++] = ((cellData & 0x08) == 0 ? backgroundColour : cellColour);
          framePixels[pixelCounter++] = ((cellData & 0x04) == 0 ? backgroundColour : cellColour);
          framePixels[pixelCounter++] = ((cellData & 0x02) == 0 ? backgroundColour : cellColour);
          framePixels[pixelCounter++] = ((cellData & 0x01) == 0 ? backgroundColour : cellColour);
        }

        // Toggle fetch toggle.
        fetchToggle = FETCH_SCREEN_CODE;
      }
    } else {
      cellData = mem[DEFAULT_FETCH_ADDRESS];

      // Output four border pixels.
      framePixels[pixelCounter++] = borderColour;
      framePixels[pixelCounter++] = borderColour;
      framePixels[pixelCounter++] = borderColour;
      framePixels[pixelCounter++] = borderColour;
    }
    
    } else {
      // Vertical blanking is in progress. Not pixels are output during this time.
    }

    // Increment the horizontal counter.
    horizontalCounter = horizontalCounter + 4;

    // If end of line is reached, reset horiz counter and increment vert
    // counter.
    if (horizontalCounter >= machineType.getTotalScreenWidth()) {
      horizontalCounter = 0;
      verticalCounter++;

      // If last line has been reached, reset all counters.
      if (verticalCounter >= machineType.getTotalScreenHeight()) {
        verticalCounter = 0;
        pixelCounter = 0;
        videoMatrixCounter = 0;
        rowStart = 0;
        cellDepthCounter = 0;
        charMemoryCellDepthStart = charMemoryStart;
        
        synchronized(frames) {
          // Mark the current frame as complete.
          frames[activeFrame].ready = true;
          
          // Toggle the active frame.
          activeFrame = ((activeFrame + 1) % 2);
          frames[activeFrame].ready = false;
        }
        
        frameRenderComplete = true;
        
      } else {
        if (videoMatrixCounter > 0) {
          cellDepthCounter++;

          if (cellDepthCounter == characterSize) {
            // Advance to the next row of characters in text window.
            cellDepthCounter = 0;
            videoMatrixCounter = rowStart + numOfColumns;
            rowStart = videoMatrixCounter;
            charMemoryCellDepthStart = charMemoryStart;
          } else {
            // Reset the video matrix to beginning of current row.
            videoMatrixCounter = rowStart;
            charMemoryCellDepthStart = charMemoryStart + cellDepthCounter;
          }
        }
      }

      // Update raster line in VIC registers.
      mem[VIC_REG_4] = (verticalCounter >> 1);
      if ((verticalCounter & 0x01) == 0) {
        mem[VIC_REG_3] &= 0x7F;
      } else {
        mem[VIC_REG_3] |= 0x80;
      }
    }
    
    return frameRenderComplete;
  }

  /**
   * Gets the pixels for the current frame from the VIC chip.
   * 
   * @return The pixels for the current frame. Returns null if there isn't one that is ready.
   */
  public short[] getFramePixels() {
    short[] framePixels = null;
    synchronized (frames) {
      Frame nonActiveFrame = frames[((activeFrame + 1) % 2)];
      if (nonActiveFrame.ready) {
        nonActiveFrame.ready = false;
        framePixels = nonActiveFrame.framePixels;
      }
    }
    return framePixels;
  }
}
