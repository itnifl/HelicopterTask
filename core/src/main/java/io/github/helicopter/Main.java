package io.github.helicopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main game class that manages the helicopter sprite movement and rendering.
 * The helicopter bounces off screen edges and faces its direction of travel.
 */
public class Main extends ApplicationAdapter {

    // Asset paths
    private static final String HELICOPTER_TEXTURE_PATH = "attackhelicopter.PNG";

    // Movement configuration
    private static final float INITIAL_VELOCITY_X = 200f;
    private static final float INITIAL_VELOCITY_Y = 150f;

    // Background color
    private static final Color BACKGROUND_COLOR = new Color(0.15f, 0.15f, 0.2f, 1f);

    // Rendering
    private SpriteBatch batch;
    private Texture helicopterTexture;
    private Sprite helicopterSprite;

    // Physics
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();

    @Override
    public void create() {
        initializeGraphics();
        initializeHelicopter();
    }

    private void initializeGraphics() {
        batch = new SpriteBatch();
    }

    private void initializeHelicopter() {
        helicopterTexture = new Texture(HELICOPTER_TEXTURE_PATH);
        helicopterSprite = new Sprite(helicopterTexture);
        helicopterSprite.setOriginCenter();

        velocity.set(INITIAL_VELOCITY_X, INITIAL_VELOCITY_Y);
        centerHelicopterOnScreen();
    }

    private void centerHelicopterOnScreen() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        position.x = (screenWidth - helicopterSprite.getWidth()) / 2f;
        position.y = (screenHeight - helicopterSprite.getHeight()) / 2f;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        update(deltaTime);
        draw();
    }

    private void update(float deltaTime) {
        updatePosition(deltaTime);
        handleScreenBounce();
        updateSpriteState();
    }

    private void updatePosition(float deltaTime) {
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
    }

    private void handleScreenBounce() {
        handleHorizontalBounce();
        handleVerticalBounce();
    }

    private void handleHorizontalBounce() {
        float screenWidth = Gdx.graphics.getWidth();
        float spriteWidth = helicopterSprite.getWidth();

        if (position.x < 0) {
            position.x = 0;
            velocity.x = Math.abs(velocity.x);
        } else if (position.x + spriteWidth > screenWidth) {
            position.x = screenWidth - spriteWidth;
            velocity.x = -Math.abs(velocity.x);
        }
    }

    private void handleVerticalBounce() {
        float screenHeight = Gdx.graphics.getHeight();
        float spriteHeight = helicopterSprite.getHeight();

        if (position.y < 0) {
            position.y = 0;
            velocity.y = Math.abs(velocity.y);
        } else if (position.y + spriteHeight > screenHeight) {
            position.y = screenHeight - spriteHeight;
            velocity.y = -Math.abs(velocity.y);
        }
    }

    private void updateSpriteState() {
        helicopterSprite.setPosition(position.x, position.y);
        updateSpriteDirection();
    }

    private void updateSpriteDirection() {
        boolean facingLeft = velocity.x < 0;
        helicopterSprite.setFlip(facingLeft, false);
    }

    private void draw() {
        clearScreen();
        renderSprites();
    }

    private void clearScreen() {
        ScreenUtils.clear(BACKGROUND_COLOR);
    }

    private void renderSprites() {
        batch.begin();
        helicopterSprite.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (helicopterTexture != null) {
            helicopterTexture.dispose();
        }
    }
}
