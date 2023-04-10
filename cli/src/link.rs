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

use std::fs;
use std::os::unix::fs as unix_fs;
use std::path::{Path, PathBuf};
use std::str::FromStr;
use log::{error, info, warn};

use crate::node::Node;

pub fn dry_run_create_link_for_node(node: &Node) -> bool {
    return match node {
        Node::Leaf(_) => false,
        Node::Link(target, source) => {
            info!("Creating symbolic link {} -> {}", target, source);
            true
        }
        Node::Branch(_, _) => false,
    };
}

pub fn create_link_for_node(node: &Node) -> bool {
    return match node {
        Node::Leaf(_) => false,
        Node::Link(target, source) => create_link(&target, &source),
        Node::Branch(_, _) => false,
    };
}

fn create_link(target: &str, source: &str) -> bool {
    if !create_parent_directory(target) {
        return false;
    }

    let result = unix_fs::symlink(source.to_string(), target.to_string());
    return match result {
        Ok(_) => true,
        Err(err) => {
            warn!("Unable to create link {:?} -> {:?}: {:?}", target, source, err);
            false
        }
    };
}

fn create_parent_directory(target: &str) -> bool {
    let result = PathBuf::from_str(target);
    match result {
        Ok(path) => create_directory(path.as_path().parent()),
        Err(error) => {
            warn!("Unable to create path for {:?}: {:?}", target, error);
            false
        }
    }
}

fn create_directory(value: Option<&Path>) -> bool {
    match value {
        Some(path) => {
            if !path.exists() {
                let result = fs::create_dir(path);
                match result.err() {
                    Some(error) => {
                        error!("Unable to create directory for {:?}: {:?}", path, error);
                        false
                    }
                    None => true
                }
            } else {
                true
            }
        }
        None => false
    }
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use std::fs;
    use std::fs::File;
    use std::path::{Path, PathBuf};

    use tempdir::TempDir;

    use crate::link::{create_link_for_node, dry_run_create_link_for_node};
    use crate::node::Node;

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

    fn create_directory_at_path(path: &Path) -> String {
        fs::create_dir(path)
            .expect("Unable to create directory");

        path.to_str()
            .map(|v| v.to_string())
            .expect("Unable to build path for directory")
    }

    // Create link (dry run)
    // - Leaf

    #[test]
    fn dry_run_create_link_for_node_with_leaf() {
        let node = Node::Leaf("/tmp/leaf".to_string());
        let expected = false;

        let actual = dry_run_create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    // - Link

    #[test]
    fn dry_run_create_link_for_node_with_link() {
        let node = Node::Link(
            "/tmp/link".to_string(),
            "/tmp/leaf".to_string(),
        );
        let expected = true;

        let actual = dry_run_create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    // - Branch

    #[test]
    fn dry_run_create_link_for_node_with_branch() {
        let node = Node::Branch(
            "/tmp/branch".to_string(),
            vec![],
        );
        let expected = false;

        let actual = dry_run_create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    // Create link
    // - Leaf

    #[test]
    fn create_link_for_node_with_leaf() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let node = Node::Leaf(create_file(&path.join("leaf")));
        let expected = false;

        let actual = create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    // - Link

    #[test]
    fn create_link_for_node_with_link() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let node: Node = Node::Link(
            path.join("link").to_str()
                .expect("Unable to build link path")
                .to_string(),
            create_file(&path.join("leaf")),
        );
        let expected = true;

        let actual = create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    #[test]
    fn create_link_for_node_with_link_in_branch() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        create_directory_at_path(&path.join("branch"));
        let node: Node = Node::Link(
            path.join("branch")
                .join("link").to_str()
                .expect("Unable to build link path")
                .to_string(),
            create_file(&path.join("leaf")),
        );
        let expected = true;

        let actual = create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    #[test]
    fn create_link_for_node_with_link_in_without_existing_branch() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let node: Node = Node::Link(
            path.join("branch")
                .join("link").to_str()
                .expect("Unable to build link path")
                .to_string(),
            create_file(&path.join("leaf")),
        );
        let expected = true;

        let actual = create_link_for_node(&node);

        assert_eq!(expected, actual)
    }

    // - Branch

    #[test]
    fn create_link_for_node_with_branch() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let node: Node = Node::Branch(
            create_directory_at_path(&path.join("branch")),
            Vec::new(),
        );
        let expected = false;

        let actual = create_link_for_node(&node);

        assert_eq!(expected, actual)
    }
}
