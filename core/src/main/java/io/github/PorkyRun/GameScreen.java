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
    private Animation<TextureRegion> porkyAnimation;
    private ShapeRenderer showHitbox;
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
    private SpriteBatch batch;

    private static final float FIXED_TIMESTEP = 1 / 60f;        // Set to 60 updates per second
    private boolean restartBuffered = false;
    private final float porkyHeight = 170;
    private boolean jumpBuffered = false;
    private final float porkyWidth = 170;
    private float obstacleSpawnTimer = 0;
    private boolean isGameOver = false;
    private boolean isOnGround = true;
    private float animationTime = 0;
    private float accumulator = 0f;
    private float velocity = 0;
    private float porkyY = 0;                                   // Y-position for Porky
    TextureAtlas atlas;
    float porkyX = 0;                                           // X-Position for Porky



    @Override
    public void show() {
        showHitbox = new ShapeRenderer();
        gameOverLayout = new GlyphLayout();
        gameOverFont = new BitmapFont();
        batch = new SpriteBatch();
        obstacles = new Array<>();

        gameOverTexture = new Texture("game-over.png");
        hayBaleTexture = new Texture("hay-bale.png");
        atlas = new TextureAtlas(Gdx.files.internal("porky-atlas.atlas"));

        Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions("run");
        porkyAnimation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);

        // Initialize FrameBuffer for background optimization
        bgFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 800, 500, false);
        viewport = new StretchViewport(800, 500);

        // Initialize Porky's hitbox
        porkyHitbox = new Rectangle(porkyX, porkyY, porkyWidth, porkyHeight);

        // Initialize Obstacle Pool (To Prevent Garbage Collection)
        obstaclePool = new Pool<>() {
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
        animationTime += delta;                                 // Animation time for Porky

        // Handle input every frame
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jumpBuffered = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartBuffered = true;
        }

        accumulator += delta;                                   // Accumulate delta time
        while (accumulator >= FIXED_TIMESTEP) {
            if (!isGameOver) {                                  // Only update game if not over
                logic();
            } else if (restartBuffered) {
                restartGame();                                  // Restart if 'R' is pressed
            }
            accumulator -= FIXED_TIMESTEP;
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
        showHitbox.dispose();
        atlas.dispose();
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

        // Draw Porky Running Animation
        TextureRegion currentFrame = porkyAnimation.getKeyFrame(animationTime);
        batch.draw(currentFrame, porkyX, porkyY, porkyWidth,porkyHeight);

        // Draw each obstacle
        for (Obstacle obstacle : obstacles) {
            obstacle.render(batch);
        }
        batch.end();

        // Draw hitbox using ShapeRenderer
        showHitbox.begin(ShapeRenderer.ShapeType.Line);
        showHitbox.setColor(0, 1, 0, 1);       // Green color for the hitbox
        showHitbox.rect(porkyHitbox.x, porkyHitbox.y, porkyHitbox.width, porkyHitbox.height);

        // Draw hitbox for each obstacle
        showHitbox.setColor(1, 0, 0, 1);       // Red color for obstacle hitbox
        for (Obstacle obstacle : obstacles) {
            Rectangle obstacleHitbox = obstacle.getHitbox();
            showHitbox.rect(obstacleHitbox.x, obstacleHitbox.y, obstacleHitbox.width, obstacleHitbox.height);
        }

        showHitbox.end();
        displayGameOverMessage();
    }

    private void  logic() {
        float gravity = -1500f;
        int hitBoxOffsetX = 50;
        int hitBoxOffsetY = 60;
        int obstacleStartPosX = 800;
        int obstacleStartPosY = 80;
        int ground = 63;


        // Handle jump input
        if (jumpBuffered && isOnGround) {
            velocity = 600;                                       // Set an upward velocity for the jump
            isOnGround = false;                                   // Porky is no longer on the ground
            jumpBuffered = false;                                 // Consume the jump input
        }

        // Apply gravity and update Porky's position
        velocity += gravity * FIXED_TIMESTEP;                     // Apply gravity to the velocity
        porkyY += velocity * FIXED_TIMESTEP;                      // Update Porky's Y position

        // Check if Porky has landed back on the ground
        if (porkyY <= ground) {
            porkyY = ground;
            isOnGround = true;                                    // Porky is back on the ground
            velocity = 0;                                         // Reset velocity
        }

        // Update Porky's hitbox position
        porkyHitbox.setPosition(porkyX + hitBoxOffsetX, porkyY + hitBoxOffsetY);
        porkyHitbox.setSize(60, 50);

        // Spawn obstacles at intervals
        obstacleSpawnTimer += FIXED_TIMESTEP;
        float obstacleSpawnInterval = 3f;                                   // Time interval between obstacle spawns
        if (obstacleSpawnTimer >= obstacleSpawnInterval) {
            Obstacle obstacle = obstaclePool.obtain();
            obstacle.setPosition(obstacleStartPosX,obstacleStartPosY);      // Set initial obstacle position
            obstacles.add(obstacle);
            obstacleSpawnTimer = 0;
        }

        // Update and remove obstacles
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update(FIXED_TIMESTEP);

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
                isGameOver = true;
                restartBuffered = false;
                break;
            }
        }
    }
}
