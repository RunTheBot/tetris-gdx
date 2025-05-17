package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

public class MenuScreen implements Screen {
    private final Tetris game;
    private Stage stage;
    private VisWindow window;

    public MenuScreen(final Tetris game) {
        this.game = game;
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        VisWindow window = new VisWindow("Main Menu");
        window.setMovable(false);
        window.setResizable(false);
        window.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        window.setPosition(0, 0);

        VisTextButton playButton = new VisTextButton("Play");
        playButton.getLabel().setFontScale(2f); // additionally scale the button text
        playButton.addListener(event -> {
            if (playButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(playButton).pad(40).width(300).height(100);
        window.add(table).expand().fill();

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
        if (window != null) {
            window.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        }
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
        if (stage != null) stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
