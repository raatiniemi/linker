use std::path::PathBuf;

use crate::configuration::{read_configuration, Configuration};
use crate::collect_nodes::collect_nodes;
use crate::filter_source_nodes::filter_source_nodes;
use crate::filter_target_nodes::filter_target_nodes;
use crate::match_link_maps::match_link_maps;
use crate::filter::filter;
use crate::node::Node;
use crate::arguments::collect_and_parse_arguments;
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
    println!("{:?}", configuration);

    let source_nodes = collect_and_filter_source_nodes(&configuration);
    let target_nodes = collect_and_filter_target_nodes(&configuration);
    filter(&source_nodes, &target_nodes)
        .iter()
        .flat_map(|n| match_link_maps(n, &configuration.link_maps))
        .for_each(|n| println!("{:?}", n));
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
