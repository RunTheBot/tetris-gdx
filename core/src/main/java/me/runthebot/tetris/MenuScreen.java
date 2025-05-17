package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

public class MenuScreen implements Screen {
    private final Tetris game;
    private Stage stage;

    public MenuScreen(final Tetris game) {
        this.game = game;
    }

    @Override
    public void show() {
        Viewport viewport = game.viewport;
        stage = new Stage(viewport);

        Gdx.input.setInputProcessor(stage);

        VisTextButton playButton = new VisTextButton("Play");
        playButton.addListener(event -> {
            if (playButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        VisWindow window = new VisWindow("Main Menu");
        window.add(playButton).pad(10);
        window.pack();
        window.centerWindow();
        window.setMovable(true);

        stage.addActor(window);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }



    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
