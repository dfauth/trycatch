package com.github.dfauth.trycatch;

import com.github.dfauth.Lazy;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LazyTest {

    @Test
    public void testIt() {
        var ref = new Object() {
            int cnt = 0;
        };
        Lazy<TestObj> lazy = Lazy.of(() -> {
            ref.cnt++;
            return new TestObj();
        });
        assertEquals(0, ref.cnt);
        TestObj it = lazy.get();
        assertNotNull(it);
        assertEquals(1, ref.cnt);
        assertEquals(it, lazy.get());
        assertEquals(1, ref.cnt);
    }

    class TestObj {
    }
}
