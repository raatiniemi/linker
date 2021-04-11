#[derive(Eq, PartialEq, Ord, PartialOrd, Clone, Debug)]
pub enum Node {
    Leaf(String),
    Link(String, String),
    Branch(String, Vec<Node>),
}
