use crate::node::Node;

pub fn filter_target_nodes(nodes: &[Node]) -> Vec<Node> {
    return nodes.iter()
        .flat_map(|n| filter_links(n))
        .collect();
}

fn filter_links(node: &Node) -> Vec<Node> {
    return match node {
        Node::Branch(_, children) => children.iter()
            .flat_map(|n| filter_links(n))
            .collect(),
        Node::Leaf(_) => Vec::new(),
        Node::Link(_, _) => [
            node.to_owned()
        ].to_vec(),
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
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string())
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_leaves() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf-1".to_string()),
            Node::Leaf("/var/tmp/leaf-2".to_string()),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_link() {
        let nodes: Vec<Node> = [
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_nested_link() {
        let nodes: Vec<Node> = [
            Node::Leaf("/var/tmp/leaf".to_string()),
            Node::Branch(
                "/var/tmp/branch".to_string(),
                [
                    Node::Link("/var/tmp/branch/link".to_string(), "/var/tmp/leaf".to_string())
                ].to_vec(),
            ),
        ].to_vec();
        let expected: Vec<Node> = [
            Node::Link("/var/tmp/branch/link".to_string(), "/var/tmp/leaf".to_string()),
        ].to_vec();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_branch() {
        let nodes: Vec<Node> = [
            Node::Branch("/var/tmp/branch".to_string(), Vec::new())
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }

    #[test]
    fn filter_target_nodes_with_branches() {
        let nodes: Vec<Node> = [
            Node::Branch("/var/tmp/branch-1".to_string(), Vec::new()),
            Node::Branch("/var/tmp/branch-2".to_string(), Vec::new()),
        ].to_vec();
        let expected: Vec<Node> = Vec::new();

        let actual = filter_target_nodes(&nodes);

        assert_eq!(expected, actual)
    }
}
