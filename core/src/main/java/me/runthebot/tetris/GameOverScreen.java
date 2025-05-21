package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

public class GameOverScreen implements Screen {
    private final Tetris game;
    private Stage stage;

    public GameOverScreen(final Tetris game) {
        this.game = game;
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        // take user input
        Gdx.input.setInputProcessor(stage);

        // game over title
        // TODO: copied from menu screen, refactor to game over
        VisLabel menuLabel = new VisLabel("Game Over");
        menuLabel.setFontScale(3f);

        VisTextButton playAgainButton = new VisTextButton("Play Again");
        playAgainButton.addListener(event -> {
            if (playAgainButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });


        VisTextButton backButton = new VisTextButton("Back to Menu");
        backButton.addListener(event -> {
            if (backButton.isPressed()) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });

        // create main table
        Table table = new Table();
        table.setFillParent(true);
        table.add(menuLabel).padBottom(48f).row();
        table.add(playAgainButton).width(180).height(60).padBottom(24f).row();
        table.add(backButton).width(180).height(60);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.13f, 0.22f, 1);
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
        if (stage != null) stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
