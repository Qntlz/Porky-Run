package io.github.PorkyRun;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;

public class GameScreen implements Screen {

    private TextureRegion bgFrameBufferTextureRegion;
    private ShapeRenderer showPorkyHitbox;
    private Pool<Obstacle> obstaclePool;
    private GlyphLayout gameOverLayout;
    private Array<Obstacle> obstacles;
    private FrameBuffer bgFrameBuffer;
    private StretchViewport viewport;
    private BitmapFont gameOverFont;
    private Texture gameOverTexture;
    private Texture hayBaleTexture;
    private BitmapFont restartFont;
    private Rectangle porkyHitbox;
    private Texture porkyTexture;
    private SpriteBatch batch;

    private float obstacleSpawnTimer = 0;                       // Timer to track when to spawn new obstacles
    private boolean isGameOver = false;                         // Track game-over state
    private boolean isOnGround = true;                          // Track if Porky is on the ground
    private float velocity = 0;                                 // Vertical velocity
    private float porkyY = 0;                                   // Y-position for Porky
    float porkyX = 0;                                           // X-Position for Porky


    @Override
    public void show() {
        showPorkyHitbox = new ShapeRenderer();
        gameOverLayout = new GlyphLayout();
        gameOverFont = new BitmapFont();
        obstacles = new Array<>();
        batch = new SpriteBatch();

        gameOverTexture = new Texture("game-over.png");
        hayBaleTexture = new Texture("hay-bale.png");
        porkyTexture = new Texture("pigAsset.png");

        // Initialize FrameBuffer for background optimization
        bgFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 800, 500, false);
        viewport = new StretchViewport(800, 500);

        // Porky's Texture Dimensions
        float porkyHeight = 300;
        float porkyWidth = 300;

        // Initialize Porky's hitbox
        porkyHitbox = new Rectangle(porkyX, porkyY, porkyWidth, porkyHeight);

        // Initialize Obstacle Pool (To Prevent Garbage Collection)
        obstaclePool = new Pool<Obstacle>() {
            @Override
            protected Obstacle newObject() {
                return new Obstacle(800, 80, hayBaleTexture);      // X:800 Y:80 Obstacle spawn location
            }
        };

