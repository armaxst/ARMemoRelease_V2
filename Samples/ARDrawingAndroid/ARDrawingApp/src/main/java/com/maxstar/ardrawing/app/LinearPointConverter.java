/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/
package com.maxstar.ardrawing.app;

/**
 * Calculate envelope
 */
public class LinearPointConverter {

    private float envelope = 1.0f;

	public LinearPointConverter(int src1, int src2, int dst1, int dst2) {
        envelope = (dst2 - dst1) / (float) (src2 - src1);
    }

    public float getConvertedValue(float input) {
        return input * envelope;
    }

    public float getEnvelope() {
        return envelope;
    }
}
