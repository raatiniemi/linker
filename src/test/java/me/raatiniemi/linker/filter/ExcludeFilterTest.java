package me.raatiniemi.linker.filter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ExcludeFilterTest {
    @Test
    public void filter_withoutObject() {
        Object object1 = new Object();
        Object object2 = new Object();

        List<Object> data = new ArrayList<>();
        data.add(object1);

        assertTrue(ExcludeFilter.filter(object2, data));
    }

    @Test
    public void filter_withObject() {
        Object object1 = new Object();
        Object object2 = new Object();

        List<Object> data = new ArrayList<>();
        data.add(object1);
        data.add(object2);

        assertFalse(ExcludeFilter.filter(object2, data));
    }
}
