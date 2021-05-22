use std::fs;
use std::path::{PathBuf, Path};
use crate::node::Node;
use rayon::prelude::*;

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
    let mut nodes = entries.par_iter()
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
            let file_type = fs::symlink_metadata(path)
                .expect(&format!("Unable to read metadata on {:?}", path.to_str()))
                .file_type();

            let node = if file_type.is_symlink() {
                Node::Link(v.to_string(), normalize_link_source(v))
            } else if file_type.is_dir() {
                Node::Branch(v.to_string(), collect_nodes(path))
            } else {
                Node::Leaf(v.to_string())
            };
            Some(node)
        }
        None => None
    }
}

fn normalize_link_source(v: &str) -> String {
    let source = fs::read_link(v)
        .map(|s| s.to_str()
            .expect(&format!("Unable to transform link to str: {:?}", v))
            .to_owned())
        .expect(&format!("Unable to read link: {:?}", v));

    let canonical_path = fs::canonicalize(
        Path::new(v)
            .parent()
            .map(|p| p.to_owned())
            .map(|p| p.join(source))
            .expect(&format!("Unable to build parent path for: {:?}", v))
    );

    canonical_path.expect(&format!("Unable to read canonical path: {:?}", v))
        .to_str()
        .expect(&format!("Unable to transform canonical path to str: {:?}", v))
        .to_string()
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    extern crate tempdir;

    use std::fs::File;
    use std::path::Path;
    use std::os::unix::fs as unix_fs;

    use tempdir::TempDir;

    use super::*;

    fn create_temporary_directory() -> TempDir {
        TempDir::new("node")
            .expect("Unable to create temporary directory")
    }

    fn create_file(path: &PathBuf) -> String {
        File::create(path)
            .expect(&format!("Unable to create file at: {:?}", path.to_str()));

        path.to_str()
            .map(|v| v.to_string())
            .expect(&format!("Unable to build path for file at: {:?}", path.to_str()))
    }

    fn create_link(original: &str, link: &PathBuf) -> String {
        let path = link.to_str()
            .expect(&format!("Unable to build path for link at: {:?}", link.to_str()));

        unix_fs::symlink(original.to_string(), path.to_string())
            .expect(&format!("Unable to create symlink for {:?} -> {:?}", link, original));
        return path.to_string();
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
            Node::Leaf(create_file(&path.join("leaf")))
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_leaves() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let expected = [
            Node::Leaf(create_file(&path.join("leaf-1"))),
            Node::Leaf(create_file(&path.join("leaf-2")))
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_link() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let original = create_file(&path.join("original"));
        let expected = [
            Node::Leaf(original.clone()),
            Node::Link(create_link(&original, &path.join("link")), original),
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_links() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let first_leaf = create_file(&path.join("leaf-1"));
        let second_leaf = create_file(&path.join("leaf-2"));
        let expected = [
            Node::Leaf(first_leaf.clone()),
            Node::Leaf(second_leaf.clone()),
            Node::Link(create_link(&first_leaf, &path.join("link-1")), first_leaf),
            Node::Link(create_link(&second_leaf, &path.join("link-2")), second_leaf),
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
                    Node::Leaf(create_file(&path.join("branch").join("leaf")))
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
                    Node::Leaf(create_file(&path.join("branch-1").join("leaf")))
                ].to_vec(),
            ),
            Node::Branch(
                branch_second.clone(),
                [
                    Node::Leaf(create_file(&path.join("branch-2").join("leaf")))
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branch_containing_link() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch = create_directory_at_path(&path.join("branch"));
        let leaf = create_file(&path.join("branch").join("leaf"));
        let link = create_link(&leaf, &path.join("branch").join("link"));
        let expected = [
            Node::Branch(
                branch.clone(),
                [
                    Node::Leaf(leaf.clone()),
                    Node::Link(link, leaf),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branches_containing_link() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch_first = create_directory_at_path(&path.join("branch-1"));
        let leaf_first = create_file(&path.join("branch-1").join("leaf"));
        let link_first = create_link(&leaf_first, &path.join("branch-1").join("link"));
        let branch_second = create_directory_at_path(&path.join("branch-2"));
        let leaf_second = create_file(&path.join("branch-2").join("leaf"));
        let link_second = create_link(&leaf_second, &path.join("branch-2").join("link"));
        let expected = [
            Node::Branch(
                branch_first.clone(),
                [
                    Node::Leaf(leaf_first.clone()),
                    Node::Link(link_first, leaf_first),
                ].to_vec(),
            ),
            Node::Branch(
                branch_second.clone(),
                [
                    Node::Leaf(leaf_second.clone()),
                    Node::Link(link_second, leaf_second),
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
                    Node::Leaf(create_file(&path.join("branch").join("leaf-1"))),
                    Node::Leaf(create_file(&path.join("branch").join("leaf-2"))),
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
                    Node::Leaf(create_file(&path.join("branch-1").join("leaf-1"))),
                    Node::Leaf(create_file(&path.join("branch-1").join("leaf-2"))),
                ].to_vec(),
            ),
            Node::Branch(
                branch_second.clone(),
                [
                    Node::Leaf(create_file(&path.join("branch-2").join("leaf-1"))),
                    Node::Leaf(create_file(&path.join("branch-2").join("leaf-2"))),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branch_containing_links() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch = create_directory_at_path(&path.join("branch"));
        let leaf_first = create_file(&path.join("branch").join("leaf-1"));
        let link_first = create_link(&leaf_first, &path.join("branch").join("link-1"));
        let leaf_second = create_file(&path.join("branch").join("leaf-2"));
        let link_second = create_link(&leaf_second, &path.join("branch").join("link-2"));
        let expected = [
            Node::Branch(
                branch.clone(),
                [
                    Node::Leaf(leaf_first.clone()),
                    Node::Leaf(leaf_second.clone()),
                    Node::Link(link_first, leaf_first),
                    Node::Link(link_second, leaf_second),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branches_containing_links() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let branch_first = create_directory_at_path(&path.join("branch-1"));
        let leaf_first = create_file(&path.join("branch-1").join("leaf-1"));
        let link_first = create_link(&leaf_first, &path.join("branch-1").join("link-1"));
        let leaf_second = create_file(&path.join("branch-1").join("leaf-2"));
        let link_second = create_link(&leaf_second, &path.join("branch-1").join("link-2"));
        let branch_second = create_directory_at_path(&path.join("branch-2"));
        let leaf_third = create_file(&path.join("branch-2").join("leaf-3"));
        let link_third = create_link(&leaf_third, &path.join("branch-2").join("link-3"));
        let leaf_fourth = create_file(&path.join("branch-2").join("leaf-4"));
        let link_fourth = create_link(&leaf_fourth, &path.join("branch-2").join("link-4"));
        let expected = [
            Node::Branch(
                branch_first.clone(),
                [
                    Node::Leaf(leaf_first.clone()),
                    Node::Leaf(leaf_second.clone()),
                    Node::Link(link_first, leaf_first),
                    Node::Link(link_second, leaf_second),
                ].to_vec(),
            ),
            Node::Branch(
                branch_second.clone(),
                [
                    Node::Leaf(leaf_third.clone()),
                    Node::Leaf(leaf_fourth.clone()),
                    Node::Link(link_third, leaf_third),
                    Node::Link(link_fourth, leaf_fourth),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }

    #[test]
    fn collect_nodes_with_branches_containing_relative_links() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let source_branch = create_directory_at_path(&path.join("sources"));
        let leaf_first = create_file(&path.join("sources").join("leaf-1"));
        let leaf_second = create_file(&path.join("sources").join("leaf-2"));
        let target_branch = create_directory_at_path(&path.join("targets"));
        let link_first = create_link(
            "../sources/leaf-1",
            &path.join("targets").join("link-1"),
        );
        let link_second = create_link(
            "../sources/leaf-2",
            &path.join("targets").join("link-2"),
        );
        let expected = [
            Node::Branch(
                source_branch.clone(),
                [
                    Node::Leaf(leaf_first.clone()),
                    Node::Leaf(leaf_second.clone()),
                ].to_vec(),
            ),
            Node::Branch(
                target_branch.clone(),
                [
                    Node::Link(link_first, leaf_first.to_owned()),
                    Node::Link(link_second, leaf_second.to_owned()),
                ].to_vec(),
            )
        ].to_vec();

        let actual = collect_nodes(&path);

        assert_eq!(expected, actual)
    }
}
