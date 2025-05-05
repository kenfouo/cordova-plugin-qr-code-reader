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
                callbackContext.error("Image non valide.");
                return;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(binaryBitmap);

            callbackContext.success(result.getText());

        } catch (NotFoundException e) {
            callbackContext.error("Aucun QR code détecté.");
        } catch (Exception e) {
            callbackContext.error("Erreur lors du décodage : " + e.getMessage());
        }
    }
}
