package io.github.helicopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

/**
 * Main application class that manages different game screens.
 */
public class Main extends ApplicationAdapter {

    // Screen constants
    public static final int SCREEN_MENU = 0;
    public static final int SCREEN_HELICOPTER = 1;
    public static final int SCREEN_PONG = 2;

    // Screens
    private MainMenuScreen menuScreen;
    private HelicopterScreen helicopterScreen;
    private PongScreen pongScreen;

    // Current screen
    private GameScreen currentScreen;
    private int currentScreenId = SCREEN_MENU;

    @Override
    public void create() {
        menuScreen = new MainMenuScreen(this);
        helicopterScreen = new HelicopterScreen(this);
        pongScreen = new PongScreen(this);

        setScreen(SCREEN_MENU);
    }

    public void setScreen(int screenId) {
        // Hide current screen
        if (currentScreen != null) {
            currentScreen.hide();
        }

        // Set new screen
        currentScreenId = screenId;
        switch (screenId) {
            case SCREEN_MENU:
                currentScreen = menuScreen;
                break;
            case SCREEN_HELICOPTER:
                currentScreen = helicopterScreen;
                break;
            case SCREEN_PONG:
                currentScreen = pongScreen;
                break;
            default:
                currentScreen = menuScreen;
                break;
        }

        // Show new screen
        currentScreen.show();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (currentScreen != null) {
            currentScreen.render(delta);
        }
    }

    @Override
    public void dispose() {
        if (menuScreen != null) menuScreen.dispose();
        if (helicopterScreen != null) helicopterScreen.dispose();
        if (pongScreen != null) pongScreen.dispose();
    }
}
