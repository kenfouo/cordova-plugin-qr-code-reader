package com.example.qrreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class QRReader extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("decodeImage".equals(action)) {
            String base64Image = args.getString(0);
            decodeImage(base64Image, callbackContext);
            return true;
        }
        return false;
    }

    private void decodeImage(String base64Image, CallbackContext callbackContext) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(decodedBytes), null, options);

            if (bitmap == null) {
                callbackContext.error("Image non valide. Impossible de décoder le bitmap.");
                return;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            MultiFormatReader reader = new MultiFormatReader();
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);

            // Hint: Tell ZXing to try harder to find and decode the barcode
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            // Optional: If you only expect QR codes, specify the format
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE));

            Result result = null;
            try {
                result = reader.decode(binaryBitmap, hints); // Pass hints to the decode method
            } catch (NotFoundException | FormatException e) {
                // Try again without specifying format if the first attempt failed,
                // in case it's a different barcode type or an issue with the hint.
                // This makes it more robust, but might be slightly slower.
                try {
                    reader.reset(); // Reset the reader for a new attempt
                    result = reader.decode(binaryBitmap); // Try without hints
                } catch (NotFoundException | ChecksumException | FormatException ex) {
                    throw ex; // Re-throw if it still fails
                }
            }


            callbackContext.success(result.getText());

        } catch (NotFoundException e) {
            callbackContext.error("Aucun QR code détecté ou code illisible. Veuillez essayer d'améliorer l'image (éclairage, netteté).");
        } catch (Exception e) {
            callbackContext.error("Erreur lors du décodage de l'image : " + e.getMessage());
        } finally {
            // It's good practice to recycle the bitmap if you are done with it
            // if (bitmap != null && !bitmap.isRecycled()) {
            //     bitmap.recycle();
            // }
        }
    }
}
