package io.github.helicopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class HelicopterScreen implements GameScreen {
    private final Main game;
    private static final String[] HELICOPTER_FRAME_PATHS = {"heli1.png", "heli2.png", "heli3.png", "heli4.png"};
    private static final String[] EXPLOSION_FRAME_PATHS = {"attackhelicopter-exploding1.png", "attackhelicopter-exploding2.png"};
    private static final String GUN_READY_TEXTURE_PATH = "1942gun-ready.png";
    private static final String GUN_FIRES_TEXTURE_PATH = "1942gun-fires.png";
    private static final float FRAME_DURATION = 0.1f;
    private static final float INITIAL_VELOCITY_X = 200f, INITIAL_VELOCITY_Y = 150f;
    private static final float MOVEMENT_SPEED = 300f, ARRIVAL_THRESHOLD = 5f, TEXT_PADDING = 10f;
    private static final Color BACKGROUND_COLOR = new Color(0.15f, 0.15f, 0.2f, 1f);
    private static final int FRAME_WIDTH = 130, FRAME_HEIGHT = 52, GUN_WIDTH = 50, GUN_HEIGHT = 120;
    private static final float MIN_FIRE_INTERVAL = 1.0f, MAX_FIRE_INTERVAL = 3.0f, FIRE_DISPLAY_DURATION = 0.15f;
    private static final float FALL_SPEED = 400f, BULLET_SPEED = 800f;

    private SpriteBatch batch;
    private Texture[] helicopterTextures, explosionTextures;
    private Texture gunReadyTexture, gunFiresTexture, currentExplosionTexture;
    private Animation<TextureRegion> helicopterAnimation;
    private BitmapFont font;
    private float stateTime = 0f;
    private boolean isExploded = false, isFalling = false, isGunFiring = false, isBulletActive = false;
    private float gunFireTimer = 0f, nextFireTime = 0f, fireDisplayTimer = 0f, bulletY = 0f;
    private final Vector2 gunPosition = new Vector2(), position = new Vector2();
    private final Vector2 velocity = new Vector2(), targetPosition = new Vector2();
    private boolean isUserControlling = false, facingLeft = false;

    public HelicopterScreen(Main game) { this.game = game; }

    @Override
    public void show() {
        batch = new SpriteBatch();
        batch.enableBlending();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        helicopterTextures = new Texture[HELICOPTER_FRAME_PATHS.length];
        TextureRegion[] frames = new TextureRegion[HELICOPTER_FRAME_PATHS.length];
        for (int i = 0; i < HELICOPTER_FRAME_PATHS.length; i++) {
            helicopterTextures[i] = loadTextureWithTransparency(HELICOPTER_FRAME_PATHS[i]);
            frames[i] = new TextureRegion(helicopterTextures[i]);
        }
        helicopterAnimation = new Animation<>(FRAME_DURATION, frames);
        helicopterAnimation.setPlayMode(Animation.PlayMode.LOOP);
        explosionTextures = new Texture[EXPLOSION_FRAME_PATHS.length];
        for (int i = 0; i < EXPLOSION_FRAME_PATHS.length; i++)
            explosionTextures[i] = loadTextureWithTransparency(EXPLOSION_FRAME_PATHS[i]);
        gunReadyTexture = loadTextureWithTransparency(GUN_READY_TEXTURE_PATH);
        gunFiresTexture = loadTextureWithTransparency(GUN_FIRES_TEXTURE_PATH);
        resetGame();
    }

    private void resetGame() {
        float sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        position.set((sw - FRAME_WIDTH) / 2f, (sh - FRAME_HEIGHT) / 2f);
        targetPosition.set(position);
        velocity.set(INITIAL_VELOCITY_X, INITIAL_VELOCITY_Y);
        gunPosition.set((sw - GUN_WIDTH) / 2f, 0);
        isExploded = isFalling = isUserControlling = isGunFiring = isBulletActive = facingLeft = false;
        currentExplosionTexture = null;
        gunFireTimer = fireDisplayTimer = bulletY = stateTime = 0f;
        nextFireTime = MIN_FIRE_INTERVAL + (float) Math.random() * (MAX_FIRE_INTERVAL - MIN_FIRE_INTERVAL);
    }

    private Texture loadTextureWithTransparency(String path) {
        Pixmap orig = new Pixmap(Gdx.files.internal(path));
        Pixmap pm = new Pixmap(orig.getWidth(), orig.getHeight(), Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.drawPixmap(orig, 0, 0);
        orig.dispose();
        for (int y = 0; y < pm.getHeight(); y++)
            for (int x = 0; x < pm.getWidth(); x++) {
                int p = pm.getPixel(x, y);
                int r = (p >>> 24) & 0xFF, g = (p >>> 16) & 0xFF, b = (p >>> 8) & 0xFF;
                if (r >= 110 && g <= 65 && b >= 110) pm.drawPixel(x, y, 0);
            }
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.setScreen(Main.SCREEN_MENU); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { resetGame(); return; }
        if (Gdx.input.isTouched()) {
            targetPosition.set(Gdx.input.getX() - FRAME_WIDTH / 2f, Gdx.graphics.getHeight() - Gdx.input.getY() - FRAME_HEIGHT / 2f);
            isUserControlling = true;
        }
        stateTime += delta;
        updateGunFiring(delta);
        if (isExploded) {
            if (isFalling) { position.y -= FALL_SPEED * delta; if (position.y <= 0) { position.y = 0; isFalling = false; } }
        } else {
            if (checkGunCollision()) { triggerExplosion(); }
            else if (isUserControlling) moveTowardsTarget(delta);
            else { position.x += velocity.x * delta; position.y += velocity.y * delta; handleScreenBounce(); }
            clampToScreen();
            facingLeft = velocity.x > 0;
        }
        ScreenUtils.clear(BACKGROUND_COLOR);
        batch.begin();
        batch.draw(isGunFiring ? gunFiresTexture : gunReadyTexture, gunPosition.x, gunPosition.y, GUN_WIDTH, GUN_HEIGHT);
        if (isExploded) batch.draw(currentExplosionTexture, position.x, position.y, FRAME_WIDTH, FRAME_HEIGHT);
        else {
            TextureRegion f = helicopterAnimation.getKeyFrame(stateTime);
            if (facingLeft != f.isFlipX()) f.flip(true, false);
            batch.draw(f, position.x, position.y, FRAME_WIDTH, FRAME_HEIGHT);
        }
        font.draw(batch, String.format(Locale.US, "Position: (%.0f, %.0f)", position.x, position.y), TEXT_PADDING, Gdx.graphics.getHeight() - TEXT_PADDING);
        font.draw(batch, "ESC: Menu | R: Restart | Click: Move", TEXT_PADDING, 25);
        batch.end();
    }

    private void updateGunFiring(float delta) {
        gunFireTimer += delta;
        if (isGunFiring) { fireDisplayTimer += delta; if (fireDisplayTimer >= FIRE_DISPLAY_DURATION) { isGunFiring = false; fireDisplayTimer = 0f; } }
        else if (gunFireTimer >= nextFireTime) { isGunFiring = true; gunFireTimer = 0f; nextFireTime = MIN_FIRE_INTERVAL + (float) Math.random() * (MAX_FIRE_INTERVAL - MIN_FIRE_INTERVAL); isBulletActive = true; bulletY = GUN_HEIGHT; }
        if (isBulletActive) {
            bulletY += BULLET_SPEED * delta;
            if (bulletY > Gdx.graphics.getHeight()) isBulletActive = false;
            float bx = gunPosition.x + GUN_WIDTH / 2f;
            if (!isExploded && bx >= position.x && bx <= position.x + FRAME_WIDTH && bulletY >= position.y && bulletY <= position.y + FRAME_HEIGHT) { triggerExplosion(); isBulletActive = false; }
        }
    }

    private boolean checkGunCollision() {
        float ox = Math.max(0, Math.min(position.x + FRAME_WIDTH, gunPosition.x + GUN_WIDTH) - Math.max(position.x, gunPosition.x));
        float oy = Math.max(0, Math.min(position.y + FRAME_HEIGHT, gunPosition.y + GUN_HEIGHT) - Math.max(position.y, gunPosition.y));
        return ox * oy >= FRAME_WIDTH * FRAME_HEIGHT * 0.5f;
    }

    private void triggerExplosion() {
        isExploded = isFalling = true;
        currentExplosionTexture = explosionTextures[(int) (Math.random() * explosionTextures.length)];
        velocity.set(0, 0);
    }

    private void moveTowardsTarget(float delta) {
        float dx = targetPosition.x - position.x, dy = targetPosition.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < ARRIVAL_THRESHOLD) { position.set(targetPosition); isUserControlling = false; return; }
        float md = Math.min(MOVEMENT_SPEED * delta, dist);
        position.x += (dx / dist) * md; position.y += (dy / dist) * md;
        if (Math.abs(dx) > ARRIVAL_THRESHOLD) velocity.x = dx > 0 ? Math.abs(INITIAL_VELOCITY_X) : -Math.abs(INITIAL_VELOCITY_X);
        if (Math.abs(dy) > ARRIVAL_THRESHOLD) velocity.y = dy > 0 ? Math.abs(INITIAL_VELOCITY_Y) : -Math.abs(INITIAL_VELOCITY_Y);
    }

    private void handleScreenBounce() {
        float sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        if (position.x < 0) { position.x = 0; velocity.x = Math.abs(velocity.x); }
        else if (position.x + FRAME_WIDTH > sw) { position.x = sw - FRAME_WIDTH; velocity.x = -Math.abs(velocity.x); }
        if (position.y < 0) { position.y = 0; velocity.y = Math.abs(velocity.y); }
        else if (position.y + FRAME_HEIGHT > sh) { position.y = sh - FRAME_HEIGHT; velocity.y = -Math.abs(velocity.y); }
    }

    private void clampToScreen() {
        float sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        position.x = Math.max(0, Math.min(position.x, sw - FRAME_WIDTH));
        position.y = Math.max(0, Math.min(position.y, sh - FRAME_HEIGHT));
    }

    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (helicopterTextures != null) for (Texture t : helicopterTextures) if (t != null) t.dispose();
        if (explosionTextures != null) for (Texture t : explosionTextures) if (t != null) t.dispose();
        if (gunReadyTexture != null) gunReadyTexture.dispose();
        if (gunFiresTexture != null) gunFiresTexture.dispose();
    }
}
