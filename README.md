## About

This is a Java port of the C++ [source code](https://github.com/LaurentGomila/SFML-Game-Development-Book) of the [SFML book](http://www.packtpub.com/sfml-game-development/book). It uses [JSFML library](http://jsfml.org/). I made it primarily in the purpose of learning, but also hoping that it will help anyone who wants to make applications with SFML, but doesn't have enough C++ knowledge to struggle through the book's source code.

I tried to keep the code as close to the original as possible, but because of the differences between C++ and Java (and some limitations of JSFML binding) you'll find some new code, aimed ot overcome those differences, and won't find another, which couldn't be implemented. Most of the "alien" code is in the "Network" chapter.

## Building

You can build a `jar` for each chapter with [Apache Ant](http://ant.apache.org/). To see available targets:

	ant -p
	
	# the output
	Buildfile: .../sfmlbook-java/build.xml
	
	Main targets:
	
	 01_Intro      Generate JAR for chapter 1
	 02_Resources  Generate JAR for chapter 2
	 03_World      Generate JAR for chapter 3
	 04_Input      Generate JAR for chapter 4
	 05_States     Generate JAR for chapter 5
	 06_Menus      Generate JAR for chapter 6
	 07_Gameplay   Generate JAR for chapter 7
	 08_Graphics   Generate JAR for chapter 8
	 09_Audio      Generate JAR for chapter 9
	 10_Network    Generate JAR for chapter 10
	 clean-all     Clean output directories
	 info          Get project information
	Default target: info

To build a `jar` for a `Gameplay` chapter, just run:

	ant 07_Gameplay
	
After that you'll have a `jar` file in the `output` directory.

## Running

	java -jar output/07_gameplay.jar

On Mac OS X you have to [pass](https://github.com/pdinklag/JSFML/wiki/Windows#windows-on-mac-os-x) the JVM option `-XstartOnFirstThread`:

	java -XstartOnFirstThread -jar output/07_gameplay.jar

## Issues

As I've mentioned earlier, I made this port in the purpose of learning. Learning Java. So I'm not a Java expert, and you can expect a number of bugs, bad implementations or whatever Java beginners can do. It goes without saying that I'll appreciate any help in improving this code. You can find the list of currently open issues on this [page](https://github.com/kalimatas/sfmlbook-java/issues).
