use std::env;

use crate::configuration::read_configuration;

mod configuration;

fn main() {
    match env::args().nth(1) {
        Some(path) => {
            let configuration = read_configuration(&path);
            println!("{:?}", configuration)
        }
        _ => panic!("No path for configuration is available")
    };
}
