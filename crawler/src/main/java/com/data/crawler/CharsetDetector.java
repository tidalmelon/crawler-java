package com.data.crawler;

import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetector {

    private static UniversalDetector detector = new UniversalDetector(null);

    public static String getEncoding(byte[] data) {
        for (int i = 0; i < data.length ; i += 4096) {

            int size = 4096;
            if ((i + 4096) > data.length) {
                size = data.length - i;
            }
            detector.handleData(data, i, size);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        //if (null != encoding) {
        //    System.out.println("detected encoding: " + encoding);
        //} else {
        //    System.out.println("No encoding detected!");
        //}
        if (null == encoding) {
            encoding = "utf-8";
        }
        detector.reset();
        return encoding;
    }
}
