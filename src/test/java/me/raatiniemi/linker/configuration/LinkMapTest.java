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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.nonNull;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(Parameterized.class)
public class LinkMapTest {
    private final Boolean expected;
    private final String regex;
    private final String match;

    public LinkMapTest(Boolean expected, String regex, String match) {
        this.expected = expected;
        this.regex = regex;
        this.match = match;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[][]{
                        {
                                Boolean.FALSE,
                                "",
                                "without-regex"
                        },
                        {
                                Boolean.FALSE,
                                "match",
                                "without-match"
                        },
                        {
                                Boolean.FALSE,
                                "^match[-]\\d+$",
                                "without-match"
                        },
                        {
                                Boolean.FALSE,
                                "^(?i)match[-]\\d+$",
                                "without-match"
                        },
                        {
                                Boolean.TRUE,
                                "match",
                                "match"
                        },
                        {
                                Boolean.TRUE,
                                "^match[-]\\d+$",
                                "match-20"
                        },
                        {
                                Boolean.TRUE,
                                "^(?i)match[-]\\d+$",
                                "Match-530"
                        },
                }
        );
    }

    @Test
    public void match() {
        LinkMap linkMap = new LinkMap(regex, "", "");

        if (expected) {
            assertTrue(linkMap.match(match));
            return;
        }

        assertFalse(linkMap.match(match));
    }
}
