package net.bplo.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class Assets implements Disposable {

    public SpriteBatch batch;
    public Texture image;

    public Assets() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
