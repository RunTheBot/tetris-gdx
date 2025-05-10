package me.runthebot.tetris;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import static me.runthebot.tetris.GameScreen.*;

// TODO: stuff
/** First screen of the application. Displayed after the application is created. */
public class MenuScreen implements Screen {
    Game game;
    OrthographicCamera camera;
    Vector3 touchPoint = new Vector3();


    public MenuScreen(Game game) {
        this.game = game;

        // set camera settings here
        camera = new OrthographicCamera();
//        camera.setToOrtho(false, GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE);

        camera.setToOrtho(false, GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE);
    }

    boolean touched(Rectangle r) {
        if (!Gdx.input.justTouched()) {
            return false;
        }

        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        return r.contains(touchPoint.x, touchPoint.y);
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        if (touched(new Rectangle(0, 0, 100, 100))) {
            game.setScreen(new GameScreen());
        }
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
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
    }
}
