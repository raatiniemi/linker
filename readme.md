# linker

[![license](https://img.shields.io/badge/license-GPLv2-blue.svg)](license)
[![pipeline status](https://gitlab.com/rahome/linker/badges/main/pipeline.svg)](https://gitlab.com/rahome/linker/-/commits/master)
[![coverage report](https://gitlab.com/rahome/linker/badges/main/coverage.svg)](https://gitlab.com/rahome/linker/-/commits/master)

The application will index a source directory and compare the index against one
or more target directories. Based on whether link map configuration exists for
the node or not, the node will either be linked or printed as unlinked source
node.

## Example

```
/source/directory-1
/source/directory-2
/source/directory-3
/target/directory-1 -> /source/directory-1
/target/directory-3 -> /source/directory-2
```

With the sample directory structure only the `/source/directory-3` will be shown
as unlinked. Note that the application works with the basename of the source of
the symbolic link, i.e. the linked name do not really matters.

## How to

In order to build the application, run `./gradlew build shadowJar`. This will
produce a JAR which will be located within the `build/libs` directory, with the
name `linker-$version-all.jar`. *Note that you need to swap the `$version` with
the actual version.* To run this JAR, with the necessary configuration, use the
following `java -jar build/libs/linker-$version-all.jar configuration.json`.

Sample configuration file:

```json
{
    "source": "/path/to/source-directory",
    "targets": [
        "/path/to/target-directory-1",
        "/path/to/target-directory-2"
    ],
    "excludes": [
        "exclude1",
        "exclude2"
    ],
    "linkMaps": [
        {
            "regex": "^(?i)(target[-]item[-]1)",
            "target": "/path/to/target-directory-1"
        }
    ]
}
```

### LinkMaps

To enable automatic linking, i.e. the application creates symbolic links
based on a regular expression.

Each configuration item must supply a regex and target.

* **regex** matches a basename.
* **target** is the location to which the link will be created.

The sample configuration will link matching items with the equivalent
`ln`-command:

```sh
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
