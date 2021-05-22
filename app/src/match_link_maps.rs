use std::path::MAIN_SEPARATOR;
use std::str::FromStr;

use regex::Regex;

use crate::configuration::LinkMap;
use crate::node::Node;

pub fn match_link_maps(node: &Node, link_maps: &[LinkMap]) -> Option<Node> {
    return match node {
        Node::Leaf(path) => find_link_map_match(path, link_maps),
        Node::Link(_, _) => None,
        Node::Branch(path, _) => find_link_map_match(path, link_maps),
    };
}

fn find_link_map_match(path: &String, link_maps: &[LinkMap]) -> Option<Node> {
    if path.is_empty() {
        return None;
    }

    match path.rsplit(MAIN_SEPARATOR).next() {
        Some(basename) => {
            let value: Option<&LinkMap> = link_maps.iter()
                .filter(|v| is_link_map_match(basename, v))
                .last();

            match value {
                Some(link_map) => Some(
                    Node::Link(
                        [link_map.target.to_string(), basename.to_string()]
                            .join(&MAIN_SEPARATOR.to_string()),
                        path.to_string(),
                    )
                ),
                None => None,
            }
        }
        None => None,
    }
}

fn is_link_map_match(basename: &str, link_map: &&LinkMap) -> bool {
    let regex = Regex::from_str(link_map.regex.as_str())
        .expect(&format!("Unable to build regex: {:?}", link_map.regex));

    regex.is_match(basename)
}

#[cfg(test)]
mod tests {
    use crate::configuration::LinkMap;
    use crate::match_link_maps::match_link_maps;
    use crate::node::Node;

    // Leaf

    #[test]
    fn match_leaf_with_empty_path() {
        let node: Node = Node::Leaf(
            "".to_string()
        );
        let link_maps: Vec<LinkMap> = Vec::new();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_leaf_without_link_maps() {
        let node: Node = Node::Leaf(
            "/var/tmp/sources/leaf".to_string()
        );
        let link_maps: Vec<LinkMap> = Vec::new();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_leaf_without_matching_link_map() {
        let node: Node = Node::Leaf(
            "/var/tmp/sources/leaf".to_string()
        );
        let link_maps: Vec<LinkMap> = [
            LinkMap {
                regex: "regex".to_string(),
                target: "/var/tmp/targets".to_string(),
            }
        ].to_vec();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_leaf_with_matching_link_map() {
        let node: Node = Node::Leaf(
            "/var/tmp/sources/leaf".to_string()
        );
        let link_maps: Vec<LinkMap> = [
            LinkMap {
                regex: "leaf".to_string(),
                target: "/var/tmp/targets".to_string(),
            }
        ].to_vec();
        let expected: Option<Node> = Some(
            Node::Link(
                "/var/tmp/targets/leaf".to_string(),
                "/var/tmp/sources/leaf".to_string(),
            )
        );

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_leaf_with_matching_link_map_using_regex() {
        let node: Node = Node::Leaf(
            "/var/tmp/sources/leaf-1".to_string()
        );
        let link_maps: Vec<LinkMap> = [
            LinkMap {
                regex: "leaf[-](\\d{1})".to_string(),
                target: "/var/tmp/targets".to_string(),
            }
        ].to_vec();
        let expected: Option<Node> = Some(
            Node::Link(
                "/var/tmp/targets/leaf-1".to_string(),
                "/var/tmp/sources/leaf-1".to_string(),
            )
        );

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    // Link

    #[test]
    fn match_link_with_empty_path() {
        let node: Node = Node::Link(
            "".to_string(),
            "".to_string(),
        );
        let link_maps: Vec<LinkMap> = Vec::new();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_link_without_link_maps() {
        let node: Node = Node::Link(
            "/var/tmp/sources/link".to_string(),
            "/var/tmp/sources/leaf".to_string(),
        );
        let link_maps: Vec<LinkMap> = Vec::new();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    // Branch

    #[test]
    fn match_branch_with_empty_path() {
        let node: Node = Node::Branch(
            "".to_string(),
            Vec::new(),
        );
        let link_maps: Vec<LinkMap> = Vec::new();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_branch_without_link_maps() {
        let node: Node = Node::Branch(
            "/var/tmp/sources/branch".to_string(),
            Vec::new(),
        );
        let link_maps: Vec<LinkMap> = Vec::new();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_branch_without_matching_link_map() {
        let node: Node = Node::Branch(
            "/var/tmp/sources/branch".to_string(),
            Vec::new(),
        );
        let link_maps: Vec<LinkMap> = [
            LinkMap {
                regex: "regex".to_string(),
                target: "/var/tmp/targets".to_string(),
            }
        ].to_vec();
        let expected: Option<Node> = None;

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }

    #[test]
    fn match_branch_with_matching_link_map() {
        let node: Node = Node::Branch(
            "/var/tmp/sources/branch".to_string(),
            Vec::new(),
        );
        let link_maps: Vec<LinkMap> = [
            LinkMap {
                regex: "branch".to_string(),
                target: "/var/tmp/targets".to_string(),
            }
        ].to_vec();
        let expected: Option<Node> = Some(
            Node::Link(
                "/var/tmp/targets/branch".to_string(),
                "/var/tmp/sources/branch".to_string(),
            )
        );

        let actual = match_link_maps(&node, &link_maps);

        assert_eq!(expected, actual)
    }
}
