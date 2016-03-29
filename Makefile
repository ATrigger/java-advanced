kgeorgiy_dir = $(shell find . -maxdepth 1 -name java-advanced-2016 -type d)
lib_dir = $(shell find $(kgeorgiy_dir) -name lib -type d)
mylib_dir = $(shell find . -maxdepth 1 -name lib -type d)
artifacts_dir = $(shell find . -name artifacts -type d)
src_dir = $(shell find . -maxdepth 1 -name src  -type d)
libs = $(shell find  $(lib_dir) -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
artifacts = $(shell find $(artifacts_dir) -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
classes = $(shell  find build -name '*.class' | tr '\n' ' ' | sed 's/\.\///g')
sources =  $(shell find $(src_dir) -name '*.java' | tr '\n' ' ' | sed 's/\.\///g')
myjar = $(shell find $(mylib_dir) -name '*.jar'| tr '\n' ' ' | sed 's/\.\///g')

hw7:
	java -cp "lib/src.jar$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/ParallelMapperTest.jar" \
	info.kgeorgiy.java.advanced.mapper.Tester list ru.ifmo.ctddev.kamenev.mapper.ParallelMapperImpl,\
	ru.ifmo.ctddev.kamenev.mapper.IterativeParallelism "$(salt)"

hw6:
	java -cp "lib/src.jar$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/IterativeParallelismTest.jar" \
	info.kgeorgiy.java.advanced.concurrent.Tester list ru.ifmo.ctddev.kamenev.parallel.IterativeParallelism "$(salt)"

hw1: hw1_easy hw1_hard

hw1_easy: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/WalkTest.jar info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.kamenev.walk.RecursiveWalk "$(salt)"	

hw1_hard: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/WalkTest.jar info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.kamenev.walk.RecursiveWalk "$(salt)"	


build_dir:
	mkdir -p build
lib_dir:
	mkdir -p lib
compile: build_dir $(sources)
	find $(src_dir)  -name '*.java' | tr '\n' ' ' | xargs javac -cp .$(shell for i in $(libs); do echo -n :$$i; done):\
	.$(shell for i in $(artifacts); do echo -n :$$i; done) -d build/
jarhw4: lib_dir
	jar cvfm src.jar src/ru/ifmo/ctddev/kamenev/implementor/META-INF/MANIFEST.MF -C build ./
	mv src.jar lib/
jarhw6: lib_dir
	jar cvfm src.jar src/ru/ifmo/ctddev/kamenev/parallel/META-INF/MANIFEST.MF -C build ./
	mv src.jar lib/
doc: javadoc

javadoc:
	rm -rf doc 
	mkdir doc
	javadoc  -sourcepath "src/:java-advanced-2016/java" -cp .$(shell for i in $(libs); do echo -n :$$i; done) -d doc -author -private -link http://docs.oracle.com/javase/8/docs/api/ \
	ru.ifmo.ctddev.kamenev.mapper ru.ifmo.ctddev.kamenev.implementor ru.ifmo.ctddev.kamenev.parallel
clean: 
	rm -rf lib/src.jar
	rm -rf doc
	rm -rf build
