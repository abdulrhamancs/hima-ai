package com.hima.ai.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceBearingTest {

    @Test
    fun `metric distance and bearing remain ordered inside RTL text`() {
        assertEquals("\u2066850 m · NE\u2069", DistanceBearing(850f, 45f).formatLabel())
        assertEquals("\u20662.3 km · SW\u2069", DistanceBearing(2_300f, 225f).formatLabel())
    }
}
