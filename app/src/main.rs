use std::env;
use std::path::PathBuf;

use crate::configuration::read_configuration;
use crate::node::collect_nodes;
use crate::filter_source_nodes::filter_source_nodes;

mod configuration;
mod node;
mod filter_source_nodes;

fn main() {
    match env::args().nth(1) {
        Some(path) => {
            let configuration = read_configuration(&path);
            println!("{:?}", configuration);

            let source = configuration.source.unwrap();
            let source_path = PathBuf::from(source.as_str());
            filter_source_nodes(&collect_nodes(&source_path), &configuration.excludes)
                .iter()
                .for_each(|n| println!("{:?}", n))
        }
        _ => panic!("No path for configuration is available")
    };
}
