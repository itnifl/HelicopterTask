package io.github.helicopter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
    private static final String[] EXPLOSION_FRAME_PATHS = {
        "attackhelicopter-exploding1.png", "attackhelicopter-exploding2.png"
    };
    private static final String GUN_READY_TEXTURE_PATH = "1942gun-ready.png";
    private static final String GUN_FIRES_TEXTURE_PATH = "1942gun-fires.png";

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
    private Texture[] explosionTextures;
    private Texture gunReadyTexture;
    private Texture gunFiresTexture;
    private Animation<TextureRegion> helicopterAnimation;
    private BitmapFont font;
    private float stateTime = 0f;

    // Explosion state
    private boolean isExploded = false;
    private boolean isFalling = false;
    private Texture currentExplosionTexture;

    // Gun position and firing state
    private final Vector2 gunPosition = new Vector2();
    private boolean isGunFiring = false;
    private float gunFireTimer = 0f;
    private float nextFireTime = 0f;
    private float fireDisplayTimer = 0f;

    // Bullet state (invisible projectile)
    private boolean isBulletActive = false;
    private float bulletY = 0f;
    private static final float BULLET_SPEED = 800f; // Pixels per second

    // Physics
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 targetPosition = new Vector2();
    private boolean isUserControlling = false;

    // Sprite dimensions (130x52 as per task description)
    private static final int FRAME_WIDTH = 130;
    private static final int FRAME_HEIGHT = 52;

    // Gun dimensions
    private static final int GUN_WIDTH = 50;
    private static final int GUN_HEIGHT = 120;

    // Gun firing configuration
    private static final float MIN_FIRE_INTERVAL = 1.0f;  // Minimum time between shots
    private static final float MAX_FIRE_INTERVAL = 3.0f;  // Maximum time between shots
    private static final float FIRE_DISPLAY_DURATION = 0.15f; // How long to show firing sprite

    // Explosion configuration
    private static final float FALL_SPEED = 400f; // Pixels per second when falling

    // Track direction for sprite facing
    private boolean facingLeft = false;

    @Override
    public void create() {
        initializeGraphics();
        initializeHelicopter();
        initializeGun();
    }

    private void initializeGraphics() {
        batch = new SpriteBatch();
        batch.enableBlending();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    private void initializeHelicopter() {
        // Load all frame textures with magenta transparency
        helicopterTextures = new Texture[HELICOPTER_FRAME_PATHS.length];
        TextureRegion[] frames = new TextureRegion[HELICOPTER_FRAME_PATHS.length];

        for (int i = 0; i < HELICOPTER_FRAME_PATHS.length; i++) {
            helicopterTextures[i] = loadTextureWithTransparency(HELICOPTER_FRAME_PATHS[i]);
            frames[i] = new TextureRegion(helicopterTextures[i]);
        }

        // Load explosion textures
        explosionTextures = new Texture[EXPLOSION_FRAME_PATHS.length];
        for (int i = 0; i < EXPLOSION_FRAME_PATHS.length; i++) {
            explosionTextures[i] = loadTextureWithTransparency(EXPLOSION_FRAME_PATHS[i]);
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

    private void initializeGun() {
        gunReadyTexture = loadTextureWithTransparency(GUN_READY_TEXTURE_PATH);
        gunFiresTexture = loadTextureWithTransparency(GUN_FIRES_TEXTURE_PATH);

        // Position gun at bottom middle of screen
        float screenWidth = Gdx.graphics.getWidth();
        gunPosition.x = (screenWidth - GUN_WIDTH) / 2f;
        gunPosition.y = 0;

        // Set initial random fire time
        nextFireTime = getRandomFireInterval();
    }

    private float getRandomFireInterval() {
        return MIN_FIRE_INTERVAL + (float) Math.random() * (MAX_FIRE_INTERVAL - MIN_FIRE_INTERVAL);
    }

    /**
     * Loads a texture and replaces the magenta (255, 0, 255) color with transparent pixels.
     */
    private Texture loadTextureWithTransparency(String path) {
        Pixmap originalPixmap = new Pixmap(Gdx.files.internal(path));

        // Always convert to RGBA8888 for consistent alpha handling
        Pixmap pixmap = new Pixmap(originalPixmap.getWidth(), originalPixmap.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.drawPixmap(originalPixmap, 0, 0);
        originalPixmap.dispose();

        // Replace magenta pixels with transparent
        for (int y = 0; y < pixmap.getHeight(); y++) {
            for (int x = 0; x < pixmap.getWidth(); x++) {
                int pixel = pixmap.getPixel(x, y);

                if (isMagenta(pixel)) {
                    pixmap.drawPixel(x, y, 0x00000000); // Fully transparent
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private boolean isMagenta(int pixel) {
        // Extract RGBA components directly from the integer
        // Format is RRGGBBAA
        int r = (pixel >>> 24) & 0xFF;
        int g = (pixel >>> 16) & 0xFF;
        int b = (pixel >>> 8) & 0xFF;

        // Allow tolerance for magenta color variations
        return r >= 110 && g <= 65 && b >= 110;
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

        // Update gun firing
        updateGunFiring(deltaTime);

        // If exploded, handle falling
        if (isExploded) {
            if (isFalling) {
                updateFalling(deltaTime);
            }
            return; // Skip normal movement
        }

        // Check for collision with gun
        if (checkGunCollision()) {
            triggerExplosion();
            return;
        }

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

    private boolean checkGunCollision() {
        // Simple bounding box collision detection
        float heliLeft = position.x;
        float heliRight = position.x + FRAME_WIDTH;
        float heliBottom = position.y;
        float heliTop = position.y + FRAME_HEIGHT;

        float gunLeft = gunPosition.x;
        float gunRight = gunPosition.x + GUN_WIDTH;
        float gunBottom = gunPosition.y;
        float gunTop = gunPosition.y + GUN_HEIGHT;

        return heliRight > gunLeft && heliLeft < gunRight &&
               heliTop > gunBottom && heliBottom < gunTop;
    }

    private void triggerExplosion() {
        isExploded = true;
        isFalling = true;

        // Select random explosion texture
        int randomIndex = (int) (Math.random() * explosionTextures.length);
        currentExplosionTexture = explosionTextures[randomIndex];

        // Stop all horizontal movement
        velocity.x = 0;
        velocity.y = 0;
    }

    private void updateFalling(float deltaTime) {
        position.y -= FALL_SPEED * deltaTime;

        // Stop falling when reaching bottom
        if (position.y <= 0) {
            position.y = 0;
            isFalling = false;
        }
    }

    private void updateGunFiring(float deltaTime) {
        gunFireTimer += deltaTime;

        if (isGunFiring) {
            // Currently showing fire sprite
            fireDisplayTimer += deltaTime;
            if (fireDisplayTimer >= FIRE_DISPLAY_DURATION) {
                isGunFiring = false;
                fireDisplayTimer = 0f;
            }
        } else {
            // Check if it's time to fire
            if (gunFireTimer >= nextFireTime) {
                isGunFiring = true;
                gunFireTimer = 0f;
                nextFireTime = getRandomFireInterval();

                // Spawn invisible bullet
                isBulletActive = true;
                bulletY = GUN_HEIGHT; // Start from top of gun
            }
        }

        // Update bullet position
        if (isBulletActive) {
            bulletY += BULLET_SPEED * deltaTime;

            // Check if bullet is off screen
            if (bulletY > Gdx.graphics.getHeight()) {
                isBulletActive = false;
            }

            // Check if bullet hits helicopter (only if not already exploded)
            if (!isExploded && checkBulletHitsHelicopter()) {
                triggerExplosion();
                isBulletActive = false;
            }
        }
    }

    private boolean checkBulletHitsHelicopter() {
        // Bullet is a vertical line at the center of the gun
        float bulletX = gunPosition.x + GUN_WIDTH / 2f;

        // Check if bullet X is within helicopter bounds
        float heliLeft = position.x;
        float heliRight = position.x + FRAME_WIDTH;

        // Check if bullet Y is within helicopter bounds
        float heliBottom = position.y;
        float heliTop = position.y + FRAME_HEIGHT;

        return bulletX >= heliLeft && bulletX <= heliRight &&
               bulletY >= heliBottom && bulletY <= heliTop;
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
        renderGun();
        renderHelicopter();
        renderUI();
    }

    private void clearScreen() {
        ScreenUtils.clear(BACKGROUND_COLOR);
    }

    private void renderGun() {
        Texture currentGunTexture = isGunFiring ? gunFiresTexture : gunReadyTexture;
        batch.begin();
        batch.draw(currentGunTexture, gunPosition.x, gunPosition.y, GUN_WIDTH, GUN_HEIGHT);
        batch.end();
    }

    private void renderHelicopter() {
        batch.begin();

        if (isExploded) {
            // Draw explosion texture
            batch.draw(currentExplosionTexture, position.x, position.y, FRAME_WIDTH, FRAME_HEIGHT);
        } else {
            // Get current animation frame
            TextureRegion currentFrame = helicopterAnimation.getKeyFrame(stateTime);

            // Flip the texture region if facing left, then draw normally
            boolean isFlippedX = currentFrame.isFlipX();
            if (facingLeft != isFlippedX) {
                currentFrame.flip(true, false);
            }

            batch.draw(currentFrame, position.x, position.y, FRAME_WIDTH, FRAME_HEIGHT);
        }

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
        if (explosionTextures != null) {
            for (Texture texture : explosionTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }
        if (gunReadyTexture != null) {
            gunReadyTexture.dispose();
        }
        if (gunFiresTexture != null) {
            gunFiresTexture.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
