/*
 * linker
 * Copyright (C) 2023 raatiniemi
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
use std::fmt::{Display, Formatter};
use std::path::PathBuf;

#[derive(Debug)]
pub(crate) enum LinkerError {
    UnableToCreateSymlink(std::io::Error),
    UnableToGetParentDirectory(PathBuf),
}

impl Eq for LinkerError {}

impl PartialEq<Self> for LinkerError {
    fn eq(&self, other: &Self) -> bool {
        match (self, other) {
            (LinkerError::UnableToCreateSymlink(lhs), LinkerError::UnableToCreateSymlink(rhs)) => {
                lhs.kind() == rhs.kind()
            }
            (LinkerError::UnableToGetParentDirectory(lhs), LinkerError::UnableToGetParentDirectory(rhs)) => {
                lhs == rhs
            }
            _ => false
        }
    }
}

impl Error for LinkerError {}

impl Display for LinkerError {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            LinkerError::UnableToCreateSymlink(e) => {
                write!(f, "Unable to create symlink: {}", e)
            }
            LinkerError::UnableToGetParentDirectory(path) => {
                write!(f, "Unable to get parent directory from path {:?}", path)
            }
        }
    }
}
