package io.github.helicopter;

/**
 * Interface for game screens to allow switching between games.
 */
public interface GameScreen {
    void show();
    void render(float delta);
    void hide();
    void dispose();
}
