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

use std::error::Error;
use std::fs;

use json::JsonValue;

#[derive(Default, Eq, PartialEq, Clone, Debug)]
pub struct Configuration {
    pub source: Option<String>,
    pub targets: Vec<String>,
    pub excludes: Vec<String>,
    pub link_maps: Vec<LinkMap>,
}

#[derive(Eq, PartialEq, Clone, Debug)]
pub struct LinkMap {
    pub regex: String,
    pub target: String,
}

impl LinkMap {
    pub(crate) fn new(regex: String, target: String) -> Result<Self, Box<dyn Error>> {
        Ok(
            LinkMap {
                regex,
                target,
            }
        )
    }
}

pub fn read_configuration(path: &str) -> Configuration {
    let data = fs::read_to_string(path)
        .expect(&format!("Unable to read configuration file at path {}", path).as_str());

    let configuration = parse_configuration(data.as_str());
    if configuration.source.is_none() {
        panic!("Configuration is missing valid source")
    }
    if configuration.targets.is_empty() {
        panic!("Configuration is missing valid targets")
    }

    configuration
}

fn parse_configuration(configuration: &str) -> Configuration {
    if configuration.is_empty() {
        return Default::default();
    }

    let data = json::parse(configuration).unwrap();
    return Configuration {
        source: map_source(&data),
        targets: map_targets(&data),
        excludes: map_excludes(&data),
        link_maps: map_link_maps(&data),
    };
}

fn map_source(data: &JsonValue) -> Option<String> {
    data["source"].as_str()
        .map_or(None, map_valid_source)
}

fn map_valid_source(v: &str) -> Option<String> {
    if !v.is_empty() {
        Some(v.to_string())
    } else {
        None
    }
}

fn map_targets(data: &JsonValue) -> Vec<String> {
    match data["targets"] {
        JsonValue::Array(ref value) => map_valid_targets(value),
        _ => Vec::new()
    }
}

fn map_valid_targets(value: &[JsonValue]) -> Vec<String> {
    value.iter()
        .map(|v| {
            v.as_str().expect("Invalid value in target configuration")
                .to_string()
        })
        .collect()
}

fn map_excludes(data: &JsonValue) -> Vec<String> {
    match data["excludes"] {
        JsonValue::Array(ref value) => map_valid_excludes(value),
        _ => Vec::new()
    }
}

fn map_valid_excludes(value: &[JsonValue]) -> Vec<String> {
    value.iter()
        .map(|v| {
            v.as_str().expect("Invalid value in exclude configuration")
                .to_string()
        })
        .map(|v| v.to_lowercase())
        .collect()
}

fn map_link_maps(data: &JsonValue) -> Vec<LinkMap> {
    match data["linkMaps"] {
        JsonValue::Array(ref value) => map_valid_link_maps(value),
        _ => Vec::new(),
    }
}

