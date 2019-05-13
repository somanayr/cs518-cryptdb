# cs518-cryptdb

## Dependencies:
Put dependencies in the lib/ folder

H2: [http://repo2.maven.org/maven2/com/h2database/h2/1.4.199/h2-1.4.199.jar](http://repo2.maven.org/maven2/com/h2database/h2/1.4.199/h2-1.4.199.jar)
Place this file in `lib/`

OPE: from [https://github.com/aymanmadkour/ope](https://github.com/aymanmadkour/ope)

JSqlParser: from [https://github.com/JSQLParser/JSqlParser](https://github.com/JSQLParser/JSqlParser)

OPE and JSqlParser are included as submodules. You can build them like so:

```bash
> git submodule update --init
> cd ope/ope
> mvn package
> mv ope/ope/target/ope-0.0.1-SNAPSHOT.jar lib/ope.jar
```

Similarly, build and copy JSqlParser to `lib/`

## Build
You have two options. You can boot up our project in Eclipse (or your favorite IDE). Alternately, you can run the following command:
`cd impl/ && ant && ant -buildfile compile.xml`. This will produce 4 JAR files in `build/` that will run the program.

## License
Except as otherwise noted, this project is Licensed under the Lesser GNU General Public License. You can find a copy in `LICENSE.txt`.

`impl/jar-in-jar-loader.zip` is unmodified from its original state, and is licensed under the Eclipse Public License, which can be found at [https://www.eclipse.org/legal/epl-v10.html](https://www.eclipse.org/legal/epl-v10.html)

## Attribution
Significant portions of the package `cs518.cryptdb.proxy.parser` are derived from JSqlParser's source code.