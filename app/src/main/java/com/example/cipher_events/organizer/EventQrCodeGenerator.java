package com.example.cipher_events.organizer;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QR helper for building and parsing event QR payloads.
 *
 * Payload format:
 * cipher-event://event/{eventId}
 */
public final class EventQrCodeGenerator {
    public static final String QR_PREFIX = "cipher-event://event/";

    private EventQrCodeGenerator() {
        // Utility class
    }

    public static String buildPayload(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID is required.");
        }
        return QR_PREFIX + eventId.trim();
    }

    public static String extractEventId(String qrPayload) {
        if (qrPayload == null || qrPayload.trim().isEmpty()) {
            throw new IllegalArgumentException("QR payload is empty.");
        }

        String trimmed = qrPayload.trim();
        if (!trimmed.startsWith(QR_PREFIX)) {
            throw new IllegalArgumentException("Invalid QR payload format.");
        }

        String eventId = trimmed.substring(QR_PREFIX.length()).trim();
        if (eventId.isEmpty()) {
            throw new IllegalArgumentException("QR payload does not contain an event ID.");
        }

        return eventId;
    }

    public static Bitmap generateQrBitmap(String qrPayload, int width, int height) throws WriterException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("QR bitmap width and height must be positive.");
        }

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(qrPayload, BarcodeFormat.QR_CODE, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
