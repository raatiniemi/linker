use std::path::{Path, PathBuf};
use std::fs;

#[derive(Eq, PartialEq, Ord, PartialOrd, Clone, Debug)]
pub enum Node {
    Branch(String, Vec<Node>),
    Leaf(String),
}

pub fn collect_nodes(path: &PathBuf) -> Vec<Node> {
    let directory = fs::read_dir(path);
    if directory.is_err() {
        eprintln!("Unable to read \"{:?}\" directory: {:?}", path.to_str().unwrap(), directory.err().unwrap());
        return Vec::new();
    }

    let mut entries: Vec<PathBuf> = Vec::new();
    for reader in directory {
        for result in reader {
            if result.is_err() {
                eprintln!("Unable to handle entry: {:?}", result.err().unwrap());
                continue;
            }

            let entry = result.unwrap();
            entries.push(entry.path());
        }
    }
    return collect_and_transform_nodes(entries);
}

fn collect_and_transform_nodes(entries: Vec<PathBuf>) -> Vec<Node> {
    let mut nodes = entries.iter()
        .map(|v| transform_to_node(v, v.to_str()))
        .filter(|v| v.is_some())
        .map(|v| v.unwrap())
        .collect::<Vec<Node>>();
    nodes.sort();

    return nodes;
}

fn transform_to_node(path: &PathBuf, s: Option<&str>) -> Option<Node> {
    match s {
        Some(v) => {
            let node = if path.is_dir() {
                Node::Branch(v.to_string(), collect_nodes(path))
            } else {
                Node::Leaf(v.to_string())
            };
            Some(node)
        }
        None => None
    }
}

#[cfg(test)]
mod tests {
    extern crate tempdir;

    use super::*;
    use tempdir::TempDir;
    use std::fs::File;

    fn create_temporary_directory() -> TempDir {
        TempDir::new("node")
            .expect("Unable to create temporary directory")
    }

    fn create_file_in_directory(directory: &PathBuf, basename: &str) -> String {
        File::create(directory.join(basename))
            .expect("Unable to create file with basename");

        directory.to_str()
            .map(|v| v.to_string())
            .map(|v| v + "/" + basename)
            .expect("Unable to build path for file")
    }

    fn create_directory_at_path(path: &Path) -> String {
        fs::create_dir(path)
            .expect("Unable to create directory");

        path.to_str()
            .map(|v| v.to_string())
            .expect("Unable to build path for directory")
    }

    #[test]
    fn collect_nodes_without_directory() {
        let path = PathBuf::from("/tmp/should-not-exists");
        let expected: Vec<Node> = Vec::new();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_empty_directory() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let expected: Vec<Node> = Vec::new();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_leaf() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let expected = [
            Node::Leaf(create_file_in_directory(&path, "leaf"))
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_leaves() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let expected = [
            Node::Leaf(create_file_in_directory(&path, "leaf-1")),
            Node::Leaf(create_file_in_directory(&path, "leaf-2"))
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branch() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let expected = [
            Node::Branch(
                create_directory_at_path(&path.join("branch")),
                Vec::new(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branches() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let expected = [
            Node::Branch(
                create_directory_at_path(&path.join("branch-1")),
                Vec::new(),
            ),
            Node::Branch(
                create_directory_at_path(&path.join("branch-2")),
                Vec::new(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branch_containing_leaf() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch = create_directory_at_path(&path.join("branch"));
        let expected = [
            Node::Branch(
                branch.clone(),
                [
                    Node::Leaf(create_file_in_directory(&path.join("branch"), "leaf"))
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branches_containing_leaf() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch_first = create_directory_at_path(&path.join("branch-1"));
        let branch_second = create_directory_at_path(&path.join("branch-2"));
        let expected = [
            Node::Branch(
                branch_first.clone(),
                [
                    Node::Leaf(create_file_in_directory(&path.join("branch-1"), "leaf"))
                ].to_vec(),
            ),
            Node::Branch(
                branch_second.clone(),
                [
                    Node::Leaf(create_file_in_directory(&path.join("branch-2"), "leaf"))
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branch_containing_leaves() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch = create_directory_at_path(&path.join("branch"));
        let expected = [
            Node::Branch(
                branch.clone(),
                [
                    Node::Leaf(create_file_in_directory(&path.join("branch"), "leaf-1")),
                    Node::Leaf(create_file_in_directory(&path.join("branch"), "leaf-2")),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branches_containing_leaves() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch_first = create_directory_at_path(&path.join("branch-1"));
        let branch_second = create_directory_at_path(&path.join("branch-2"));
        let expected = [
            Node::Branch(
                branch_first.clone(),
                [
                    Node::Leaf(create_file_in_directory(&path.join("branch-1"), "leaf-1")),
                    Node::Leaf(create_file_in_directory(&path.join("branch-1"), "leaf-2")),
                ].to_vec(),
            ),
            Node::Branch(
                branch_second.clone(),
                [
                    Node::Leaf(create_file_in_directory(&path.join("branch-2"), "leaf-1")),
                    Node::Leaf(create_file_in_directory(&path.join("branch-2"), "leaf-2")),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }
}
