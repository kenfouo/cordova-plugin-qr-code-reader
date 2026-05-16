package com.example.qrreader;

import com.google.zxing.*;

import java.util.Map;

public final class MSIReader implements Reader {

    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException {
        throw NotFoundException.getNotFoundInstance();
    }

    @Override
    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        throw NotFoundException.getNotFoundInstance();
    }

    @Override
    public void reset() {
    }
}