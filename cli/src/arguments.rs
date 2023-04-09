/*
 * linker
 * Copyright (C) 2021 raatiniemi
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

use std::env::ArgsOs;
use std::ffi::OsString;

use clap::{App, Arg};

#[derive(Eq, PartialEq, Debug)]
pub struct Arguments {
    pub configuration: String,
    pub dry_run: bool,
}

pub fn collect_and_parse_arguments(args: ArgsOs) -> Arguments {
        parse_arguments(collect_arguments(args))
}

fn collect_arguments(args: ArgsOs) -> Vec<OsString> {
    args.map(|v| v).collect::<Vec<OsString>>()
}

fn parse_arguments(args: Vec<OsString>) -> Arguments {
    let matches = App::new("linker")
        .author("Tobias Raatiniemi <raatiniemi@gmail.com>")
        .arg(Arg::with_name("dry-run")
            .long("dry-run")
            .help("Run without performing any actual changes")
            .takes_value(false))
        .arg(Arg::with_name("configuration")
            .short("c")
            .long("configuration")
            .value_name("configuration")
            .help("Path to configuration file")
            .takes_value(true))
        .get_matches_from(args);

    return Arguments {
        configuration: matches.value_of("configuration")
            .filter(|v| !v.is_empty())
            .expect(&format!("No path for configuration is available"))
            .to_string(),
        dry_run: matches.is_present("dry-run"),
    };
}

#[cfg(test)]
mod tests {
    use std::ffi::OsString;

    use crate::arguments::{Arguments, parse_arguments};

    fn transform_and_parse_arguments(arguments: Vec<&str>) -> Arguments {
        parse_arguments(
            arguments.iter()
                .map(|v| OsString::from(v))
                .collect()
        )
    }

    #[test]
    #[should_panic]
    fn parse_arguments_without_arguments() {
        let arguments: Vec<&str> = vec![];

        transform_and_parse_arguments(arguments);
    }

    #[test]
    #[should_panic]
    fn parse_arguments_with_binary_argument() {
        let arguments: Vec<&str> = vec!["linker"];

        transform_and_parse_arguments(arguments);
    }

    #[test]
    fn parse_arguments_with_short_configuration() {
        let arguments: Vec<&str> = vec!["linker", "-c", "path-to-configuration"];
        let expected = Arguments {
            configuration: "path-to-configuration".to_string(),
            dry_run: false,
        };

        let actual = transform_and_parse_arguments(arguments);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_arguments_with_long_configuration() {
        let arguments: Vec<&str> = vec!["linker", "--configuration", "path-to-configuration"];
        let expected = Arguments {
            configuration: "path-to-configuration".to_string(),
            dry_run: false,
        };

        let actual = transform_and_parse_arguments(arguments);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_arguments_with_dry_run() {
        let arguments: Vec<&str> = vec!["linker", "--dry-run", "--configuration", "path-to-configuration"];
        let expected = Arguments {
            configuration: "path-to-configuration".to_string(),
            dry_run: true,
        };

        let actual = transform_and_parse_arguments(arguments);

        assert_eq!(expected, actual)
    }
}
