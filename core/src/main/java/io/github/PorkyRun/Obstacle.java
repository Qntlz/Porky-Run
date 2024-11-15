package io.github.PorkyRun;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Obstacle {
    private final Texture hayBale;
    private float x;
    private float y;
    private final Rectangle obstacleHitbox;

    public Obstacle(float startX, float startY, Texture hailBaleTexture) {
        hayBale = hailBaleTexture;
        x = startX;
        y = startY;

        obstacleHitbox = new Rectangle(x,y, 70, 60);
    }

    // New method to set position for reusing obstacles from the pool
    public void setPosition(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        obstacleHitbox.setPosition(startX, startY);  // Update hitbox position as well
    }

    public void update(float delta) {
        int hitboxOffsetX = 30;
        int hitboxOffsetY = 30;
        float speed = 400;                                      // Speed at which the obstacle moves
        x -= speed * delta;                                     // Move obstacle to the left

        obstacleHitbox.setPosition(x + hitboxOffsetX,y + hitboxOffsetY);
    }

    public void render(SpriteBatch batch) {
        // Display Obstacle
        batch.draw(hayBale, x, y, 120, 120);
    }

    public void dispose() {
        hayBale.dispose();
    }

    public float getWidth() {
        return hayBale.getWidth() / 7f;
    }

    public Rectangle getHitbox() {
        return obstacleHitbox;
    }

    public boolean isOffScreen() {
        return x + getWidth() < 0; // Check if the obstacle has moved off-screen
    }
}
