use std::env;
use std::path::PathBuf;

use crate::configuration::{read_configuration, Configuration};
use crate::collect_nodes::collect_nodes;
use crate::filter_source_nodes::filter_source_nodes;
use crate::filter_target_nodes::filter_target_nodes;
use crate::match_link_maps::match_link_maps;
use crate::filter::filter;
use crate::node::Node;

mod configuration;
mod node;
mod collect_nodes;
mod filter_source_nodes;
mod filter_target_nodes;
mod filter;
mod match_link_maps;

fn main() {
    match env::args().nth(1) {
        Some(path) => {
            let configuration = read_configuration(&path);
            println!("{:?}", configuration);

            let source_nodes = collect_and_filter_source_nodes(&configuration);
            let target_nodes = collect_and_filter_target_nodes(&configuration);
            filter(&source_nodes, &target_nodes)
                .iter()
                .flat_map(|n| match_link_maps(n, &configuration.link_maps))
                .for_each(|n| println!("{:?}", n));
        }
        _ => panic!("No path for configuration is available")
    };
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
