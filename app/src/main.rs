use std::env;

use crate::configuration::read_configuration;
use crate::node::collect_nodes;
use std::path::Path;

mod configuration;
mod node;

fn main() {
    match env::args().nth(1) {
        Some(path) => {
            let configuration = read_configuration(&path);
            println!("{:?}", configuration);

            let source = configuration.source.unwrap();
            let source_path = Path::new(source.as_str());
            collect_nodes(source_path).iter()
                .for_each(|n| println!("{:?}", n))
        }
        _ => panic!("No path for configuration is available")
    };
}
