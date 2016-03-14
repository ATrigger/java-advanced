lib_dir = $(shell find . -name lib -type d)
artifacts_dir = $(shell find . -name artifacts -type d)
src_dir = $(shell find . -name src -type d)
libs = $(shell find $(lib_dir) -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
artifacts = $(shell find $(artifacts_dir) -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
classes = $(shell  find build -name '*.class' | tr '\n' ' ' | sed 's/\.\///g')
sources =  $(shell find $(src_dir) -name '*.java' | tr '\n' ' ' | sed 's/\.\///g')

hw3: hw3_easy hw3_hard

hw3_hard:
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/ImplementorTest.jar info.kgeorgiy.java.advanced.implementor.Tester class ru.ifmo.ctddev.kamenev.implementor.Implementor "$(salt)"

hw3_easy:
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/ImplementorTest.jar info.kgeorgiy.java.advanced.implementor.Tester interface ru.ifmo.ctddev.kamenev.implementor.Implementor "$(salt)"

hw2: hw2_easy hw2_hard
hw2_hard: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/ArraySetTest.jar info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet ru.ifmo.ctddev.kamenev.arrayset.ArraySet "$(salt)"
hw2_easy: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):$(artifacts_dir)/ArraySetTest.jar info.kgeorgiy.java.advanced.arrayset.Tester SortedSet ru.ifmo.ctddev.kamenev.arrayset.ArraySet "$(salt)"


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
	find $(src_dir) -name '*.java' | tr '\n' ' ' | xargs javac -cp .$(shell for i in $(libs); do echo -n :$$i; done):.$(shell for i in $(artifacts); do echo -n :$$i; done) -d build/
jar: compile lib_dir
	jar cf src.jar -C build .
	mv src.jar lib/
	rm -rf build

clean: 
	rm -rf lib
