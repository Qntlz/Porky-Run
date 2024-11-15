package io.github.PorkyRun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;


/**
 * First screen of the application. Displayed after the application is created.
 */
public class FirstScreen implements Screen {
    private final Main game;
    private SpriteBatch batch;
    private StretchViewport viewport;
    private BitmapFont titleFont, menuFont;
    private int selectedOption = 0;  // 0 = "New Game", 1 = "Exit"
    private float rockingTimer = 0f;
    private Music bgMusic;
    private FrameBuffer bgFrameBuffer;
    private TextureRegion bgFrameBufferTextureRegion;

    public FirstScreen(Main game){
        this.game = game;
    }

    @Override
    public void show() {
        // Prepare your screen here.
        batch = new SpriteBatch();
        viewport = new StretchViewport(800, 500);
        bgFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 800, 500, false);       // Initialize FrameBuffer for background optimization

        handleBg();
        handleFont();
        handleBgm();
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        draw(delta);
        logic();
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        viewport.update(width, height);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        batch.dispose();
        titleFont.dispose();
        menuFont.dispose();
        bgMusic.dispose();
        bgFrameBuffer.dispose();
    }

    private void draw(float delta) {
        Gdx.gl.glClearColor(1,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        batch.begin();

        // Draw background
        batch.draw(bgFrameBufferTextureRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        displayMenu(delta);

        batch.end();
    }

    private void logic() {
        // Handle input for menu navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % 2;              // Toggle between 0 and 1
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = (selectedOption + 1) % 2;              // Toggle between 0 and 1
        }

        // Menu Actions
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedOption == 0) {
                game.setScreen(new GameScreen());        // Start a new game
            } else if (selectedOption == 1) {
                Gdx.app.exit();                          // Closes the application
            }
        }
    }

    private void handleBg() {
        Texture bgTexture = new Texture("background.png");
        Sprite bgSprite = new Sprite(bgTexture);
        bgSprite.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());

        // Render the background to the FrameBuffer
        bgFrameBuffer.begin();
        Gdx.gl.glClearColor(1, 1, 1, 1);       // Clear the FrameBuffer, Adjust to desired color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        bgSprite.draw(batch);
        batch.end();

        bgFrameBuffer.end();
        bgFrameBufferTextureRegion = new TextureRegion(bgFrameBuffer.getColorBufferTexture());
        bgFrameBufferTextureRegion.flip(false, true);           // Flip vertically to correct inversion
    }

    private void handleBgm() {
        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("happy-farm.mp3"));
        bgMusic.setVolume(.5f);
        bgMusic.setLooping(true);
        bgMusic.play();
    }

    private void handleFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("LuckiestGuy-Regular.ttf"));
        FreeTypeFontParameter titleParameter = new FreeTypeFontParameter();
        FreeTypeFontParameter menuParameter = new FreeTypeFontParameter();

        titleParameter.size = 60;
        titleParameter.color = Color.PINK;
        titleFont = generator.generateFont(titleParameter);

        menuParameter.size = 40;
        menuParameter.color = Color.WHITE;
        menuFont = generator.generateFont(menuParameter);

        generator.dispose();
    }

    private void displayMenu(float delta){
        rockingTimer += delta * 5;
        float rockingOffset = (float) Math.sin(rockingTimer) * 8;          // Adjust Amplitude

        titleFont.draw(batch, "Porky Run", 350, 350);

        // Draw menu options with rocking effect on the selected option
        if (selectedOption == 0) {
            // "New Game" is selected - apply rocking effect
            menuFont.setColor(Color.YELLOW);
            menuFont.draw(batch, "New Game", 350, 170 + rockingOffset);
            menuFont.setColor(Color.WHITE); // Reset color for non-selected text
            menuFont.draw(batch, "Exit", 350, 115);
        } else {
            // "Exit" is selected - apply rocking effect
            menuFont.setColor(Color.WHITE);
            menuFont.draw(batch, "New Game", 350, 170);
            menuFont.setColor(Color.YELLOW);
            menuFont.draw(batch, "Exit", 350, 115 + rockingOffset);
        }
    }


}
