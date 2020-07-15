# Linker
![license](https://img.shields.io/badge/license-GPLv2-blue.svg)

The application takes a source directory and indexes the containing directories. These directories are then checked against one or more target directories, which source directories do not have a symbolic link within the target directories.

## Example

```
source/directory-1
source/directory-2
source/directory-3
target/directory-1 -> ../source/directory-1
target/directory-3 -> ../source/directory-2
```

With the sample directory structure only the `source/directory-3` will be shown as unlinked. Note that the application works with the basename of the source of the symbolic link, i.e. the linked name do not really matters.

## How to

Run the application via Gradle: `./gradlew run -Pconfig=path/to/configuration-file`

Sample configuration file
```json
{
    // Only one source directory can be supplied.
    "source": "/path/to/source-directory",

    // One or more target directories can be supplied.
    "targets": [
        "/path/to/target-directory-1",
        "/path/to/target-directory-2"
    ],

    // One or more exclude directories can be supplied.
    //
    // Also, only the basename of the directory should be supplied.
    // Full or partial path exclusion is not supported.
    "excludes": [
        "exclude1",
        "exclude2"
    ],

    // One or more link maps can be supplied.
    "linkMaps": [
        {
            "regex": "^(?i)(target[-]item[-]1)",

            "prefix": "../source-directory",
            "target": "/path/to/target-directory-1"
        }
    ]
}
```

### LinkMaps

To enable automatic linking, i.e. the application creates symbolic links based on a regular expression.

Each configuration item must supply a regex, prefix, and target.

* **regex** matches a basename.
* **prefix** prepend the link target, can be a relative path from target.
* **target** is the location to which the link will be created.

The sample configuration will link matching items with the equivalent `ln`-command:
```
ln -s ../source-directory/target-item-1 /path/to/target-directory-1/target-item-1
```

## License

```
linker
Copyright (C) 2020 raatiniemi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
