use std::env;
use std::path::PathBuf;

use crate::configuration::read_configuration;
use crate::collect_nodes::collect_nodes;
use crate::filter_source_nodes::filter_source_nodes;
use crate::filter_target_nodes::filter_target_nodes;

mod configuration;
mod node;
mod collect_nodes;
mod filter_source_nodes;
mod filter_target_nodes;

fn main() {
    match env::args().nth(1) {
        Some(path) => {
            let configuration = read_configuration(&path);
            println!("{:?}", configuration);

            let source = configuration.source.unwrap();
            let source_path = PathBuf::from(source.as_str());
            filter_source_nodes(&collect_nodes(&source_path), &configuration.excludes)
                .iter()
                .for_each(|n| println!("{:?}", n));

            let target_nodes = configuration.targets
                .iter()
                .map(|target| PathBuf::from(target.as_str()))
                .flat_map(|target_path| collect_nodes(&target_path))
                .collect();

            filter_target_nodes(&target_nodes).iter()
                .for_each(|n| println!("{:?}", n));
        }
        _ => panic!("No path for configuration is available")
    };
}
