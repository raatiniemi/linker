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

use std::env;
use std::path::PathBuf;

use opentelemetry::global;
use opentelemetry::sdk::trace;
use opentelemetry::trace::Tracer;
use rayon::prelude::*;

use crate::arguments::{Arguments, collect_and_parse_arguments};
use crate::collect_nodes::collect_nodes;
use crate::configuration::{Configuration, LinkMap, read_configuration};
use crate::filter::filter;
use crate::filter_source_nodes::filter_source_nodes;
use crate::filter_target_nodes::filter_target_nodes;
use crate::link::{create_link_for_node, dry_run_create_link_for_node};
use crate::match_link_maps::match_link_maps;
use crate::node::Node;

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
    global::set_text_map_propagator(opentelemetry_jaeger::Propagator::new());
    let tracer = opentelemetry_jaeger::new_pipeline()
        .with_service_name("linker")
        .install_simple()
        .expect("Unable to install open telemetry pipeline");

    tracer.in_span("execution", |_| {
        let arguments = collect_and_parse_arguments(&tracer, env::args_os());
        let configuration = read_configuration(&tracer, &arguments.configuration);

        let mut remaining_nodes = run(&tracer, &arguments, &configuration);
        remaining_nodes.sort();
        remaining_nodes.iter()
            .for_each(|v| print(v.to_owned()));
    });

    global::shutdown_tracer_provider();
}

fn run(tracer: &trace::Tracer, arguments: &Arguments, configuration: &Configuration) -> Vec<Node> {
    let source_nodes = collect_and_filter_source_nodes(&tracer, &configuration);
    let target_nodes = collect_and_filter_target_nodes(&tracer, &configuration);
    let nodes = filter(&tracer, &source_nodes, &target_nodes);

    tracer.in_span("link_nodes_matching_link_maps", |_| {
        link_nodes_matching_configuration(
            &nodes,
            &configuration.link_maps,
            if arguments.dry_run {
                dry_run_create_link_for_node
            } else {
                create_link_for_node
            },
        )
    })
}

fn collect_and_filter_source_nodes(tracer: &trace::Tracer, configuration: &Configuration) -> Vec<Node> {
    tracer.in_span("collect_and_filter_source_nodes", |_| {
        let source_nodes = configuration.source.to_owned()
            .map(|v| PathBuf::from(v.as_str()))
            .map(|v| collect_nodes(&v))
            .expect(&format!("Unable to read path for sources from configuration"));

        filter_source_nodes(&source_nodes, &configuration.excludes)
    })
}

fn collect_and_filter_target_nodes(tracer: &trace::Tracer, configuration: &Configuration) -> Vec<Node> {
    tracer.in_span("collect_and_filter_target_nodes", |_| {
        let target_nodes = &configuration.targets.to_owned()
            .par_iter()
            .map(|v| PathBuf::from(v.as_str()))
            .flat_map(|v| collect_nodes(&v))
            .collect::<Vec<Node>>();

        filter_target_nodes(&target_nodes)
    })
}

fn link_nodes_matching_configuration(nodes: &[Node], link_maps: &[LinkMap], create_link: fn(&Node) -> bool) -> Vec<Node> {
    nodes.par_iter()
        .flat_map(|v| link_node_matching_configuration(&v, &link_maps, create_link))
        .collect::<Vec<Node>>()
}

fn link_node_matching_configuration(
    nodes: &Node,
    link_maps: &&[LinkMap],
    create_link: fn(&Node) -> bool,
) -> Vec<Node> {
    match match_link_maps(&nodes, &link_maps) {
        Some(n) => create_node_link(&n, create_link),
        None => match nodes {
            Node::Leaf(_) => [nodes.to_owned()].to_vec(),
            Node::Link(_, _) => [nodes.to_owned()].to_vec(),
            Node::Branch(path, nodes) => {
                let remaining_nodes = link_nodes_matching_configuration(&nodes, link_maps, create_link);
                if !remaining_nodes.is_empty() {
                    [Node::Branch(path.to_owned(), remaining_nodes)].to_vec()
                } else {
                    [].to_vec()
                }
            }
        }
    }
}

fn create_node_link(node: &Node, create_link: fn(&Node) -> bool) -> Vec<Node> {
    if create_link(&node) {
        [].to_vec()
    } else {
        [node.to_owned()].to_vec()
    }
}

fn print(node: Node) {
    match node {
        Node::Leaf(path) => println!("{:?}", path),
        Node::Link(target, _) => println!("{:?}", target),
        Node::Branch(path, mut nodes) => {
            println!("{:?}", path);
            nodes.sort();
            nodes.par_iter()
                .for_each(|v| print(v.to_owned()));
        }
    }
}

//noinspection DuplicatedCode
#[cfg(test)]
mod tests {
    use std::fs;
    use std::fs::File;
    use std::path::{Path, PathBuf};

    use opentelemetry::sdk::export::trace::stdout;
    use tempdir::TempDir;

    use crate::arguments::Arguments;
    use crate::collect_nodes::collect_nodes;
    use crate::configuration::{Configuration, LinkMap};
    use crate::node::Node;
    use crate::run;

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
        let tracer = stdout::new_pipeline().install_simple();
        let arguments = Arguments {
            configuration: "/etc/linker/configuration.json".to_string(),
            dry_run: false,
        };
        let configuration = Configuration::default();

        run(&tracer, &arguments, &configuration);
    }

    #[test]
    #[should_panic]
    fn run_without_source_path() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);
    }

    // Run

    #[test]
    fn run_with_leaf_source() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn run_with_branch_source() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn run_with_nested_sources() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn run_when_link_map_match_both_parent_and_child() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    // Dry run

    #[test]
    fn dry_run_with_leaf_source() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn dry_run_with_branch_source() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn dry_run_with_nested_sources() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }

    #[test]
    fn dry_run_when_link_map_match_both_parent_and_child() {
        let tracer = stdout::new_pipeline().install_simple();
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

        run(&tracer, &arguments, &configuration);

        let actual = collect_nodes(&targets_path);
        assert_eq!(expected, actual);
    }
}
