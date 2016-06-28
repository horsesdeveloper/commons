package com.horses.camera.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Brian Salvattore
 */
public class IOUtil {

    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException ignore) { }

        finally {

            try {
                is.close();
            }
            catch (IOException ignore) { }
        }

        return sb.toString();
    }

    public static void closeStream(Closeable stream) {

        try {

            if (stream != null)
                stream.close();
        }
        catch (IOException ignore) { }
    }

    public static byte[] InputStreamToByte(InputStream is) throws IOException {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            byteStream.write(ch);
        }
        byte byteData[] = byteStream.toByteArray();
        byteStream.close();
        return byteData;
    }
}
