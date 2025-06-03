package me.runthebot.tetris;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class BackButton {
    private Vector2 position;

    public BackButton(Vector2 position) {
        this.position = position;
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(Color.GRAY);
        renderer.rect(position.x, position.y, 100, 50);

        // create a back arrow icon
        renderer.setColor(Color.WHITE);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.triangle(position.x + 20, position.y + 25,
                          position.x + 80, position.y + 10,
                          position.x + 80, position.y + 40);
        renderer.end();
    }
}
