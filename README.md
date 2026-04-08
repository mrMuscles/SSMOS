# SSMOS - 1.21.4

Super Smash Mobs Open Source Recreation for community usage

This is an SSMOS Fork for version 1.21.4

# Dependencies (required)	

Spigot 1.21.4

Please use the Remapped Spigot JAR when compiling the plugin (--remapped option if using buildtools cmd line)

# Development Environment Setup

1. Download the Updated Source Code from https://github.com/mrMuscles/SSMOS
2. Run your IDE (probably intellij) and open up the SSMOS project
3. Allow the build scripts to initialize the developer environment
4. Press "maven" and "reload all maven projects" button (alternatively right-click "SSMOS" in the maven sub-menu and press "generate sources and update folders")
5. Once this process is finished, the developer environment should be ready and all your libraries compiled and ready to go

**Notes:** The template directory was removed at the last commit of the original code found here: [https://github.com/Whoneedspacee/SSMOS](https://github.com/Whoneedspacee/SSMOS)

If you want to view the template directory it is found here: https://github.com/Whoneedspacee/SSMOS/commit/d774b9b

The template directory was built for 1.8.9 and this fork has been updated to 1.21.4, because of this the template server won't run without serious work

# Compiling code & gameplay

1. Press the "maven" menu, expand the "lifecycle" tab and click the "install" button
2. Wait for the IDE to successfully compile your code, which should end in a "BUILD SUCCESS"
3. Under the "projects" menu, expand the "target" directory, and copy-paste the file titled "SSMOS-1.0.jar" into your desired plugins folder
4. Finally create a Paper 1.21.4 Server to run the plugin and place it into the "plugins" directory.
   
**Note:** Spigot Server may not work or hang.
