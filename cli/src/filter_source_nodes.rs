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

pub fn filter_source_nodes(nodes: &[Node], excludes: &[String]) -> Vec<Node> {
    return recursive_exclusion_for_nodes(nodes, excludes);
}

fn recursive_exclusion_for_nodes(nodes: &[Node], excludes: &[String]) -> Vec<Node> {
    return nodes.par_iter()
        .map(|n| recursive_exclusion_for_node(n, excludes))
        .filter(|n| exclude(n, excludes))
        .collect();
}

fn recursive_exclusion_for_node(node: &Node, excludes: &[String]) -> Node {
    return match node {
        Node::Branch(path, nodes) => {
            Node::Branch(path.clone(), recursive_exclusion_for_nodes(nodes, &excludes))
        }
        Node::Leaf(_) => node.to_owned(),
        Node::Link(_, _) => node.to_owned()
    };
}

fn exclude(node: &Node, excludes: &[String]) -> bool {
    let value = extract_basename_from_node(node);
    return match value {
        Some(basename) => !excludes.contains(&basename.to_lowercase()),
        None => {
            eprintln!("Unable to extract basename from {:?}", node);
            true
        }
    };
}

fn extract_basename_from_node(node: &Node) -> Option<String> {
    let path = match node {
        Node::Branch(path, _) => path,
        Node::Leaf(path) => path,
        Node::Link(path, _) => path,
    };

    return path.split("/")
        .last()
        .map(|v| v.to_string())
        .to_owned();
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use crate::filter_source_nodes::filter_source_nodes;
    use crate::node::Node;

    #[test]
    fn filter_source_nodes_without_nodes() {
        let nodes: Vec<Node> = Vec::new();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_leaf() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string())
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string())
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_leaves() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf-1".to_string()),
            Node::Leaf("/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf-1".to_string()),
            Node::Leaf("/var/tmp/leaf-2".to_string()),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_leaf() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf-1".to_string()),
            Node::Leaf("/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "leaf-1".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf-2".to_string())
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_leaf() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/LEAF-1".to_string()),
            Node::Leaf("/var/tmp/LEAF-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "leaf-1".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Leaf("/var/tmp/LEAF-2".to_string())
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_leaves() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf-1".to_string()),
            Node::Leaf("/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "leaf-1".to_string(),
            "leaf-2".to_string(),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_leaves() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/LEAF-1".to_string()),
            Node::Leaf("/var/tmp/LEAF-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "leaf-1".to_string(),
            "leaf-2".to_string(),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_link() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_links() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/link-1".to_string(), "/var/tmp/leaf-1".to_string()),
            Node::Link("/var/tmp/link-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/link-1".to_string(), "/var/tmp/leaf-1".to_string()),
            Node::Link("/var/tmp/link-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_link() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/link-1".to_string(), "/var/tmp/leaf-1".to_string()),
            Node::Link("/var/tmp/link-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "link-1".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/link-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_link() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/LINK-1".to_string(), "/var/tmp/leaf-1".to_string()),
            Node::Link("/var/tmp/LINK-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "link-1".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/LINK-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_links() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/link-1".to_string(), "/var/tmp/leaf-1".to_string()),
            Node::Link("/var/tmp/link-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "link-1".to_string(),
            "link-2".to_string(),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_links() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/LINK-1".to_string(), "/var/tmp/leaf-1".to_string()),
            Node::Link("/var/tmp/LINK-2".to_string(), "/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let excludes: Vec<String> = [
            "link-1".to_string(),
            "link-2".to_string(),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_without_empty_branch() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            )
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            )
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_branch_and_child() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/leaf".to_string())
                ].to_vec(),
            )
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/leaf".to_string())
                ].to_vec(),
            )
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_branch_and_children() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/leaf-1".to_string()),
                    Node::Leaf("/var/tmp/branch/leaf-2".to_string()),
                ].to_vec(),
            )
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/leaf-1".to_string()),
                    Node::Leaf("/var/tmp/branch/leaf-2".to_string()),
                ].to_vec(),
            )
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_empty_branches() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                Vec::new(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                Vec::new(),
            ),
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                Vec::new(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                Vec::new(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_branches_and_child() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-1/leaf-1".to_string())
                ].to_vec(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-2/leaf-2".to_string())
                ].to_vec(),
            ),
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-1/leaf-1".to_string())
                ].to_vec(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-2/leaf-2".to_string())
                ].to_vec(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_with_branches_and_children() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-1/leaf-1".to_string()),
                    Node::Leaf("/var/tmp/branch-1/leaf-2".to_string()),
                ].to_vec(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-2/leaf-3".to_string()),
                    Node::Leaf("/var/tmp/branch-2/leaf-4".to_string()),
                ].to_vec(),
            ),
        ].to_vec();
        let excludes: Vec<String> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-1/leaf-1".to_string()),
                    Node::Leaf("/var/tmp/branch-1/leaf-2".to_string()),
                ].to_vec(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                [
                    Node::Leaf("/var/tmp/branch-2/leaf-3".to_string()),
                    Node::Leaf("/var/tmp/branch-2/leaf-4".to_string()),
                ].to_vec(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_branch() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                Vec::new(),
            ),
            Node::Branch(
                "/var/tmp/branch-2".to_string(),
                Vec::new(),
            ),
        ].to_vec();
        let excludes: Vec<String> = [
            "branch-2".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch-1".to_string(),
                Vec::new(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_branch() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/BRANCH-1".to_string(),
                Vec::new(),
            ),
            Node::Branch(
                "/var/tmp/BRANCH-2".to_string(),
                Vec::new(),
            ),
        ].to_vec();
        let excludes: Vec<String> = [
            "branch-2".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/BRANCH-1".to_string(),
                Vec::new(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_branch_with_child() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/leaf".to_string()),
                ].to_vec(),
            ),
        ].to_vec();
        let excludes: Vec<String> = [
            "branch".to_string()
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_branch_with_child() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/BRANCH".to_string(),
                [
                    Node::Leaf("/var/tmp/BRANCH/leaf".to_string()),
                ].to_vec(),
            ),
        ].to_vec();
        let excludes: Vec<String> = [
            "branch".to_string()
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_child_in_branch() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/leaf".to_string()),
                ].to_vec(),
            ),
        ].to_vec();
        let excludes: Vec<String> = [
            "leaf".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_source_nodes_when_excluding_uppercase_child_in_branch() {
        let nodes: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Leaf("/var/tmp/branch/LEAF".to_string()),
                ].to_vec(),
            ),
        ].to_vec();
        let excludes: Vec<String> = [
            "leaf".to_string()
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            ),
        ].to_vec();

        let actual = filter_source_nodes(&nodes, &excludes);

        assert_eq!(expected, actual)
    }
}