        handleFont();
        handleBg();
    }

    @Override
    public void render(float delta) {
        draw();
        if (!isGameOver) {          // Only update game if not over
            logic(delta);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();          // Restart if 'R' is pressed
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        porkyTexture.dispose();
        showPorkyHitbox.dispose();
        for (Obstacle obstacle : obstacles) {
            obstacle.dispose();
        }
        gameOverTexture.dispose();
        gameOverFont.dispose();
        hayBaleTexture.dispose();
        bgFrameBuffer.dispose();
    }

    private void handleFont(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LuckiestGuy-Regular.ttf"));
        FreeTypeFontParameter gameOverFontParam = new FreeTypeFontParameter();
        FreeTypeFontParameter restartFontParam = new FreeTypeFontParameter();

        // Game-Over Text Configuration
        gameOverFontParam.size = 80;
        gameOverFontParam.color = Color.RED;
        gameOverFont = generator.generateFont(gameOverFontParam);

        // Restart Text Configuration
        restartFontParam.size = 40;
        restartFontParam.color = Color.YELLOW;
        restartFont = generator.generateFont(restartFontParam);


        generator.dispose();
    }

    private void handleBg() {
        Texture bgTexture = new Texture("background.png");
        Sprite bgSprite = new Sprite(bgTexture);
        bgSprite.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());

        // Render the background to the FrameBuffer
        bgFrameBuffer.begin();
        Gdx.gl.glClearColor(1, 1, 1, 1);        // Clear the FrameBuffer, Adjust to desired color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        bgSprite.draw(batch);
        batch.end();

        bgFrameBuffer.end();
        bgFrameBufferTextureRegion = new TextureRegion(bgFrameBuffer.getColorBufferTexture());
        bgFrameBufferTextureRegion.flip(false, true);           // Flip vertically to correct inversion
    }

    private void displayGameOverMessage() {
        if (isGameOver) {
            batch.begin();
            batch.draw(gameOverTexture,360 ,25,300,300);        // Draw Lechon-Baboy Image

            String gameOverText = "GAME OVER!";
            String restartText = "Press R to Restart";
            gameOverLayout.setText(gameOverFont, gameOverText);
            gameOverLayout.setText(restartFont, restartText);
            gameOverFont.draw(batch, gameOverText, 320, 370);
            restartFont.draw(batch, restartText, 200, 70);
            batch.end();
        }
    }

    private void restartGame() {
        isGameOver = false;
        porkyX = 0;
        porkyY = 90;            // Reset Porky to the ground position
        velocity = 0;
        obstacles.clear();
    }

    private void draw() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        batch.begin();
        // Draw background
        batch.draw(bgFrameBufferTextureRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // X-position for Porky
        batch.draw(porkyTexture, porkyX, porkyY, porkyTexture.getWidth() / 3f, porkyTexture.getHeight() / 3f);

        // Draw each obstacle
        for (Obstacle obstacle : obstacles) {
            obstacle.render(batch);
        }
        batch.end();

        // Draw hitbox using ShapeRenderer
        showPorkyHitbox.begin(ShapeRenderer.ShapeType.Line);
        showPorkyHitbox.setColor(0, 1, 0, 1);       // Green color for the hitbox
        showPorkyHitbox.rect(porkyHitbox.x, porkyHitbox.y, porkyHitbox.width, porkyHitbox.height);

        // Draw hitbox for each obstacle
        showPorkyHitbox.setColor(1, 0, 0, 1);       // Red color for obstacle hitbox
        for (Obstacle obstacle : obstacles) {
            Rectangle obstacleHitbox = obstacle.getHitbox();
            showPorkyHitbox.rect(obstacleHitbox.x, obstacleHitbox.y, obstacleHitbox.width, obstacleHitbox.height);
        }

        showPorkyHitbox.end();
        displayGameOverMessage();
    }

    private void logic(float delta) {
        float gravity = -0.43f;
        int hitBoxOffsetX = 50;
        int hitBoxOffsetY = 40;


        // Handle jump input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            velocity = 10;                                        // Set an upward velocity for the jump
            isOnGround = false;                                   // Porky is no longer on the ground
        }

        // Apply gravity and update Porky's position
        velocity += gravity;                                      // Apply gravity to the velocity
        porkyY += velocity;                                       // Update Porky's Y position

        // Check if Porky has landed back on the ground
        if (porkyY <= 90) {                                       // Assuming 90 is the ground level
            porkyY = 90;
            isOnGround = true;                                    // Porky is back on the ground
            velocity = 0;                                         // Reset velocity
        }

        // Update Porky's hitbox position
        porkyHitbox.setPosition(porkyX + hitBoxOffsetX, porkyY + hitBoxOffsetY);
        porkyHitbox.setSize(60, 50);

        // Spawn obstacles at intervals
        obstacleSpawnTimer += delta;
        float obstacleSpawnInterval = 3f;                          // Time interval between obstacle spawns
        if (obstacleSpawnTimer >= obstacleSpawnInterval) {
            Obstacle obstacle = obstaclePool.obtain();
            obstacle.setPosition(800,80);             // Set the initial position for the obstacle
            obstacles.add(obstacle);
            obstacleSpawnTimer = 0;
        }

        // Update and remove obstacles
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update(delta);

            if (obstacle.isOffScreen()) {
                obstacles.removeIndex(i);
                obstaclePool.free(obstacle);                       // Return to pool
            }
        }
        // Check for collisions
        checkCollisions();
    }

    private void checkCollisions() {
        for (Obstacle obstacle : obstacles) {
            // Use the existing hitbox for collision detection
            if (porkyHitbox.overlaps(obstacle.getHitbox())) {
                isGameOver = true;                              // Set game over if there's a collision
                break;
            }
        }
    }
}
