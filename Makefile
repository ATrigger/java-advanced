libs = $(shell find lib -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
artifacts = $(shell find artifacts -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
classes = $(shell find -name '*.class' | tr '\n' ' ' | sed 's/\.\///g')
sources =  $(shell find -name '*.java' | tr '\n' ' ' | sed 's/\.\///g')

hw3: hw3_easy hw3_hard

hw3_hard: 
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):artifacts/ImplementorTest.jar info.kgeorgiy.java.advanced.implementor.Tester class ru.ifmo.ctddev.zholus.implementor.Implementor "$(salt)"

hw3_easy: 
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):artifacts/ImplementorTest.jar info.kgeorgiy.java.advanced.implementor.Tester interface ru.ifmo.ctddev.zholus.implementor.Implementor "$(salt)"

hw2: hw2_easy hw2_hard
hw2_hard: 
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):artifacts/ArraySetTest.jar info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet ru.ifmo.ctddev.zholus.collection.ArraySet "$(salt)"
hw2_easy: 
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):artifacts/ArraySetTest.jar info.kgeorgiy.java.advanced.arrayset.Tester SortedSet ru.ifmo.ctddev.zholus.collection.ArraySet "$(salt)"


hw1: hw1_easy hw1_hard

hw1_easy: 
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):artifacts/WalkTest.jar info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.zholus.walk.RecursiveWalk $(salt)	

hw1_hard: 
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done):artifacts/WalkTest.jar info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.zholus.walk.RecursiveWalk $(salt)	


build_dir:
	mkdir -p build
lib_dir:
	mkdir -p lib
compile: build_dir $(sources)
	find -name '*.java' | tr '\n' ' ' | xargs javac -cp .$(shell for i in $(artifacts); do echo -n :$$i; done) -d build/
jar: compile lib_dir
	jar cf src.jar -C build .
	mv src.jar lib/
	rm -rf build

clean: lib_dir
	rm -f lib/src.jar
