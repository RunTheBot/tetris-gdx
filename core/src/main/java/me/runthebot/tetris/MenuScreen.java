package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisLabel;
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
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        VisLabel menuLabel = new VisLabel("Tetris");
        menuLabel.setFontScale(3f);

        VisTextButton classicPlayButton = new VisTextButton("Play Classic");
        classicPlayButton.getLabel().setFontScale(2f);
        classicPlayButton.addListener(event -> {
            if (classicPlayButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        VisTextButton sprintPlayButton = new VisTextButton("Play Sprint");
        sprintPlayButton.getLabel().setFontScale(2f);
        sprintPlayButton.addListener(event -> {
            if (sprintPlayButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        VisTextButton arcadePlayButton = new VisTextButton("Play Arcade");
        arcadePlayButton.getLabel().setFontScale(2f);
        arcadePlayButton.addListener(event -> {
            if (arcadePlayButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        VisTextButton settingsButton = new VisTextButton("Settings");
        settingsButton.getLabel().setFontScale(1f);
        settingsButton.addListener(event -> {
            if (settingsButton.isPressed()) {
                game.setScreen(new SettingsScreen(game));
                return true;
            }
            return false;
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(menuLabel).padBottom(80f).row();
        table.add(classicPlayButton).width(360).height(90).padBottom(30f).row();
        table.add(sprintPlayButton).width(360).height(90).padBottom(30f).row();
        table.add(arcadePlayButton).width(360).height(90).padBottom(60f).row();
        table.add(settingsButton).width(200).height(60).padBottom(10f).row();

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
