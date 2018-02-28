# Icytomine - Integrating Cytomine into ICY

## Description (Version 0.0.1)

Icytomine is a plugin that allows _Cytomine_ users to interact with their account directly from _ICY_. Users can interact with Cytomine from Icy using the Graphical Interface or from a command line. In the sections below, the plugin is described, presenting a guide to use Icytomine, how to configure the development environment for develop to help the evolution of this plugin.

### Using Icytomine

When using Icy to interact with Cytomine there are two ways of achieving this: Using a GUI and using a command line (or *terminal*). The GUI presents an ergonomic UI that allows to interact in real-time with images stored on a remote Cytomine server. This mode is recommended to explore images before performing any analysis on them. Meanwhile, for automatic processing the terminal allows for chaining tasks. This mode is suitable for batch processes that usually take time, and thus, they can be prepared and then run without any user supervision.

In the next two sections, these two modes are introduced and a quick guide to use them is presented.

#### Using the graphical interface

The graphical mode is the most intuitive way of interacting with Cytomine from Icy. It is probably the easiest way to get started with using Icy with cytomine as there is no need to learn any command to perform tasks.

##### Starting Icytomine

1. Launch the *Icytomine Explorer*.
2. Login to the Cytomine server.
3. Exploring existing projects.
4. Exploring images of a project.
5. Viewing images.

#### Using the command line

The command line is the most powerful way to use Cytomine with Icy as tasks can be chained and automatized to perform multiple processes without user supervision.

1. Launching Icytomine from a terminal.
2. Understanding the commands and how to use them.

### Setting up a development environment with Eclipse

#### Requirements

* JDK
* Icy
* Eclipse IDE
* A Git Client (Or Eclipse)

#### Setup

1. Set the ICY_HOME environment variable.
2. Download the project from github.
3. Import the project with Eclipse.

#### Project organization

