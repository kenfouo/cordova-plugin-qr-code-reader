package com.example.qrreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.util.*;

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

            Bitmap bitmap = BitmapFactory.decodeStream(
                    new ByteArrayInputStream(decodedBytes),
                    null,
                    options
            );

            if (bitmap == null) {
                callbackContext.error("Image non valide.");
                return;
            }

            Result result = decodeWithRotations(bitmap);

            if (result == null) {
                callbackContext.error("Aucun code détecté.");
                return;
            }

            String value = result.getText();
            String format = result.getBarcodeFormat().toString();

            String output = value + ":::" + format + ":::false:::Images";

            callbackContext.success(output);

        } catch (Exception e) {
            callbackContext.error("Erreur lors du décodage : " + e.getMessage());
        }
    }

    private Result decodeWithRotations(Bitmap bitmap) {

        int[] rotations = {0, 90, 180, 270};

        for (int rotation : rotations) {

            Bitmap rotated = rotateBitmap(bitmap, rotation);

            Result result = tryDecode(rotated);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Result tryDecode(Bitmap bitmap) {

        try {

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            MultiFormatReader reader = new MultiFormatReader();

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);

            EnumSet<BarcodeFormat> formats = EnumSet.of(

                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.PDF_417,
                    BarcodeFormat.DATA_MATRIX,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODABAR,
                    BarcodeFormat.ITF,
                    BarcodeFormat.RSS_14,
                    BarcodeFormat.RSS_EXPANDED,
                    BarcodeFormat.AZTEC
            );

            hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);

            // améliore la détection
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            reader.setHints(hints);

            return reader.decode(binaryBitmap);

        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {

        if (angle == 0) return source;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true
        );
    }
}