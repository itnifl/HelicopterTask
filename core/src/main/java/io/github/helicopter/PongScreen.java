package io.github.helicopter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class PongScreen implements GameScreen {
    private final Main game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private static final float PADDLE_WIDTH = 15f, PADDLE_HEIGHT = 80f, PADDLE_SPEED = 400f;
    private static final float BALL_SIZE = 15f, INITIAL_BALL_SPEED = 300f, BALL_SPEED_INCREMENT = 20f;
    private static final int WINNING_SCORE = 21;
    private static final float PADDLE_MARGIN = 30f;

    private float leftPaddleY, rightPaddleY, ballX, ballY, ballVelX, ballVelY, currentBallSpeed;
    private float screenWidth, screenHeight;
    private int leftScore = 0, rightScore = 0;
    private boolean gameOver = false, singlePlayer = true;
    private String winner = "";

    public PongScreen(Main game) { this.game = game; }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        resetGame();
    }

    private void resetGame() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        leftPaddleY = rightPaddleY = screenHeight / 2f - PADDLE_HEIGHT / 2f;
        leftScore = rightScore = 0;
        gameOver = false;
        winner = "";
        currentBallSpeed = INITIAL_BALL_SPEED;
        resetBall();
    }

    private void resetBall() {
        ballX = screenWidth / 2f - BALL_SIZE / 2f;
        ballY = screenHeight / 2f - BALL_SIZE / 2f;
        float angle = (float) (Math.random() * Math.PI / 2 - Math.PI / 4);
        int dir = Math.random() > 0.5 ? 1 : -1;
        ballVelX = dir * currentBallSpeed * (float) Math.cos(angle);
        ballVelY = currentBallSpeed * (float) Math.sin(angle);
    }

    @Override
    public void render(float delta) {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.setScreen(Main.SCREEN_MENU); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { resetGame(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) singlePlayer = !singlePlayer;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) leftPaddleY += PADDLE_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) leftPaddleY -= PADDLE_SPEED * delta;
        if (!singlePlayer) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) rightPaddleY += PADDLE_SPEED * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) rightPaddleY -= PADDLE_SPEED * delta;
        }
        leftPaddleY = Math.max(0, Math.min(leftPaddleY, screenHeight - PADDLE_HEIGHT));
        rightPaddleY = Math.max(0, Math.min(rightPaddleY, screenHeight - PADDLE_HEIGHT));

        if (!gameOver) {
            if (singlePlayer) {
                float pc = rightPaddleY + PADDLE_HEIGHT / 2f, bc = ballY + BALL_SIZE / 2f;
                float aiSpeed = PADDLE_SPEED * 0.7f;
                if (bc > pc + 10) rightPaddleY += aiSpeed * delta;
                else if (bc < pc - 10) rightPaddleY -= aiSpeed * delta;
                rightPaddleY = Math.max(0, Math.min(rightPaddleY, screenHeight - PADDLE_HEIGHT));
            }
            ballX += ballVelX * delta;
            ballY += ballVelY * delta;
            if (ballY <= 0) { ballY = 0; ballVelY = Math.abs(ballVelY); }
            else if (ballY + BALL_SIZE >= screenHeight) { ballY = screenHeight - BALL_SIZE; ballVelY = -Math.abs(ballVelY); }

            float lpx = PADDLE_MARGIN, rpx = screenWidth - PADDLE_MARGIN - PADDLE_WIDTH;
            if (ballX <= lpx + PADDLE_WIDTH && ballX + BALL_SIZE >= lpx && ballY + BALL_SIZE >= leftPaddleY && ballY <= leftPaddleY + PADDLE_HEIGHT) {
                ballX = lpx + PADDLE_WIDTH; ballVelX = Math.abs(ballVelX); increaseBallSpeed(); adjustBallAngle(leftPaddleY);
            }
            if (ballX + BALL_SIZE >= rpx && ballX <= rpx + PADDLE_WIDTH && ballY + BALL_SIZE >= rightPaddleY && ballY <= rightPaddleY + PADDLE_HEIGHT) {
                ballX = rpx - BALL_SIZE; ballVelX = -Math.abs(ballVelX); increaseBallSpeed(); adjustBallAngle(rightPaddleY);
            }
            if (ballX + BALL_SIZE < 0) { rightScore++; checkWinner(); if (!gameOver) resetBall(); }
            else if (ballX > screenWidth) { leftScore++; checkWinner(); if (!gameOver) resetBall(); }
        }

        ScreenUtils.clear(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < screenHeight; i += 30) shapeRenderer.rect(screenWidth / 2f - 2, i, 4, 15);
        shapeRenderer.rect(PADDLE_MARGIN, leftPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        shapeRenderer.rect(screenWidth - PADDLE_MARGIN - PADDLE_WIDTH, rightPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        shapeRenderer.rect(ballX, ballY, BALL_SIZE, BALL_SIZE);
        shapeRenderer.end();

        batch.begin();
        font.draw(batch, String.valueOf(leftScore), screenWidth / 4f, screenHeight - 30);
        font.draw(batch, String.valueOf(rightScore), 3 * screenWidth / 4f, screenHeight - 30);
        font.getData().setScale(1f);
        font.draw(batch, singlePlayer ? "Single Player (T)" : "Multiplayer (T)", 10, 30);
        font.draw(batch, "W/S: Left | UP/DOWN: Right | R: Restart | ESC: Menu", 10, 55);
        font.getData().setScale(2f);
        if (gameOver) {
            font.getData().setScale(3f);
            font.setColor(Color.YELLOW);
            font.draw(batch, winner, screenWidth / 2f - 150, screenHeight / 2f);
            font.getData().setScale(1.5f);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press R to restart", screenWidth / 2f - 100, screenHeight / 2f - 50);
            font.getData().setScale(2f);
        }
        batch.end();
    }

    private void increaseBallSpeed() {
        currentBallSpeed += BALL_SPEED_INCREMENT;
        float speed = (float) Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
        float ratio = currentBallSpeed / speed;
        ballVelX *= ratio; ballVelY *= ratio;
    }

    private void adjustBallAngle(float paddleY) {
        float hit = (ballY + BALL_SIZE / 2f - paddleY) / PADDLE_HEIGHT;
        float angle = (hit - 0.5f) * (float) Math.PI / 3;
        float dir = ballVelX > 0 ? 1 : -1;
        ballVelX = dir * currentBallSpeed * (float) Math.cos(angle);
        ballVelY = currentBallSpeed * (float) Math.sin(angle);
    }

    private void checkWinner() {
        if (leftScore >= WINNING_SCORE) { gameOver = true; winner = singlePlayer ? "YOU WIN!" : "LEFT WINS!"; }
        else if (rightScore >= WINNING_SCORE) { gameOver = true; winner = singlePlayer ? "AI WINS!" : "RIGHT WINS!"; }
    }

    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}
