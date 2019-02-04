# Random Picture

Display a picture of your favourite list randomly.

## Setup

You need Docker installed first, then:

* Pick your favourite pictures and copy them into a folder (sub-folders are supported), like:
  
  ```shell
    ~/Pictures $ ls -l
    1.jpg
    2.jpg
    3.jpg
    4.jpg
    5.jpg
  ```
  > The list of supported formats are in MainVerticle#ACCEPTABLE_FILES

* Let's go!
  
  Mount the folder and start:
  ```shell
  $ docker run --rm -p 8080:8080 -v your/favourite/folder:/app/favourite thnuiwelr/randompicture
  ```
  Finally visit localhost:8080.

# Licenses

GPL-v3