package com.gesh.backend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EcgSignalParser {

    private static final int EXPECTED_LEADS = 12;
    private static final int EXPECTED_SAMPLES = 1000;

    private static final Pattern SHAPE_PATTERN = Pattern.compile("'shape':\\s*\\(([^)]*)\\)");
    private static final Pattern DESCR_PATTERN = Pattern.compile("'descr':\\s*'([^']+)'");

    private EcgSignalParser() {
    }

    public static float[][] parseCsv(InputStream inputStream) throws IOException {
        List<float[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                float[] row = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    row[i] = Float.parseFloat(parts[i].trim());
                }
                rows.add(row);
            }
        }

        if (rows.size() != EXPECTED_LEADS) {
            throw new IllegalArgumentException(
                    "فایل باید دقیقاً ۱۲ خط (۱۲ لید) داشته باشه؛ تعداد خط‌های پیدا‌شده: " + rows.size());
        }

        return toFixedShape(rows);
    }

    public static float[][] parseNpy(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();

        if (bytes.length < 10 || (bytes[0] & 0xFF) != 0x93
                || bytes[1] != 'N' || bytes[2] != 'U' || bytes[3] != 'M' || bytes[4] != 'P' || bytes[5] != 'Y') {
            throw new IllegalArgumentException("فایل یه فایل .npy معتبر نیست (magic number اشتباهه)");
        }

        int majorVersion = bytes[6] & 0xFF;
        int headerLen;
        int headerStart;
        if (majorVersion == 1) {
            headerLen = (bytes[8] & 0xFF) | ((bytes[9] & 0xFF) << 8);
            headerStart = 10;
        } else {
            headerLen = (bytes[8] & 0xFF) | ((bytes[9] & 0xFF) << 8)
                    | ((bytes[10] & 0xFF) << 16) | ((bytes[11] & 0xFF) << 24);
            headerStart = 12;
        }

        String header = new String(bytes, headerStart, headerLen, StandardCharsets.US_ASCII);

        if (header.contains("'fortran_order': True")) {
            throw new IllegalArgumentException("فایل .npy با fortran_order=True پشتیبانی نمی‌شه");
        }

        Matcher descrMatcher = DESCR_PATTERN.matcher(header);
        if (!descrMatcher.find()) {
            throw new IllegalArgumentException("هدر .npy فیلد descr نداشت");
        }
        String descr = descrMatcher.group(1);

        Matcher shapeMatcher = SHAPE_PATTERN.matcher(header);
        if (!shapeMatcher.find()) {
            throw new IllegalArgumentException("هدر .npy فیلد shape نداشت");
        }
        String[] shapeParts = shapeMatcher.group(1).split(",");
        List<Integer> shape = new ArrayList<>();
        for (String part : shapeParts) {
            part = part.trim();
            if (!part.isEmpty()) {
                shape.add(Integer.parseInt(part));
            }
        }
        if (shape.size() != 2) {
            throw new IllegalArgumentException("فقط آرایه‌ی دوبعدی (leads, samples) پشتیبانی می‌شه؛ shape فایل: " + shape);
        }

        int leads = shape.get(0);
        int samples = shape.get(1);

        if (leads != EXPECTED_LEADS) {
            throw new IllegalArgumentException("فایل باید " + EXPECTED_LEADS + " لید داشته باشه؛ این فایل " + leads + " لید داره");
        }

        int dataStart = headerStart + headerLen;
        ByteBuffer buffer = ByteBuffer.wrap(bytes, dataStart, bytes.length - dataStart);
        buffer.order(descr.startsWith(">") ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        float[] flat = new float[leads * samples];
        if (descr.contains("f8")) {
            for (int i = 0; i < flat.length; i++) {
                flat[i] = (float) buffer.getDouble();
            }
        } else if (descr.contains("f4")) {
            for (int i = 0; i < flat.length; i++) {
                flat[i] = buffer.getFloat();
            }
        } else {
            throw new IllegalArgumentException("فقط dtype از نوع float32 یا float64 پشتیبانی می‌شه؛ این فایل: " + descr);
        }

        List<float[]> rows = new ArrayList<>();
        for (int lead = 0; lead < leads; lead++) {
            float[] row = new float[samples];
            System.arraycopy(flat, lead * samples, row, 0, samples);
            rows.add(row);
        }

        return toFixedShape(rows);
    }

    private static float[][] toFixedShape(List<float[]> rows) {
        float[][] result = new float[EXPECTED_LEADS][EXPECTED_SAMPLES];
        for (int lead = 0; lead < EXPECTED_LEADS; lead++) {
            result[lead] = resample(rows.get(lead), EXPECTED_SAMPLES);
        }
        return result;
    }

    private static float[] resample(float[] input, int targetLength) {
        if (input.length == targetLength) {
            return input;
        }
        float[] output = new float[targetLength];
        float ratio = (float) (input.length - 1) / (targetLength - 1);
        for (int i = 0; i < targetLength; i++) {
            float pos = i * ratio;
            int lower = (int) Math.floor(pos);
            int upper = Math.min(lower + 1, input.length - 1);
            float frac = pos - lower;
            output[i] = input[lower] * (1 - frac) + input[upper] * frac;
        }
        return output;
    }
}