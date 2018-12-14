# Icytomine - Integrating Cytomine into ICY

## Version 0.0.1

Icytomine is a set of plugin that allows _Cytomine_ users to interact with their account directly from _ICY_. Users can access Cytomine from Icy using the Graphical Interface or from a command line. In the following sections plugins in Icytomine are described. Also, instructions on how to configure the development environment are included.

### 1. Introduction

Icytomine tries to integrate two technologies, one handling large images and the other performing image analysis.

On one hand, Cytomine is a server based software that provides a solution to store and handle annotations on large medical and biological images. At the moment of writing Cytomine supports only 2D images.

On the other hand, Icy is a client-side software specialized in image analysis for biological images. It can perform heavy image processing techniques but can only work in images of limited size.

The idea of integrating these two technologies was started back in 2015 by developing a simple prototype comprising a connection and image transferring plugin. This effort was extended in 2016 when a primitive viewer was introduced alongside with some protocols to retrieve images from Cytomine servers as Sequences in Icy. This current release takes those works and creates from the ground a set of plugins that offer users the possibility to interact with Cytomine servers directly on Icy in the following ways.

* Supporting multiple active connections to different servers.
* Allowing for image and project exploration and search with a user-friendly interface.
* An easy-to-use viewer is available to view Annotations and images at different resolutions.
* Import/Export of image between Icy and Cytomine, enabling image processing results to be stored on servers.
* Different protocols have been developed in order to enable unattended processing and batch processing of annotations, images and projects.

### 2. Using Icytomine

#### 2.1. Opening the Icytomine Explorer

The explorer is the main plugin of Icytomine. It allows users to view available projects and images, as well as opening the viewer to explore annotations inside each image.

To open the explorer type `Icytomine` on the plugin search bar in Icy and select the plugin called *Icytomine Explorer*. 

![alt text](https://github.com/danyfel80/icytomine/raw/master/imgs/01.OpenIcytomine.png "Open Icytomine")

The connection dialog will appear to allow the user to register his credentials and perform the connection to the server.

![alt-text](https://github.com/danyfel80/icytomine/raw/master/imgs/02.LoginDialog.png "Login Dialog")

##### 2.1.1. Adding User Credentials to Icytomine

Once on the connection dialog, the user can add multiple credentials to connect to multiple servers. In order to add a new credential click on the button *Add user*.

![alt-text](https://github.com/danyfel80/icytomine/raw/master/imgs/03.AddUserDialog.png "Add user")

Fill the credential registration form and click *Add*. The credentials just created will now appear as an option on the connection dialog. If you want to cancel the addition of a user click on *Cancel* to go back to the connection dialog.

##### 2.1.2. Editing User Credentials

If you have already registered an account on Icytomine, you can edit its credentials by clicking the *Edit user* button on the connection dialog.

![alt-text](https://github.com/danyfel80/icytomine/raw/master/imgs/04.EditUserDialog.png "Edit user")

Edit the credentials where necessary on the form and click *Save*. The credentials will now be up to date. If you want to discard the changes made to the credentials click on *Cancel*, you will be redirected back to the connection dialog.

##### 2.1.3. Connecting with an existing account

Once you have an account setup for the target server, you can connect to it by selecting the target server and the user used to establish the connection. Then click the *Connect* button.

If the connection is successful the Explorer panel will appear. Otherwise, check your connection credentials and adjust them appropriately.

#### 2.2. Using the Icytomine Explorer

![alt-text](https://github.com/danyfel80/icytomine/raw/master/imgs/05.ExplorerInterface.png "Explorer interface")

##### 2.2.1. Window structure

The explorer is divided in three main panels (columns). The left most panel presents available projects. The central panel shows available images for a selected project. Finally, the rightmost panel presents details on the last selected item (either a project or an image).

##### 2.2.2. Searching projects

The projects panel allows users to see the list of projects available for the current user as well as to search for a project by its name or identifier. To do this, the user just has to type on the search bar on top of the projects panel. The search criterion can be partial. In other words, the user does not have to type the full name or identifier of the project. The list will progressively filter available projects to keep only those who respect the search criterion.

When a project is selected, two things happen. First, the list of available images for the selected project will be displayed on the images panel. And second, the details of the project will be displayed on the details panel.

#####2.2.3. Searching images

Once a project is selected, the user can choose an image from the list of available images for the that project. In addition, users can search images by their name or their identifier. To do this, the user just has to type on the search bar on top of the images panel. The search criterion can be partial. In other words, the user does not have to type the full name or identifier of the image. The list will progressively filter available images to keep only those who respect the search criterion.

When an image is selected the details of the image will be displayed on the details panel.

#####2.2.4. Setting up image magnification and pixel resolution

The user can adapt  the magnification and the pixel resolution values of a selected image if and only if the user is the uploader of the image in the server. To do this the user has to click the *Edit* button on the details panel on the desired property, then type the new value, followed by clicking the *Save* button. The details will be updated afterwards.

####2.3. Using the Image Viewer

#####2.3.1. Moving around an image

#####2.3.2. Transferring images

######2.3.2.1. Transferring images from Cytomine to Icy

######2.3.2.2. Transferring annotations on an open sequence to Icytomine

######2.3.2.3. Transferring annotations on an image file to Icytomine

######2.3.2.4. Transferring annotations on multiple image files in a folder to Icytomine

#####2.3.3. Handling annotations

######2.3.3.1. Filtering visible annotations

######2.3.3.2. Selecting annotations

######2.3.3.3. Associating terms to an annotation

######2.3.3.4. Deleting annotations



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

