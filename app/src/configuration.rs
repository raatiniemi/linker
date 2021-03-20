use json::JsonValue;

#[derive(Default, Eq, PartialEq, Clone, Debug)]
struct Configuration {
    source: Option<String>,
    targets: Vec<String>,
    excludes: Vec<String>,
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
        };

        let actual = parse_configuration(&configuration);

        assert_eq!(expected, actual)
    }
}
