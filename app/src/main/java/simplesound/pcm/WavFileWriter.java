package simplesound.pcm;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Writes a wav file. Careful that it writes the total amount of the bytes information once the close method
 * is called. It has a counter in it to calculate the samle size.
 */
public class WavFileWriter implements Closeable {

    private final WavAudioFormat pcmAudioFormat;
    private final PcmAudioOutputStream pos;
    private int totalSampleBytesWritten = 0;
    private final File file;

    public WavFileWriter(WavAudioFormat pcmAudioFormat, File file) throws IOException {
        if (pcmAudioFormat.isBigEndian())
            throw new IllegalArgumentException("Wav file cannot contain bigEndian sample data.");
        if (pcmAudioFormat.getSampleSizeInBits() > 8 && !pcmAudioFormat.isSigned())
            throw new IllegalArgumentException("Wav file cannot contain unsigned data for this sampleSize:"
                    + pcmAudioFormat.getSampleSizeInBits());
        this.pcmAudioFormat = pcmAudioFormat;
        this.file = file;
        this.pos = new PcmAudioOutputStream(pcmAudioFormat, file);
        pos.write(new RiffHeaderData(pcmAudioFormat, 0).asByteArray());
    }

    public WavFileWriter write(byte[] bytes) throws IOException {
        checkLimit(totalSampleBytesWritten, bytes.length);
        pos.write(bytes);
        totalSampleBytesWritten += bytes.length;
        return this;
    }

    private void checkLimit(int total, int toAdd) {
        final long result = total + toAdd;
        if (result >= Integer.MAX_VALUE) {
            throw new IllegalStateException("Size of bytes is too big:" + result);
        }
    }

    public WavFileWriter write(short[] shorts) throws IOException {
        checkLimit(totalSampleBytesWritten, shorts.length * 2);
        pos.write(shorts);
        totalSampleBytesWritten += shorts.length * 2;
        return this;
    }

    public WavFileWriter writeStereo(int[] channel0, int[] channel1) throws IOException {
        if (channel0.length != channel1.length)
            throw new IllegalArgumentException("channels must have equeal amount of data.");
        final int bytePerSample = pcmAudioFormat.getBytePerSample();
        for (int i = 0; i < channel0.length; i++) {
            pos.write(Bytes.toByteArray(channel0[i], bytePerSample, false));
            pos.write(Bytes.toByteArray(channel1[i], bytePerSample, false));
            totalSampleBytesWritten += (bytePerSample * 2);
        }
        return this;
    }

    public WavFileWriter write(int[] samples) throws IOException {
        final int bytePerSample = pcmAudioFormat.getBytePerSample();
        checkLimit(totalSampleBytesWritten, samples.length * bytePerSample);
        pos.write(samples);
        totalSampleBytesWritten += samples.length * bytePerSample;
        return this;
    }

    public void close() throws IOException {
        pos.close();
        PcmAudioHelper.modifyRiffSizeData(file, totalSampleBytesWritten);
    }

    public PcmAudioFormat getWavFormat() {
        return pcmAudioFormat;
    }


    public int getTotalSampleBytesWritten() {
        return totalSampleBytesWritten;
    }
}