#[derive(Eq, PartialEq, Ord, PartialOrd, Clone, Debug)]
pub enum Node {
    Leaf(String),
    Link(String, String),
    Branch(String, Vec<Node>),
}

pub fn filter(sources: &Vec<Node>, targets: &Vec<Node>) -> Vec<Node> {
    let source_path_for_targets = extract_source_path_for_targets(targets);
    return sources.iter()
        .flat_map(|n| filter_linked_nodes(&n, &source_path_for_targets))
        .collect();
}

/// Extracts the source path from targets. As the targets should only be `Node::Link`
/// it's the only type that we'll handle.
fn extract_source_path_for_targets(targets: &Vec<Node>) -> Vec<String> {
    targets.iter()
        .flat_map(|n| {
            match n {
                Node::Leaf(_) => Vec::new(),
                Node::Link(_, path) => [path.to_owned()].to_vec(),
                Node::Branch(_, _) => Vec::new(),
            }
        })
        .collect()
}

fn filter_linked_nodes(node: &Node, source_path_for_targets: &Vec<String>) -> Vec<Node> {
    if is_linked(node, source_path_for_targets) {
        Vec::new()
    } else {
        match node {
            Node::Branch(_, _) => filter_branch(node, source_path_for_targets),
            _ => [node.to_owned()].to_vec()
        }
    }
}

fn is_linked(node: &Node, source_path_for_targets: &Vec<String>) -> bool {
    match node {
        Node::Leaf(path) => source_path_for_targets.contains(path),
        Node::Link(_, source) => source_path_for_targets.contains(source),
        Node::Branch(path, _) => source_path_for_targets.contains(path),
    }
}

fn filter_branch(node: &Node, source_path_for_targets: &Vec<String>) -> Vec<Node> {
    return match node {
        Node::Branch(path, nodes) => {
            if !nodes.is_empty() {
                let remaining_nodes: Vec<Node> = nodes.iter()
                    .flat_map(|n| filter_linked_nodes(n, source_path_for_targets))
                    .collect();
                if !remaining_nodes.is_empty() {
                    [Node::Branch(path.to_owned(), remaining_nodes.to_owned())].to_vec()
                } else {
                    Vec::new()
                }
            } else {
                [node.to_owned()].to_vec()
            }
        }
        _ => Vec::new(),
    };
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use crate::node::{Node, filter};

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
        let sources: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string()),
        ].to_vec();
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string()),
        ].to_vec();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_linked_source_leaf() {
        let sources: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string()),
        ].to_vec();
        let targets: Vec<Node> = [
            Node::Link(
                "/var/tmp/link".to_string(),
                "/var/tmp/leaf".to_string(),
            ),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    // Link

    #[test]
    fn filter_with_source_link() {
        let sources: Vec<Node> = [
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    // Branch

    #[test]
    fn filter_with_source_branch() {
        let sources: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            ),
        ].to_vec();
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                Vec::new(),
            ),
        ].to_vec();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_nested_source_branch() {
        let sources: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ].to_vec(),
            ),
        ].to_vec();
        let targets: Vec<Node> = Vec::new();
        let expected: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ].to_vec(),
            ),
        ].to_vec();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_linked_source_branch() {
        let sources: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ].to_vec(),
            ),
        ].to_vec();
        let targets: Vec<Node> = [
            Node::Link(
                "/var/tmp/link".to_string(),
                "/var/tmp/branch".to_string(),
            ),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_with_nested_linked_source_branch() {
        let sources: Vec<Node> = [
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Branch(
                        "/var/tmp/branch/child".to_string(),
                        Vec::new(),
                    ),
                ].to_vec(),
            ),
        ].to_vec();
        let targets: Vec<Node> = [
            Node::Link(
                "/var/tmp/link".to_string(),
                "/var/tmp/branch/child".to_string(),
            ),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter(&sources, &targets);

        assert_eq!(expected, actual)
    }
}
