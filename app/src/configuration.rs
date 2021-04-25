use std::fs;

use json::JsonValue;

#[derive(Default, Eq, PartialEq, Clone, Debug)]
pub struct Configuration {
    pub source: Option<String>,
    pub targets: Vec<String>,
    pub excludes: Vec<String>,
    link_maps: Vec<LinkMap>,
}

#[derive(Eq, PartialEq, Clone, Debug)]
pub struct LinkMap {
    regex: String,
    target: String,
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

    return configuration;
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

fn map_valid_targets(value: &Vec<JsonValue>) -> Vec<String> {
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

fn map_valid_excludes(value: &Vec<JsonValue>) -> Vec<String> {
    value.iter()
        .map(|v| {
            v.as_str().expect("Invalid value in exclude configuration")
                .to_string()
        })
        .collect()
}

fn map_link_maps(data: &JsonValue) -> Vec<LinkMap> {
    match data["linkMaps"] {
        JsonValue::Array(ref value) => map_valid_link_maps(value),
        _ => Vec::new(),
    }
}

fn map_valid_link_maps(value: &Vec<JsonValue>) -> Vec<LinkMap> {
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
        .map(|(regex, target)| LinkMap { regex, target })
        .collect()
}

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
            targets: [
                "/var/www/archlinux/pkg".to_string()
            ].to_vec(),
            excludes: Vec::new(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    //noinspection DuplicatedCode
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
            targets: [
                "/var/www/archlinux/pkg".to_string()
            ].to_vec(),
            excludes: [
                "*zip".to_string()
            ].to_vec(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    //noinspection DuplicatedCode
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
            targets: [
                "/var/www/archlinux/pkg".to_string()
            ].to_vec(),
            excludes: [
                "*zip".to_string()
            ].to_vec(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    //noinspection DuplicatedCode
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
            targets: [
                "/var/www/archlinux/pkg".to_string()
            ].to_vec(),
            excludes: [
                "*zip".to_string()
            ].to_vec(),
            link_maps: Vec::new(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }

    //noinspection DuplicatedCode
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
            targets: [
                "/var/www/archlinux/pkg".to_string()
            ].to_vec(),
            excludes: [
                "*zip".to_string()
            ].to_vec(),
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
            targets: [
                "/var/www/archlinux/pkg".to_string()
            ].to_vec(),
            excludes: [
                "*zip".to_string()
            ].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "(.*)\\.pkg\\.tar\\.xz".to_string(),
                    target: "/var/www/archlinux/pkg".to_string(),
                }
            ].to_vec(),
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }
}
