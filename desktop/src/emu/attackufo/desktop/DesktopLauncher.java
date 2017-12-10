package emu.attackufo.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import emu.attackufo.AttackUFOGame;
import emu.attackufo.ui.ConfirmHandler;
import emu.attackufo.ui.ConfirmResponseHandler;

/**
 * Launches the Desktop version of the Attack UFO emulator.
 * 
 * @author Lance Ewing
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.width = 540;
    config.height = 900;
    config.title = "Attack UFO (1980) by Ryoto Electric Co.";
    new LwjglApplication(new AttackUFOGame(new ConfirmHandler() {

      @Override
      public void confirm(String message, ConfirmResponseHandler confirmResponseHandler) {
      }
      
    }), config);
	}
}
