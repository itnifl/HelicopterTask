package io.github.helicopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Locale;

/**
 * Main game class that manages the helicopter sprite movement and rendering.
 * The helicopter bounces off screen edges with animated rotor blades.
 * User can control movement with touch/mouse input.
 */
public class Main extends ApplicationAdapter {

    // Asset paths
    private static final String[] HELICOPTER_FRAME_PATHS = {
        "heli1.png", "heli2.png", "heli3.png", "heli4.png"
    };

    // Animation configuration
    private static final float FRAME_DURATION = 0.1f; // 100ms per frame

    // Movement configuration
    private static final float INITIAL_VELOCITY_X = 200f;
    private static final float INITIAL_VELOCITY_Y = 150f;
    private static final float MOVEMENT_SPEED = 300f;
    private static final float ARRIVAL_THRESHOLD = 5f;

    // UI configuration
    private static final float TEXT_PADDING = 10f;

    // Background color
    private static final Color BACKGROUND_COLOR = new Color(0.15f, 0.15f, 0.2f, 1f);

    // Rendering
    private SpriteBatch batch;
    private Texture[] helicopterTextures;
    private Animation<TextureRegion> helicopterAnimation;
    private BitmapFont font;
    private float stateTime = 0f;

    // Physics
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 targetPosition = new Vector2();
    private boolean isUserControlling = false;

    // Sprite dimensions (130x52 as per task description)
    private static final int FRAME_WIDTH = 130;
    private static final int FRAME_HEIGHT = 52;

    // Track direction for sprite facing
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
        // Load all frame textures
        helicopterTextures = new Texture[HELICOPTER_FRAME_PATHS.length];
        TextureRegion[] frames = new TextureRegion[HELICOPTER_FRAME_PATHS.length];

        for (int i = 0; i < HELICOPTER_FRAME_PATHS.length; i++) {
            helicopterTextures[i] = new Texture(HELICOPTER_FRAME_PATHS[i]);
            frames[i] = new TextureRegion(helicopterTextures[i]);
        }

        // Create looping animation
        helicopterAnimation = new Animation<>(FRAME_DURATION, frames);
        helicopterAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Initialize velocity
        velocity.set(INITIAL_VELOCITY_X, INITIAL_VELOCITY_Y);

        // Center helicopter on screen
        centerHelicopterOnScreen();
        targetPosition.set(position);
    }

    private void centerHelicopterOnScreen() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        position.x = (screenWidth - FRAME_WIDTH) / 2f;
        position.y = (screenHeight - FRAME_HEIGHT) / 2f;
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
            targetPosition.x = touchX - FRAME_WIDTH / 2f;
            targetPosition.y = touchY - FRAME_HEIGHT / 2f;
            isUserControlling = true;
        }
    }

    private void update(float deltaTime) {
        // Update animation time
        stateTime += deltaTime;

        if (isUserControlling) {
            moveTowardsTarget(deltaTime);
        } else {
            // Update position based on velocity (bouncing mode)
            updatePosition(deltaTime);
            // Handle bouncing off screen edges
            handleScreenBounce();
        }

        // Always clamp to screen
        clampToScreen();

        // Update facing direction based on velocity
        updateFacingDirection();
    }

    private void moveTowardsTarget(float deltaTime) {
        float deltaX = targetPosition.x - position.x;
        float deltaY = targetPosition.y - position.y;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance < ARRIVAL_THRESHOLD) {
            position.set(targetPosition);
            isUserControlling = false;
            // Helicopter continues in the last direction user dragged it
            return;
        }

        // Normalize and apply speed
        float moveDistance = MOVEMENT_SPEED * deltaTime;
        if (moveDistance > distance) {
            moveDistance = distance;
        }

        float moveX = (deltaX / distance) * moveDistance;
        float moveY = (deltaY / distance) * moveDistance;

        // Update velocity to match movement direction (for continuing after release)
        // Use the actual movement direction to set velocity
        if (Math.abs(deltaX) > ARRIVAL_THRESHOLD) {
            velocity.x = deltaX > 0 ? Math.abs(INITIAL_VELOCITY_X) : -Math.abs(INITIAL_VELOCITY_X);
        }
        if (Math.abs(deltaY) > ARRIVAL_THRESHOLD) {
            velocity.y = deltaY > 0 ? Math.abs(INITIAL_VELOCITY_Y) : -Math.abs(INITIAL_VELOCITY_Y);
        }

        position.x += moveX;
        position.y += moveY;
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

        if (position.x < 0) {
            position.x = 0;
            velocity.x = Math.abs(velocity.x);
        } else if (position.x + FRAME_WIDTH > screenWidth) {
            position.x = screenWidth - FRAME_WIDTH;
            velocity.x = -Math.abs(velocity.x);
        }
    }

    private void handleVerticalBounce() {
        float screenHeight = Gdx.graphics.getHeight();

        if (position.y < 0) {
            position.y = 0;
            velocity.y = Math.abs(velocity.y);
        } else if (position.y + FRAME_HEIGHT > screenHeight) {
            position.y = screenHeight - FRAME_HEIGHT;
            velocity.y = -Math.abs(velocity.y);
        }
    }

    private void clampToScreen() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        position.x = Math.max(0, Math.min(position.x, screenWidth - FRAME_WIDTH));
        position.y = Math.max(0, Math.min(position.y, screenHeight - FRAME_HEIGHT));
    }

    private void updateFacingDirection() {
        // Facing left when moving right (velocity.x > 0) based on sprite orientation
        facingLeft = velocity.x > 0;
    }

    private void draw() {
        clearScreen();
        renderHelicopter();
        renderUI();
    }

    private void clearScreen() {
        ScreenUtils.clear(BACKGROUND_COLOR);
    }

    private void renderHelicopter() {
        // Get current animation frame
        TextureRegion currentFrame = helicopterAnimation.getKeyFrame(stateTime);

        batch.begin();

        // Flip the texture region if facing left, then draw normally
        boolean isFlippedX = currentFrame.isFlipX();
        if (facingLeft != isFlippedX) {
            currentFrame.flip(true, false);
        }

        batch.draw(currentFrame, position.x, position.y, FRAME_WIDTH, FRAME_HEIGHT);

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
        if (helicopterTextures != null) {
            for (Texture texture : helicopterTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }
        if (font != null) {
            font.dispose();
        }
    }
}
