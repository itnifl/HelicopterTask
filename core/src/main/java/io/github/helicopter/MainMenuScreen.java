package io.github.helicopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements GameScreen {
    private final Main game;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont menuFont;
    private GlyphLayout layout;
    private int selectedOption = 0;

    private static final String[] MENU_OPTIONS = {"1. Helicopter Game", "2. Pong Game", "3. Exit"};
    private static final Color BACKGROUND_COLOR = new Color(0.1f, 0.1f, 0.15f, 1f);

    public MainMenuScreen(Main game) { this.game = game; }

    @Override
    public void show() {
        batch = new SpriteBatch();
        titleFont = new BitmapFont();
        titleFont.setColor(Color.CYAN);
        titleFont.getData().setScale(3f);
        menuFont = new BitmapFont();
        menuFont.getData().setScale(2f);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        handleInput();
        ScreenUtils.clear(BACKGROUND_COLOR);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        batch.begin();
        String title = "GAME MENU";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, (screenWidth - layout.width) / 2f, screenHeight - 100);
        float menuStartY = screenHeight / 2f + 50;
        for (int i = 0; i < MENU_OPTIONS.length; i++) {
            menuFont.setColor(i == selectedOption ? Color.YELLOW : Color.WHITE);
            layout.setText(menuFont, MENU_OPTIONS[i]);
            menuFont.draw(batch, MENU_OPTIONS[i], (screenWidth - layout.width) / 2f, menuStartY - (i * 50f));
        }
        menuFont.setColor(Color.GRAY);
        menuFont.getData().setScale(1f);
        String instr = "UP/DOWN to navigate, ENTER to select";
        layout.setText(menuFont, instr);
        menuFont.draw(batch, instr, (screenWidth - layout.width) / 2f, 80);
        menuFont.getData().setScale(2f);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) selectedOption = (selectedOption - 1 + MENU_OPTIONS.length) % MENU_OPTIONS.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selectedOption = (selectedOption + 1) % MENU_OPTIONS.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) selectOption();
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) { selectedOption = 0; selectOption(); }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) { selectedOption = 1; selectOption(); }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) { selectedOption = 2; selectOption(); }
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0: game.setScreen(Main.SCREEN_HELICOPTER); break;
            case 1: game.setScreen(Main.SCREEN_PONG); break;
            case 2: Gdx.app.exit(); break;
        }
    }

    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (menuFont != null) menuFont.dispose();
    }
}
