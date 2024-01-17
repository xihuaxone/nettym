package com.xihua.nettym.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LocalCmdUtil {
    public static String run(String cmd) throws Exception {
        StringBuilder res = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(cmd);

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;

            while ((line = reader.readLine()) != null) {
                res.append(line);
            }

            process.waitFor();
        } catch (Exception e) {
            throw new Exception(e);
        }

        return res.toString();
    }
}
