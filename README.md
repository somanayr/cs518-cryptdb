# cs518-cryptdb

## Dependencies:
Put dependencies in the lib/ folder

H2: http://repo2.maven.org/maven2/com/h2database/h2/1.4.199/h2-1.4.199.jar
OPE: from https://github.com/aymanmadkour/ope.git
Included as a submodule
``` > git submodule update --init
``` > cd ope/ope
``` > mvn package
``` > mv ope/ope/target/ope-0.0.1-SNAPSHOT.jar lib/ope.jar

## License
Until we decide on a license, all source code is all rights reserved, except functions which are explicitly mentioned in comments as sourced externally.

jar-in-jar-loader.zip is unmodified from its original state, and is licensed under the Eclipse Public License, which can be found here https://www.eclipse.org/legal/epl-v10.html