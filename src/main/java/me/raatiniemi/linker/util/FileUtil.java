/*
 * linker
 * Copyright (C) 2020 raatiniemi
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

package me.raatiniemi.linker.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    /**
     * Create a symbolic link between two locations.
     *
     * @param link Location of the link.
     * @param target Target of the link.
     * @return true if symbolic link was created, otherwise false.
     */
    public static boolean createSymbolicLink(Path link, Path target) {
        System.out.println("Linking: " + link.getFileName());

        try {
            Files.createSymbolicLink(link, target);
            return true;
        } catch (IOException e) {
            System.out.println("Failed to link: " + e.getMessage());

            return false;
        }
    }
}