fn map_valid_link_maps(value: &[JsonValue]) -> Vec<LinkMap> {
    value.iter()
        .filter(|v| v["regex"].is_string() && v["target"].is_string())
        .map(|v| {
            let regex = v["regex"].as_str()
                .expect("Invalid value in link map regex configuration")
                .to_string();
            let target = v["target"].as_str()
                .expect("Invalid value in link map target configuration")
                .to_string();
            (regex, target)
        })
        .filter(|(regex, target)| !regex.is_empty() && !target.is_empty())
        .map(|(regex, target)| {
            LinkMap::new(regex, target)
                .expect("Unable to create link map")
        })
        .collect()
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_configuration_with_empty_configuration() {
        let configuration: &str = "";
        let expected: Configuration = Default::default();

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_without_source() {
        let configuration: &str = r#"
        {
        }
        "#;
        let expected: Configuration = Default::default();

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_with_empty_source() {
        let configuration: &str = r#"
        {
            "source": ""
        }
        "#;
        let expected: Configuration = Configuration {
            source: None,
            targets: Vec::new(),
            excludes: Vec::new(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_without_targets() {
        let configuration: &str = r#"
        {
            "source": "/tmp"
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/tmp".to_string()),
            targets: Vec::new(),
            excludes: Vec::new(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_with_empty_targets() {
        let configuration: &str = r#"
        {
            "source": "/tmp",
            "targets": []
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/tmp".to_string()),
            targets: Vec::new(),
            excludes: Vec::new(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_with_required_configuration() {
        let configuration: &str = r#"
        {
            "source": "/var/cache/pacman/pkg",
            "targets": [
                "/var/www/archlinux/pkg"
            ]
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/var/cache/pacman/pkg".to_string()),
            targets: vec![
                "/var/www/archlinux/pkg".to_string()
            ],
            excludes: Vec::new(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_without_link_map_regex_configuration() {
        let configuration: &str = r#"
        {
            "source": "/var/cache/pacman/pkg",
            "targets": [
                "/var/www/archlinux/pkg"
            ],
            "excludes": [
                "*zip"
            ],
            "linkMaps": [
                {
                    "target": "/var/www/archlinux/pkg"
                }
            ]
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/var/cache/pacman/pkg".to_string()),
            targets: vec![
                "/var/www/archlinux/pkg".to_string()
            ],
            excludes: vec![
                "*zip".to_string()
            ],
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_without_link_map_target_configuration() {
        let configuration: &str = r#"
        {
            "source": "/var/cache/pacman/pkg",
            "targets": [
                "/var/www/archlinux/pkg"
            ],
            "excludes": [
                "*zip"
            ],
            "linkMaps": [
                {
                    "regex": "(.*)\\.pkg\\.tar\\.xz"
                }
            ]
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/var/cache/pacman/pkg".to_string()),
            targets: vec![
                "/var/www/archlinux/pkg".to_string()
            ],
            excludes: vec![
                "*zip".to_string()
            ],
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_with_empty_link_map_regex_configuration() {
        let configuration: &str = r#"
        {
            "source": "/var/cache/pacman/pkg",
            "targets": [
                "/var/www/archlinux/pkg"
            ],
            "excludes": [
                "*zip"
            ],
            "linkMaps": [
                {
                    "regex": "",
                    "target": "/var/www/archlinux/pkg"
                }
            ]
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/var/cache/pacman/pkg".to_string()),
            targets: vec![
                "/var/www/archlinux/pkg".to_string()
            ],
            excludes: vec![
                "*zip".to_string()
            ],
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_with_empty_link_map_target_configuration() {
        let configuration: &str = r#"
        {
            "source": "/var/cache/pacman/pkg",
            "targets": [
                "/var/www/archlinux/pkg"
            ],
            "excludes": [
                "*zip"
            ],
            "linkMaps": [
                {
                    "regex": "(.*)\\.pkg\\.tar\\.xz",
                    "target": ""
                }
            ]
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/var/cache/pacman/pkg".to_string()),
            targets: vec![
                "/var/www/archlinux/pkg".to_string()
            ],
            excludes: vec![
                "*zip".to_string()
            ],
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    #[test]
    fn parse_configuration_with_full_configuration() {
        let configuration: &str = r#"
        {
            "source": "/var/cache/pacman/pkg",
            "targets": [
                "/var/www/archlinux/pkg"
            ],
            "excludes": [
                "*zip"
            ],
            "linkMaps": [
                {
                    "regex": "(.*)\\.pkg\\.tar\\.xz",
                    "target": "/var/www/archlinux/pkg"
                }
            ]
        }
        "#;
        let expected: Configuration = Configuration {
            source: Some("/var/cache/pacman/pkg".to_string()),
            targets: vec![
                "/var/www/archlinux/pkg".to_string()
            ],
            excludes: vec![
                "*zip".to_string()
            ],
            link_maps: vec![
                LinkMap::new(
                    "(.*)\\.pkg\\.tar\\.xz".to_string(),
                    "/var/www/archlinux/pkg".to_string(),
                ).unwrap()
            ],
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }
}
