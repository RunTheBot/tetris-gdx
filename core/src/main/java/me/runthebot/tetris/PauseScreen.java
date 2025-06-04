package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class PauseScreen implements Screen {
    private final Tetris game;
    private final Screen previousScreen;
    private  final Stage stage;

    public PauseScreen(Tetris game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        // load visui?

        stage = new Stage(new ScreenViewport(), new SpriteBatch());
        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        VisLabel titleLabel = new VisLabel("PAUSED");
        titleLabel.setFontScale(2.5f);

        VisTextButton resumeButton = new VisTextButton("Resume");
        VisTextButton quitButton = new VisTextButton("Quit to menu");
        VisTextButton exitButton = new VisTextButton("Exit game");

        resumeButton.addListener(event -> {
            if (resumeButton.isPressed()) {
                game.setScreen(previousScreen);
                return true;
            }
            return false;
        });

        quitButton.addListener(event -> {
            if (quitButton.isPressed()) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });

        exitButton.addListener(event -> {
            if (exitButton.isPressed()) {
                Gdx.app.exit();
                return true;
            }
            return false;
        });

        table.add(titleLabel).padBottom(30).row();
        table.add(resumeButton).pad(10).row();
        table.add(quitButton).pad(10).row();
        table.add(exitButton).pad(10);
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0,0.8f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (VisUI.isLoaded()) {
            VisUI.dispose();
        }
    }
}
