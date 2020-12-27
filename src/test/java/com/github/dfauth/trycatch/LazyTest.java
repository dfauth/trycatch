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
            return new TestObj(1);
        });
        assertEquals(0, ref.cnt);
        TestObj it = lazy.get();
        assertNotNull(it);
        assertEquals(1, ref.cnt);
        assertEquals(it, lazy.get());
        assertEquals(1, ref.cnt);
    }

    @Test
    public void testMap() {
        var ref = new Object() {
            int cnt = 0;
        };
        Lazy<TestObj> lazy = Lazy.of(() -> {
            ref.cnt++;
            return new TestObj(1);
        });
        assertEquals(0, ref.cnt);
        Lazy<TestObj> lazy2 = lazy.map(t -> new TestObj(t.getI() * 2));
        assertNotNull(lazy2);
        assertEquals(0, ref.cnt);

        TestObj it2 = lazy2.get();
        assertEquals(2, it2.getI());
        assertEquals(1, ref.cnt);

        TestObj it = lazy.get();
        assertEquals(1, it.getI());
        assertEquals(1, ref.cnt);
    }

    class TestObj {
        private final int i;

        TestObj(int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }
    }
}
