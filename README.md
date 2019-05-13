# cs518-cryptdb

## Dependencies:
Put dependencies in the lib/ folder

H2: http://repo2.maven.org/maven2/com/h2database/h2/1.4.199/h2-1.4.199.jar
Place this file in lib/

OPE: from https://github.com/aymanmadkour/ope.git
Included as a submodule
```> git submodule update --init
> cd ope/ope
> mvn package
> mv ope/ope/target/ope-0.0.1-SNAPSHOT.jar lib/ope.jar```

Similarly, build and copy JSqelParser to lib/

##Build
You have two options. You can boot up our project in Eclipse (or your favorite IDE). Alternately, you can run the following:
```cd impl/ && ant && ant -buildfile compile.xml```

## License
This project is Licensed under the Lesser GNU General Public License. You can find a copy in LICENSE.TXT.

jar-in-jar-loader.zip is unmodified from its original state, and is licensed under the Eclipse Public License, which can be found here https://www.eclipse.org/legal/epl-v10.html

## Attribution
Significant portions of cs518.cryptdb.proxy.parser are derived from JSqlParser's source code.