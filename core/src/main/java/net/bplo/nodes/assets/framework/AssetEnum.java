package net.bplo.nodes.assets.framework;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.Serial;

public interface AssetEnum<ResourceType> {

    default ResourceType get() {
        throw new MethodImplementationMissingException("get");
    }

    class MethodImplementationMissingException extends GdxRuntimeException {

        @Serial
        private static final long serialVersionUID = -7295483983524218790L;

        public MethodImplementationMissingException(String methodName) {
            super("override %s to use param in concrete AssetType enum".formatted(methodName));
        }
    }
}
