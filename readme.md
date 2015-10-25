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
```
# Only one source directory can be supplied.
source.directory=/path/to/source-directory

# One or more target directories can be supplied, not that each
# property name have to be unique and begin with 'target.directory'.
target.directory1=/path/to/target-directory-1
target.directory2=/path/to/target-directory-2
```

## License

Copyright (C) 2015 Raatiniemi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.