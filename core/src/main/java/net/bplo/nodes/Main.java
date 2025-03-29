package net.bplo.nodes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import net.bplo.nodes.assets.Assets;
import net.bplo.nodes.editor.Editor;
import net.bplo.nodes.editor.EditorUtil;
import net.bplo.nodes.imgui.ImGuiPlatform;

/** {@link ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    public static Main app;

    public final ImGuiPlatform imgui;

    public Assets assets;
    public Editor editor;
    public OrthographicCamera windowCamera;

    public Main(ImGuiPlatform imguiPlatform) {
        Main.app = this;
        this.imgui = imguiPlatform;
    }

    @Override
    public void create() {
        assets = new Assets();

        windowCamera = new OrthographicCamera();
        windowCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        windowCamera.update();

        imgui.init();

        EditorUtil.init(imgui, assets);

        editor = new Editor();
    }

    @Override
    public void render() {
        var shouldExit = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        if (shouldExit) {
            Gdx.app.exit();
        }

        var delta = Gdx.graphics.getDeltaTime();
        var batch = assets.batch;
        var image = assets.gdx;
        var imgWidth = image.getRegionWidth();
        var imgHeight = image.getRegionHeight();
        var winWidth = windowCamera.viewportWidth;
        var winHeight = windowCamera.viewportHeight;

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, (winWidth - imgWidth) / 2, (winHeight - imgHeight) / 2);
        batch.end();

        imgui.startFrame();
        editor.update(delta);
        editor.render();
        imgui.endFrame();
    }

    @Override
    public void resize(int width, int height) {
        windowCamera.setToOrtho(false, width, height);
        windowCamera.update();
    }

    @Override
    public void dispose() {
        assets.dispose();
        imgui.dispose();
    }
}
