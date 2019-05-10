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
All source code is all rights reserved, except functions which are explicitly mentioned in comments as sourced externally.

The contents of impl/data are derivate works of Giuseppe Maxia and can be found at https://github.com/datacharmer/test_db. That work is licensed under Creative Commons Attribution-Share Alike 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA."
