package emu.attackufo;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import emu.attackufo.ui.ConfirmHandler;
import emu.attackufo.ui.MachineInputProcessor;
import emu.attackufo.ui.ViewportManager;

/**
 * The main screen in the Attack UFO emulator, i.e. the one that shows the video 
 * output of the VIC.
 * 
 * @author Lance Ewing
 */
public class MachineScreen implements Screen {

  /**
   * The Game object for AttackUFOGame. Allows us to easily change screens.
   */
  private AttackUFOGame game;
  
  /**
   * This represents the Attack UFO machine.
   */
  private Machine machine;

  /**
   * The Thread that updates the machine at the expected rate.
   */
  private MachineRunnable machineRunnable;
  
  /**
   * The InputProcessor for the MachineScreen. Handles the key and touch input.
   */
  private MachineInputProcessor machineInputProcessor;
  
  /**
   * SpriteBatch shared by all rendered components.
   */
  private SpriteBatch batch;
  
  // Currently in use components to support rendering of the Attack UFO screen. The objects 
  // that these references point to will change depending on the MachineType.
  private Pixmap screenPixmap;
  private Viewport viewport;
  private Camera camera;
  private Texture[] screens;
  private int drawScreen = 1;
  private int updateScreen = 0;
  
  // Screen resources for each MachineType.
  private Map<MachineType, Pixmap> machineTypePixmaps;
  private Map<MachineType, Camera> machineTypeCameras;
  private Map<MachineType, Viewport> machineTypeViewports;
  private Map<MachineType, Texture[]> machineTypeTextures;

  private ViewportManager viewportManager;
  
  /**
   * Constructor for MachineScreen.
   * 
   * @param game The AttackUFOGame instance.
   * @param confirmHandler
   */
  public MachineScreen(AttackUFOGame game, ConfirmHandler confirmHandler) {
    this.game = game;
    
    // Create the Machine, at this point not configured with a MachineType.
    this.machine = new Machine();
    this.machineRunnable = new MachineRunnable(this.machine);
    
    batch = new SpriteBatch();
    
    machineTypePixmaps = new HashMap<MachineType, Pixmap>();
    machineTypeTextures = new HashMap<MachineType, Texture[]>();
    machineTypeViewports = new HashMap<MachineType, Viewport>();
    machineTypeCameras = new HashMap<MachineType, Camera>();
    
    createScreenResourcesForMachineType(MachineType.PAL);
    createScreenResourcesForMachineType(MachineType.NTSC);
    
    viewportManager = ViewportManager.getInstance();
    
    // Create and register an input processor for keys, etc.
    machineInputProcessor = new MachineInputProcessor(this, confirmHandler);
    
    // Start up the MachineRunnable Thread. It will initially be paused, awaiting machine configuration.
    Thread machineThread = new Thread(this.machineRunnable);
    machineThread.start();
  }
  
  /**
   * Initialises the Machine with the given AppConfigItem. This will represent an app that was
   * selected on the HomeScreen. As part of this initialisation, it creates the Pixmap, screen
   * Textures, Camera and Viewport required to render the Attack UFO screen at the size needed for
   * the MachineType being emulated.
   * 
   * @param machineType The type of VIC chip machine type, i.e. NTSC or PAL.
   */
  public void initMachine(MachineType machineType) {
    machine.init(machineType);
    
    // Switch libGDX screen resources used by the Attack UFO screen to the size required by the MachineType.
    screenPixmap = machineTypePixmaps.get(machineType);
    screens = machineTypeTextures.get(machineType);
    camera = machineTypeCameras.get(machineType);
    viewport = machineTypeViewports.get(machineType);
    
    drawScreen = 1;
    updateScreen = 0;
  }
  
  /**
   * Creates the libGDX screen resources required for the given MachineType.
   * 
   * @param machineType The MachineType to create the screen resources for.
   */
  private void createScreenResourcesForMachineType(MachineType machineType) {
    // Create the libGDX screen resources used by the Attack UFO screen to the size required by the MachineType.
    Pixmap screenPixmap = new Pixmap(machineType.getTotalScreenWidth(), machineType.getTotalScreenHeight(), Pixmap.Format.RGB565);
    Texture[] screens = new Texture[3];
    screens[0] = new Texture(screenPixmap, Pixmap.Format.RGB565, false);
    screens[1] = new Texture(screenPixmap, Pixmap.Format.RGB565, false);
    screens[2] = new Texture(screenPixmap, Pixmap.Format.RGB565, false);
    Camera camera = new OrthographicCamera();
    Viewport viewport = new ExtendViewport((machineType.getTotalScreenWidth() / 3) * 4, (int)(machineType.getTotalScreenWidth() * 2.315), camera);
    
    machineTypePixmaps.put(machineType, screenPixmap);
    machineTypeTextures.put(machineType, screens);
    machineTypeCameras.put(machineType, camera);
    machineTypeViewports.put(machineType, viewport);
  }
  
  private long lastLogTime;
  private long avgRenderTime;
  private long avgDrawTime;
  private long renderCount;
  private long drawCount;
  
