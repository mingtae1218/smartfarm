package com.example.smartfarm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import me.aflak.bluetooth.reader.SocketReader;

public class ByteReader extends SocketReader {

    public ByteReader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public byte[] read() throws IOException {
        byte[] buf = new byte[10];
        int len = inputStream.read(buf);
        return Arrays.copyOf(buf, len);
    }
}
