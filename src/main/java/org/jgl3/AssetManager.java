package org.jgl3;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public final class AssetManager extends ResourceManager {

    private final Hashtable<String, Object> assets = new Hashtable<>();
    private final Hashtable<String, AssetLoader> assetLoaders = new Hashtable<>();
    private final Hashtable<Integer, Vector<String>> assetTypes = new Hashtable<>();

    public AssetManager() {
        registerAssetLoader(".png", 0, new Texture.TextureLoader());
        registerAssetLoader(".jpg", 0, new Texture.TextureLoader());
        registerAssetLoader(".wav", 0, new Sound.SoundLoader());
    }
    
    public void registerAssetLoader(String extension, int type, AssetLoader assetLoader) {
        assetLoaders.put(extension, assetLoader);
        if(!assetTypes.containsKey(type)) {
            assetTypes.put(type, new Vector<>());
        }
        assetTypes.get(type).add(extension);
    }

    public Vector<String> getExtensionsForType(int type) {
        return new Vector<>(assetTypes.get(type));
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T load(File file) throws Exception {
        String key = file.getPath();

        if(!assets.containsKey(key)) {
            Log.put(1, "Loading assets - '" + key + "' ...");

            assets.put(key, assetLoaders.get(IO.extension(file)).load(file, this));
        }
        return (T)assets.get(key);
    }

    public void unLoad(File file) throws Exception {
        String key = file.getPath();

        if(assets.containsKey(key)) {
            Object asset = assets.get(key);

            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
            assets.remove(key);
        }
    }

    @Override
    public void clear() throws Exception {
        Enumeration<String> keys = assets.keys();

        while(keys.hasMoreElements()) {
            Object asset = assets.get(keys.nextElement());

            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
        }
        assets.clear();
        
        super.clear();
    }
}
