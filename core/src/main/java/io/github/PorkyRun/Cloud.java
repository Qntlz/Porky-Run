package io.github.PorkyRun;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Cloud {
    private Texture texture;
    private float x, y, speed;

    public Cloud() {
        this.texture = null;  // Initially no texture
        this.x = 0;
        this.y = 0;
        this.speed = 0;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void update(float delta) {
        x -= speed * delta;  // Move cloud to the left
    }

    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y);  // Draw the cloud
        }
    }

    public boolean isOffScreen() {
        return x + texture.getWidth() < 0;  // Cloud is off-screen if it goes past the left edge
    }

}
