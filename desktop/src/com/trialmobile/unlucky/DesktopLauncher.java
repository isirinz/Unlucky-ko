package com.trialmobile.unlucky;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.trialmobile.unlucky.main.Unlucky;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher implements AppInterface {
	public static void main (String[] arg) {
		DesktopLauncher main = new DesktopLauncher();
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle(Unlucky.TITLE);
		config.setWindowedMode(Unlucky.V_WIDTH * Unlucky.V_SCALE, 960);
		config.setForegroundFPS(60);
		config.setResizable(false);
		config.useVsync(false);
		config.setWindowIcon(Files.FileType.Internal, "desktop_icon128.png");
		new Lwjgl3Application(new UnluckyMain(main), config);
	}

	@Override
	public void openOfferwall(String userId) {
	}

	@Override
	public void showBanner(boolean show) {
	}

	@Override
	public void showVideo(VideoCallback callback) {

	}
}
