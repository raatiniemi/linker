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

use crate::node::Node;

pub fn filter_target_nodes(nodes: &[Node]) -> Vec<Node> {
    return nodes.iter()
        .flat_map(|n| filter_links(n))
        .collect();
}

fn filter_links(node: &Node) -> Vec<Node> {
    return match node {
        Node::Branch(_, children) => {
            children.iter()
                .flat_map(|n| filter_links(n))
                .collect()
        }
        Node::Leaf(_) => Vec::new(),
        Node::Link(_, _) => vec![
            node.to_owned()
        ],
    };
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use crate::filter_target_nodes::filter_target_nodes;
    use crate::node::Node;

    #[test]
    fn filter_target_nodes_without_nodes() {
        let nodes: Vec<Node> = Vec::new();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_leaf() {
        let nodes: Vec<Node> = vec![
            Node::Leaf("/var/tmp/leaf".to_string())
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_leaves() {
        let nodes: Vec<Node> = vec![
            Node::Leaf("/var/tmp/leaf-1".to_string()),
            Node::Leaf("/var/tmp/leaf-2".to_string()),
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_link() {
        let nodes: Vec<Node> = vec![
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ];
        let expected: Vec<Node> = vec![
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ];

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_nested_link() {
        let nodes: Vec<Node> = vec![
            Node::Leaf("/var/tmp/leaf".to_string()),
            Node::Branch(
                "/var/tmp/branch".to_string(),
                vec![
                    Node::Link("/var/tmp/branch/link".to_string(), "/var/tmp/leaf".to_string())
                ],
            ),
        ];
        let expected: Vec<Node> = vec![
            Node::Link("/var/tmp/branch/link".to_string(), "/var/tmp/leaf".to_string()),
        ];

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_branch() {
        let nodes: Vec<Node> = vec![
            Node::Branch("/var/tmp/branch".to_string(), Vec::new())
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_branches() {
        let nodes: Vec<Node> = vec![
            Node::Branch("/var/tmp/branch-1".to_string(), Vec::new()),
            Node::Branch("/var/tmp/branch-2".to_string(), Vec::new()),
        ];
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }
}
