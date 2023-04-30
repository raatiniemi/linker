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

use std::error::Error;
use std::fs;
use std::os::unix::fs as unix_fs;
use std::path::PathBuf;
use std::str::FromStr;

use log::{debug, info, warn};

use crate::linker_error::LinkerError;
use crate::node::Node;

pub fn create_link_for_node_dry_run(node: &Node) -> bool {
    return match node {
        Node::Leaf(path) => {
            warn!("Unable to create link with leaf path {}", path);
            false
        }
        Node::Link(target, source) => {
            info!("Creating symbolic link {} -> {}", target, source);
            true
        }
        Node::Branch(path, _) => {
            warn!("Unable to create link with branch path {}", path);
            false
        }
    };
}

pub fn create_link_for_node(node: &Node) -> bool {
    return match node {
        Node::Leaf(path) => {
            warn!("Unable to create link with leaf path {}", path);
            false
        }
        Node::Link(target, source) => {
            match create_link(&target, &source) {
                Ok(_) => {
                    info!("Symbolic link {} -> {} was successfully created", target, source);
                    true
                }
                Err(e) => {
                    warn!("Unable to link {:?} -> {:?}: {}", target, source, e);
                    false
                }
            }
        }
        Node::Branch(path, _) => {
            warn!("Unable to create link with branch path {}", path);
            false
        }
    };
}

fn create_link(target: &str, source: &str) -> Result<(), Box<dyn Error>> {
    let target_path = PathBuf::from_str(target.clone())?;
    return match target_path.as_path().parent() {
        Some(parent_path) => {
            debug!("Check if path {:?} exists", parent_path);
            if !parent_path.exists() {
                debug!("Path {:?} do not exists, creating...", parent_path);
                fs::create_dir(parent_path)?;
                debug!("Path {:?} was successfully created", parent_path);
            }


            debug!("Creating symbolic link {} -> {}...", target, source);
            unix_fs::symlink(source.to_string(), target.to_string())
                .map_err(|e| Box::new(LinkerError::UnableToCreateSymlink(e)))?;
            Ok(())
        }
        None => Err(Box::new(LinkerError::UnableToGetParentDirectory(target_path))),
    };
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use std::fs;
    use std::fs::File;
    use std::path::{Path, PathBuf};

    use tempdir::TempDir;

    use crate::link::{create_link_for_node, create_link_for_node_dry_run};
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
    fn create_link_for_node_dry_run_with_leaf() {
        let node = Node::Leaf("/tmp/leaf".to_string());
        let expected = false;

        let actual = create_link_for_node_dry_run(&node);

        assert_eq!(expected, actual)
    }

    // - Link

    #[test]
    fn create_link_for_node_dry_run_with_link() {
        let node = Node::Link(
            "/tmp/link".to_string(),
            "/tmp/leaf".to_string(),
        );
        let expected = true;

        let actual = create_link_for_node_dry_run(&node);

        assert_eq!(expected, actual)
    }

    // - Branch

    #[test]
    fn create_link_for_node_dry_run_with_branch() {
        let node = Node::Branch(
            "/tmp/branch".to_string(),
            vec![],
        );
        let expected = false;

        let actual = create_link_for_node_dry_run(&node);

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
