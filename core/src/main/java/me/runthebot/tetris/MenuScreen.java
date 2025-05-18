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

        VisLabel menuLabel = new VisLabel("Tetris");
        menuLabel.setFontScale(3f);

        VisTextButton classicPlayButton = new VisTextButton("Play Classic");
        classicPlayButton.getLabel().setFontScale(2f); // additionally scale the button text
        classicPlayButton.addListener(event -> {
            if (classicPlayButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        VisTextButton sprintPlayButton = new VisTextButton("Play Sprint");
        sprintPlayButton.getLabel().setFontScale(2f); // additionally scale the button text
        sprintPlayButton.addListener(event -> {
            if (sprintPlayButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        VisTextButton arcadePlayButton = new VisTextButton("Play Arcade");
        arcadePlayButton.getLabel().setFontScale(2f); // additionally scale the button text
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
        table.add(menuLabel);
        table.add(classicPlayButton).pad(40).width(300).height(100);
        table.add(sprintPlayButton);
        table.add(arcadePlayButton);
        table.add(settingsButton);
        // TODO: make vertical
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
