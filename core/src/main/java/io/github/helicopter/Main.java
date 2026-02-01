package io.github.helicopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Locale;

/**
 * Main game class that manages the helicopter sprite movement and rendering.
 * The helicopter follows touch/mouse input and displays its position on screen.
 */
public class Main extends ApplicationAdapter {

    // Asset paths
    private static final String HELICOPTER_TEXTURE_PATH = "attackhelicopter.PNG";

    // Movement configuration
    private static final float MOVEMENT_SPEED = 300f;
    private static final float ARRIVAL_THRESHOLD = 5f;

    // UI configuration
    private static final float TEXT_PADDING = 10f;

    // Background color
    private static final Color BACKGROUND_COLOR = new Color(0.15f, 0.15f, 0.2f, 1f);

    // Rendering
    private SpriteBatch batch;
    private Texture helicopterTexture;
    private Sprite helicopterSprite;
    private BitmapFont font;

    // Physics
    private final Vector2 position = new Vector2();
    private final Vector2 targetPosition = new Vector2();
    private boolean isMoving = false;

    // Track last horizontal direction for sprite facing
    private boolean facingLeft = false;

    @Override
    public void create() {
        initializeGraphics();
        initializeHelicopter();
    }

    private void initializeGraphics() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    private void initializeHelicopter() {
        helicopterTexture = new Texture(HELICOPTER_TEXTURE_PATH);
        helicopterSprite = new Sprite(helicopterTexture);
        helicopterSprite.setOriginCenter();

        centerHelicopterOnScreen();
        targetPosition.set(position);
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

        handleInput();
        update(deltaTime);
        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Convert to world coordinates

            // Set target to center the sprite on touch position
            targetPosition.x = touchX - helicopterSprite.getWidth() / 2f;
            targetPosition.y = touchY - helicopterSprite.getHeight() / 2f;
            isMoving = true;
        }
    }

    private void update(float deltaTime) {
        if (isMoving) {
            moveTowardsTarget(deltaTime);
        }
        clampToScreen();
        updateSpriteState();
    }

    private void moveTowardsTarget(float deltaTime) {
        float deltaX = targetPosition.x - position.x;
        float deltaY = targetPosition.y - position.y;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance < ARRIVAL_THRESHOLD) {
            position.set(targetPosition);
            isMoving = false;
            return;
        }

        // Normalize and apply speed
        float moveDistance = MOVEMENT_SPEED * deltaTime;
        if (moveDistance > distance) {
            moveDistance = distance;
        }

        float moveX = (deltaX / distance) * moveDistance;
        float moveY = (deltaY / distance) * moveDistance;

        // Update facing direction based on horizontal movement
        if (Math.abs(deltaX) > ARRIVAL_THRESHOLD) {
            facingLeft = deltaX > 0;
        }

        position.x += moveX;
        position.y += moveY;
    }

    private void clampToScreen() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float spriteWidth = helicopterSprite.getWidth();
        float spriteHeight = helicopterSprite.getHeight();

        position.x = Math.max(0, Math.min(position.x, screenWidth - spriteWidth));
        position.y = Math.max(0, Math.min(position.y, screenHeight - spriteHeight));

        // Also clamp target position to prevent helicopter from trying to go off-screen
        targetPosition.x = Math.max(0, Math.min(targetPosition.x, screenWidth - spriteWidth));
        targetPosition.y = Math.max(0, Math.min(targetPosition.y, screenHeight - spriteHeight));
    }

    private void updateSpriteState() {
        helicopterSprite.setPosition(position.x, position.y);
        helicopterSprite.setFlip(facingLeft, false);
    }

    private void draw() {
        clearScreen();
        renderSprites();
        renderUI();
    }

    private void clearScreen() {
        ScreenUtils.clear(BACKGROUND_COLOR);
    }

    private void renderSprites() {
        batch.begin();
        helicopterSprite.draw(batch);
        batch.end();
    }

    private void renderUI() {
        float screenHeight = Gdx.graphics.getHeight();
        String positionText = String.format(Locale.US, "Position: (%.0f, %.0f)", position.x, position.y);

        batch.begin();
        font.draw(batch, positionText, TEXT_PADDING, screenHeight - TEXT_PADDING);
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
        if (font != null) {
            font.dispose();
        }
    }
}
