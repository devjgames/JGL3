package org.jgl3;

import java.io.File;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public final class Sound extends Resource {

    public final static class SoundLoader implements AssetLoader {

        @Override
        public Object load(File file) throws Exception {
            BinReader reader = new BinReader(IO.readAllBytes(file));
            int b1 = reader.readByte();
            int b2 = reader.readByte();
            int b3 = reader.readByte();
            int b4 = reader.readByte();
            String header = new String(new byte[]{(byte) b1, (byte) b2, (byte) b3, (byte) b4});
            if (!header.equals("RIFF")) {
                throw new Exception("Invalid WAV file format!");
            }
            reader.setPosition(reader.getPosition() + 18);
    
            int chan = reader.readShort();
            int sampleRate = reader.readInt();
    
            reader.setPosition(reader.getPosition() + 6);
    
            int bps = reader.readShort();
    
            reader.setPosition(reader.getPosition() + 4);
    
            int size = reader.readInt();
            byte[] data = new byte[size];
    
            reader.readBytes(data);
    
            int format;
    
            if (chan == 1) {
                if (bps == 8) {
                    format = AL10.AL_FORMAT_MONO8;
                } else {
                    format = AL10.AL_FORMAT_MONO16;
                }
            } else if (bps == 8) {
                format = AL10.AL_FORMAT_STEREO8;
            } else {
                format = AL10.AL_FORMAT_STEREO16;
            }
    
            ByteBuffer buf = BufferUtils.createByteBuffer(data.length);
            
            buf.put(data);
            buf.flip();

            Sound sound = new Sound(Game.getInstance().getSoundEngine());

            AL10.alBufferData(sound.getBuffer(), format, buf, sampleRate);
            AL10.alSourcei(sound.getSource(), AL10.AL_BUFFER, sound.getBuffer());

            sound.getEngine().checkError("SoundLoader.load()");

            return sound;
        }
    }

    private final SoundEngine engine;
    private final int source;
    private final int buffer;
    
    public Sound(SoundEngine engine) {
        this.engine = engine;

        source = AL10.alGenSources();
        buffer = AL10.alGenBuffers();
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public int getSource() {
        return source;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setVolume(float value) {
        AL10.alSourcef(source, AL10.AL_GAIN, Math.max(0, Math.min(1, value)));
    }

    public void stop() {
        AL10.alSourceStop(source);
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public void play(boolean looping) {
        stop();
        AL10.alSourcei(source, AL10.AL_LOOPING, (looping) ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcePlay(source);
    }

    @Override
    public void destroy() throws Exception {
        stop();
        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
        super.destroy();
    }
}
