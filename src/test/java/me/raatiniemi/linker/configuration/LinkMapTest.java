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

package me.raatiniemi.linker.configuration;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class LinkMapTest {
    @DataProvider
    public static Object[][] match_withoutMatchDataProvider() {
        return new Object[][]{
                {"match", "without-match"},
                {"^match[-]\\d+$", "without-match"},
                {"^(?i)match[-]\\d+$", "without-match"}
        };
    }

    @DataProvider
    public static Object[][] match_withMatchDataProvider() {
        return new Object[][]{
                {"match", "match"},
                {"^match[-]\\d+$", "match-20"},
                {"^(?i)match[-]\\d+$", "Match-530"}
        };
    }

    @Test
    public void match_withoutRegex() {
        LinkMap map = new LinkMap();

        assertFalse(map.match("without-regex"));
    }

    @Test
    @UseDataProvider("match_withoutMatchDataProvider")
    public void match_withoutMatch(String regex, String basename) {
        LinkMap map = new LinkMap();
        map.setRegex(regex);

        assertFalse(map.match(basename));
    }

    @Test
    @UseDataProvider("match_withMatchDataProvider")
    public void match_withMatch(String regex, String basename) {
        LinkMap map = new LinkMap();
        map.setRegex(regex);

        assertTrue(map.match(basename));
    }
}