  @Override
  public void render(float delta) {
    long renderStartTime = TimeUtils.nanoTime();
    long fps = Gdx.graphics.getFramesPerSecond();
    long maxFrameDuration = (long)(1000000000L * (fps == 0? 0.016667f : delta));
    boolean draw = false;
    
    if (machine.isPaused()) {
      // When paused, we limit the draw frequency since there isn't anything to change.
      draw = ((fps < 30) || ((renderCount % (fps/30)) == 0));
      
    } else {
      // Check if the Machine has a frame ready to be displayed.
      short[] framePixels = machine.getFramePixels();
      if (framePixels != null) {
        // If it does then update the Texture on the GPU.
        BufferUtils.copy(framePixels, 0, screenPixmap.getPixels(), 
            machine.getMachineType().getTotalScreenWidth() * machine.getMachineType().getTotalScreenHeight());
        screens[updateScreen].draw(screenPixmap, 0, 0);
        updateScreen = (updateScreen + 1) % 3;
        drawScreen = (drawScreen + 1) % 3;
      }
      
      draw = true;
    }
    
    if (draw) {
      drawCount++;
      draw();
      long drawDuration = TimeUtils.nanoTime() - renderStartTime;
      if (renderCount == 0) {
        avgDrawTime = drawDuration;
      } else {
        avgDrawTime = ((avgDrawTime * renderCount) + drawDuration) / (renderCount + 1);
      }
    }
    
    long renderDuration = TimeUtils.nanoTime() - renderStartTime;
    if (renderCount == 0) {
      avgRenderTime = renderDuration;
    } else {
      avgRenderTime = ((avgRenderTime * renderCount) + renderDuration) / (renderCount + 1);
    }
    
    renderCount++;
    
    if ((lastLogTime == 0) || (renderStartTime - lastLogTime > 10000000000L)) {
      lastLogTime = renderStartTime;
      //Gdx.app.log("RenderTime", String.format(
      //    "[%d] avgDrawTime: %d avgRenderTime: %d maxFrameDuration: %d delta: %f fps: %d", 
      //    drawCount, avgDrawTime, avgRenderTime, maxFrameDuration, delta, Gdx.graphics.getFramesPerSecond()));
    }
  }

  private void draw() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    
    // Render the VIC screen.
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    batch.disableBlending();
    batch.begin();
    Color c = batch.getColor();
    batch.setColor(c.r, c.g, c.b, 1f);
    batch.draw(screens[drawScreen], 
        0, -140,
        machine.getScreenWidth() / 2, machine.getScreenHeight() / 2,
        machine.getScreenWidth(), machine.getScreenHeight(), 
        2.0f, 2.0f, 90.0f,
        machine.getScreenLeft(), machine.getScreenTop(), 
        machine.getMachineType().getVisibleScreenWidth(), 
        machine.getMachineType().getVisibleScreenHeight(), 
        false, false);
    batch.end();

    // Render the UI elements, e.g. the keyboard and joystick icons.
    viewportManager.getCurrentCamera().update();
    batch.setProjectionMatrix(viewportManager.getCurrentCamera().combined);
    batch.enableBlending();
    batch.begin();
    batch.end();
  }
  
  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    
    // Align VIC screen's top edge to top of the viewport.
    Camera camera = viewport.getCamera();
    camera.position.x = machine.getScreenWidth() /2;
    camera.position.y = machine.getScreenHeight() - viewport.getWorldHeight()/2;
    camera.update();
    
    machineInputProcessor.resize(width, height);
    viewportManager.update(width, height);
  }

  @Override
  public void pause() {
    // On Android, this is also called when the "Home" button is pressed.
    machineRunnable.pause();
  }

  @Override
  public void resume() {
    machineRunnable.resume();
  }
  
  @Override
  public void show() {
    // Note that this screen should not be shown unless the Machine has been initialised by calling
    // the initMachine method of MachineScreen. This will create the necessary PixMap and Textures 
    // required for the MachineType.
    Gdx.input.setInputProcessor(machineInputProcessor);
    machineRunnable.resume();
  }
  
  @Override
  public void hide() {
    // On Android, this is also called when the "Back" button is pressed.
  }

  @Override
  public void dispose() {
    batch.dispose();
    machineRunnable.stop();
    disposeScreens();
  }
  
  /**
   * Disposes the libGDX screen resources for each MachineType.
   */
  private void disposeScreens() {
    //Gdx.app.log("MachineScreen", "Disposing screens");
    for (Pixmap pixmap : machineTypePixmaps.values()) {
      pixmap.dispose();
    }
    for (Texture[] screens : machineTypeTextures.values()) {
      screens[0].dispose();
      screens[1].dispose();
      screens[2].dispose();
    }
  }
  
  /**
   * Gets the Machine that this MachineScreen is running.
   *  
   * @return The Machine that this MachineScreen is running.
   */
  public Machine getMachine() {
    return machine;
  }
  
  /**
   * Gets the MachineRunnable that is running the Machine.
   * 
   * @return The MachineRunnable that is running the Machine.
   */
  public MachineRunnable getMachineRunnable() {
    return machineRunnable;
  }
}
