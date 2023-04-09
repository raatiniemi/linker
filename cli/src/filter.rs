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

use rayon::prelude::*;

use crate::node::Node;

pub fn filter(sources: &[Node], targets: &[Node]) -> Vec<Node> {
    let source_path_for_targets = extract_source_path_for_targets(targets);
    sources.par_iter()
        .flat_map(|n| filter_linked_nodes(&n, &source_path_for_targets))
        .collect()
}

/// Extracts the source path from targets. As the targets should only be `Node::Link`
/// it's the only type that we'll handle.
fn extract_source_path_for_targets(targets: &[Node]) -> Vec<String> {
    targets.par_iter()
        .flat_map(|n| {
            match n {
                Node::Leaf(_) => Vec::new(),
                Node::Link(_, path) => vec![path.to_owned()],
                Node::Branch(_, _) => Vec::new(),
            }
        })
        .collect()
}

fn filter_linked_nodes(node: &Node, source_path_for_targets: &[String]) -> Vec<Node> {
    if is_linked(node, source_path_for_targets) {
        Vec::new()
    } else {
        match node {
            Node::Branch(_, _) => filter_branch(node, source_path_for_targets),
            _ => vec![node.to_owned()]
        }
    }
}

fn is_linked(node: &Node, source_path_for_targets: &[String]) -> bool {
    match node {
        Node::Leaf(path) => source_path_for_targets.contains(path),
        Node::Link(_, source) => source_path_for_targets.contains(source),
        Node::Branch(path, _) => source_path_for_targets.contains(path),
    }
}

fn filter_branch(node: &Node, source_path_for_targets: &[String]) -> Vec<Node> {
    return match node {
        Node::Branch(path, nodes) => {
            if !nodes.is_empty() {
                let remaining_nodes: Vec<Node> = nodes.par_iter()
                    .flat_map(|n| filter_linked_nodes(n, source_path_for_targets))
                    .collect();
                if !remaining_nodes.is_empty() {
                    vec![Node::Branch(path.to_owned(), remaining_nodes.to_owned())]
                } else {
                    Vec::new()
                }
            } else {
                vec![node.to_owned()]
            }
        }
        _ => Vec::new(),
    };
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use crate::filter::filter;
    use crate::node::Node;

    #[test]
    fn filter_without_sources_and_targets() {
        let sources: Vec<Node> = Vec::new();
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    // Leaf

    #[test]
    fn filter_with_source_leaf() {
        let sources: Vec<Node> = vec![
            Node::Leaf("/var/tmp/leaf".to_string()),
        ];
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = vec![
            Node::Leaf("/var/tmp/leaf".to_string()),
        ];

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_linked_source_leaf() {
        let sources: Vec<Node> = vec![
            Node::Leaf("/var/tmp/leaf".to_string()),
        ];
        let targets: Vec<Node> = vec![
            Node::Link(
                "/var/tmp/link".to_string(),
                "/var/tmp/leaf".to_string(),
            ),
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    // Link

    #[test]
    fn filter_with_source_link() {
        let sources: Vec<Node> = vec![
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ];
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = vec![
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ];

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    // Branch

    #[test]
    fn filter_with_source_branch() {
        let sources: Vec<Node> = vec![
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            ),
        ];
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = vec![
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            ),
        ];

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_nested_source_branch() {
        let sources: Vec<Node> = vec![
            Node::Branch(
                "/var/tmp/branch".to_string(),
                vec![
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ],
            ),
        ];
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = vec![
            Node::Branch(
                "/var/tmp/branch".to_string(),
                vec![
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ],
            ),
        ];

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_linked_source_branch() {
        let sources: Vec<Node> = vec![
            Node::Branch(
                "/var/tmp/branch".to_string(),
                vec![
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ],
            ),
        ];
        let targets: Vec<Node> = vec![
            Node::Link(
                "/var/tmp/link".to_string(),
                "/var/tmp/branch".to_string(),
            ),
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_nested_linked_source_branch() {
        let sources: Vec<Node> = vec![
            Node::Branch(
                "/var/tmp/branch".to_string(),
                vec![
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ],
            ),
        ];
        let targets: Vec<Node> = vec![
            Node::Link(
                "/var/tmp/link".to_string(),
                "/var/tmp/branch/child".to_string(),
            ),
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }
}
