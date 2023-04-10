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

use clap::Parser;

const ARGUMENT_AUTHOR: &'static str = "Tobias Raatiniemi <raatiniemi@gmail.com>";
const ARGUMENT_VERSION: &'static str = "0.0.1";
const ARGUMENT_ABOUT: &'static str = "Create symbolic links from target directories to a single source directory.";

const ARGUMENT_CONFIGURATION_HELP: &'static str = "Path to the configuration file.";
const ARGUMENT_DRY_RUN_HELP: &'static str = "Run application without performing any changes.";

#[derive(Parser, Debug)]
#[command(author = ARGUMENT_AUTHOR, version = ARGUMENT_VERSION, about = ARGUMENT_ABOUT, long_about = None)]
pub(crate) struct Arguments {
    #[arg(short, long, help = ARGUMENT_CONFIGURATION_HELP)]
    pub(crate) configuration: String,
    #[arg(long, help = ARGUMENT_DRY_RUN_HELP)]
    pub(crate) dry_run: bool,
}
