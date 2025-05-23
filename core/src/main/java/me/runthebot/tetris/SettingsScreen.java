package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.*;

public class SettingsScreen implements Screen {
    private final Tetris game;
    private final ConfigManager configManager;
    private final GameConfig config;
    private Stage stage;
    private VisSlider dasSlider;
    private VisLabel dasValueLabel;
    private VisSlider arrSlider;
    private VisLabel arrValueLabel;

    public SettingsScreen(final Tetris game) {
        this.game = game;
        this.configManager = ConfigManager.getInstance();
        this.config = configManager.getConfig();
    }

    @Override
    public void show() {
        // create the main viewport
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        // take user input
        Gdx.input.setInputProcessor(stage);

        // settings menu title
        VisLabel titleLabel = new VisLabel("Settings");
        titleLabel.setFontScale(2.2f);

        dasSlider = new VisSlider(0, 500, 1, false);
        dasSlider.setValue(config.DAS_DELAY);
        dasValueLabel = new VisLabel("DAS: " + dasSlider.getValue());
        dasSlider.addListener(event -> {
            config.DAS_DELAY = (int) dasSlider.getValue();
            dasValueLabel.setText("DAS: " + config.DAS_DELAY);
            return false;
        });


        // ARR setting slider
        arrSlider = new VisSlider(0, 500, 1, false);
        arrSlider.setValue(config.ARR_DELAY);
        // TODO: add tooltips?
        arrValueLabel = new VisLabel("ARR: " + arrSlider.getValue());
        arrSlider.addListener(event -> {
            config.ARR_DELAY = (int) arrSlider.getValue();
            arrValueLabel.setText("ARR: " + config.ARR_DELAY);
            return false;
        });

        Table dasTable = new Table();
        dasTable.add(new VisLabel("Delayed Auto Shift (DAS)")).padRight(18f);
        dasTable.add(dasSlider).width(240);
        dasTable.add(dasValueLabel).width(70).padLeft(8f);


        // main table
        Table arrTable = new Table();
        arrTable.add(new VisLabel("Auto Repeat Rate (ARR)")).padRight(18f);
        arrTable.add(arrSlider).width(240);
        arrTable.add(arrValueLabel).width(70).padLeft(8f);

        // animation
        VisCheckBox animationBox = new VisCheckBox("Display Animations");
        animationBox.setChecked(true);

        VisCheckBox showGhostPiece = new VisCheckBox("Show Ghost Pieces");
        showGhostPiece.setChecked(config.showGhostPiece);
        showGhostPiece.addListener(event -> {
            config.showGhostPiece = showGhostPiece.isChecked();
            return false;
        });

        // button to go back to the main screen
        VisTextButton backButton = new VisTextButton("Back");
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
        table.center();

        // add each element to table and pad each
        table.add(titleLabel).padBottom(48f).row();
        table.add(dasTable).padBottom(32f).row();
        table.add(arrTable).padBottom(32f).row();
        table.add(animationBox).padBottom(32f).row();
        table.add(showGhostPiece).padBottom(32f).row();
        table.add(backButton).width(180).height(60);

        stage.addActor(table);
    }


    @Override
    public void render(float delta) {
        // clear screen and add stage
        ScreenUtils.clear(Color.BLACK);
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
        configManager.saveConfig();
        if (stage != null) stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
