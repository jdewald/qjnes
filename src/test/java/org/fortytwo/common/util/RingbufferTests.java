package org.fortytwo.common.util;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RingbufferTests {

    private Ringbuffer ringbuffer;

    @Before
    public void initRingbuffer() {
        var objectsToAdd = List.of("0Ala ma kota", 11, 223.4, 300_000_000L);

        ringbuffer = new Ringbuffer(3);
        objectsToAdd.forEach(object -> ringbuffer.add(object));
    }

    @Test
    public void testFirstAndCurrent() {
        assertEquals(11, ringbuffer.getFirst());
        assertEquals(11, ringbuffer.getCurrent());
    }

    @Test
    public void testPreviousAndNext() {
        assertNull(ringbuffer.getNext());
    }

    @Test
    public void testAdding() {
        ringbuffer.add("Litwo, Ojczyzno moja");
        assertEquals(223.4, ringbuffer.getFirst());
        assertEquals(300_000_000L, ringbuffer.getNext());
        assertEquals(300_000_000L, ringbuffer.getCurrent());

    }


    @Test
    public void testAddFirst(){
        assertEquals(11, ringbuffer.getFirst());
        assertEquals(11, ringbuffer.getCurrent());
        assertEquals(223.4, ringbuffer.getNext());
    }
}
