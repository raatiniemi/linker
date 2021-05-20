use std::path::PathBuf;

use crate::configuration::{read_configuration, Configuration, LinkMap};
use crate::collect_nodes::collect_nodes;
use crate::link::{create_link_for_node, dry_run_create_link_for_node};
use crate::filter_source_nodes::filter_source_nodes;
use crate::filter_target_nodes::filter_target_nodes;
use crate::match_link_maps::match_link_maps;
use crate::filter::filter;
use crate::node::Node;
use crate::arguments::{collect_and_parse_arguments, Arguments};
use std::env;

mod configuration;
mod node;
mod collect_nodes;
mod filter_source_nodes;
mod filter_target_nodes;
mod filter;
mod match_link_maps;
mod link;
mod arguments;

fn main() {
    let arguments = collect_and_parse_arguments(env::args_os());
    let configuration = read_configuration(&arguments.configuration);

    run(&arguments, &configuration).iter()
        .for_each(|n| println!("{:?}", n));
}

fn run(arguments: &Arguments, configuration: &Configuration) -> Vec<Node> {
    let source_nodes = collect_and_filter_source_nodes(&configuration);
    let target_nodes = collect_and_filter_target_nodes(&configuration);
    let nodes = filter(&source_nodes, &target_nodes);

    link_nodes_matching_link_maps(
        &configuration.link_maps,
        &nodes,
        if arguments.dry_run {
            dry_run_create_link_for_node
        } else {
            create_link_for_node
        },
    )
}

fn collect_and_filter_source_nodes(configuration: &Configuration) -> Vec<Node> {
    let source_nodes = configuration.source.clone()
        .map(|v| PathBuf::from(v.as_str()))
        .map(|v| collect_nodes(&v))
        .expect(&format!("Unable to read path for sources from configuration"));

    filter_source_nodes(&source_nodes, &configuration.excludes)
}

fn collect_and_filter_target_nodes(configuration: &Configuration) -> Vec<Node> {
    let target_nodes = &configuration.targets.clone()
        .iter()
        .map(|v| PathBuf::from(v.as_str()))
        .flat_map(|v| collect_nodes(&v))
        .collect::<Vec<Node>>();

    filter_target_nodes(&target_nodes)
}

fn link_nodes_matching_link_maps(link_maps: &Vec<LinkMap>, sources: &Vec<Node>, create_link: fn(&Node) -> bool) -> Vec<Node> {
    sources.iter()
        .flat_map(|source| {
            match match_link_maps(&source, &link_maps) {
                Some(n) => create_node_link(&n, create_link),
                None => match source {
                    Node::Leaf(_) => [source.to_owned()].to_vec(),
                    Node::Link(_, _) => [source.to_owned()].to_vec(),
                    Node::Branch(path, nodes) => {
                        let remaining_nodes = link_nodes_matching_link_maps(link_maps, &nodes, create_link);
                        if !remaining_nodes.is_empty() {
                            [Node::Branch(path.to_owned(), remaining_nodes)].to_vec()
                        } else {
                            [].to_vec()
                        }
                    }
                }
            }
        })
        .collect::<Vec<Node>>()
}

fn create_node_link(node: &Node, create_link: fn(&Node) -> bool) -> Vec<Node> {
    if create_link(&node) {
        [].to_vec()
    } else {
        [node.to_owned()].to_vec()
    }
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use crate::arguments::Arguments;
    use crate::run;

    use std::fs;

    use tempdir::TempDir;
    use std::path::{PathBuf, Path};
    use std::fs::File;
    use crate::configuration::{Configuration, LinkMap};
    use crate::collect_nodes::collect_nodes;
    use crate::node::Node;

    fn create_temporary_directory() -> TempDir {
        TempDir::new("linker")
            .expect("Unable to create temporary directory")
    }

    fn as_string(path: &Path) -> String {
        return path.to_str()
            .expect(&format!("Unable to transform {:?} to string", path))
            .to_string();
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

    #[test]
    #[should_panic]
    fn run_with_empty_configuration_file() {
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let configuration = Configuration::default();

        run(&arguments, &configuration);
    }

    #[test]
    #[should_panic]
    fn run_without_source_path() {
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let configuration = Configuration {
            source: None,
            targets: [].to_vec(),
            excludes: [].to_vec(),
            link_maps: [].to_vec(),
        };

        run(&arguments, &configuration);
    }

    // Run

    #[test]
    fn run_with_leaf_source() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_file(&sources_path.join("name.pkg.tar.zst"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "(.*)\\.pkg\\.tar\\.zst".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [
            Node::Link(
                as_string(&targets_path.join("name.pkg.tar.zst")),
                as_string(&sources_path.join("name.pkg.tar.zst")),
            ),
        ].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn run_with_branch_source() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_directory_at_path(&sources_path.join("folder"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "folder".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [
            Node::Link(
                as_string(&targets_path.join("folder")),
                as_string(&sources_path.join("folder")),
            ),
        ].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn run_with_nested_sources() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_directory_at_path(&sources_path.join("folder"));
        create_directory_at_path(&sources_path.join("folder").join("subfolder"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "subfolder".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [
            Node::Link(
                as_string(&targets_path.join("subfolder")),
                as_string(&sources_path.join("folder").join("subfolder")),
            ),
        ].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn run_when_link_map_match_both_parent_and_child() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_directory_at_path(&sources_path.join("folder"));
        create_directory_at_path(&sources_path.join("folder").join("folder"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "folder".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [
            Node::Link(
                as_string(&targets_path.join("folder")),
                as_string(&sources_path.join("folder")),
            ),
        ].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    // Dry run

    #[test]
    fn dry_run_with_leaf_source() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: true,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_file(&sources_path.join("name.pkg.tar.zst"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "(.*)\\.pkg\\.tar\\.zst".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn dry_run_with_branch_source() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: true,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_directory_at_path(&sources_path.join("folder"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "folder".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn dry_run_with_nested_sources() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: true,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_directory_at_path(&sources_path.join("folder"));
        create_directory_at_path(&sources_path.join("folder").join("subfolder"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "subfolder".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn dry_run_when_link_map_match_both_parent_and_child() {
        let directory = create_temporary_directory();
        let path = PathBuf::from(directory.path());
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: true,
        };
        let sources_path = path.join("sources");
        let targets_path = path.join("targets");
        create_directory_at_path(&sources_path);
        create_directory_at_path(&sources_path.join("folder"));
        create_directory_at_path(&sources_path.join("folder").join("folder"));
        create_directory_at_path(&targets_path);
        let configuration = Configuration {
            source: Some(as_string(&sources_path)),
            targets: [
                as_string(&targets_path),
            ].to_vec(),
            excludes: [].to_vec(),
            link_maps: [
                LinkMap {
                    regex: "folder".to_string(),
                    target: as_string(&targets_path),
                }
            ].to_vec(),
        };
        let expected: Vec<Node> = [].to_vec();

        run(&arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }
}
