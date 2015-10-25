/*
 * Copyright (C) 2015 Raatiniemi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
