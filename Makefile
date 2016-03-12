libs = $(shell find -name '*.jar' | tr '\n' ' ' | sed 's/\.\///g')
classes = $(shell find -name '*.class' | tr '\n' ' ' | sed 's/\.\///g')
sources =  $(shell find -name '*.java' | tr '\n' ' ' | sed 's/\.\///g')

hw3: hw3_easy hw3_hard

hw3_hard: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done) info.kgeorgiy.java.advanced.implementor.Tester class ru.ifmo.ctddev.kamenev.implementor.Implementor $(salt)

hw3_easy: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done) info.kgeorgiy.java.advanced.implementor.Tester interface ru.ifmo.ctddev.kamenev.implementor.Implementor $(salt)

hw2: hw2_easy hw2_hard
hw2_hard: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done) info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet ru.ifmo.ctddev.kamenev.arrayset.ArraySet $(salt)
hw2_easy: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done) info.kgeorgiy.java.advanced.arrayset.Tester SortedSet ru.ifmo.ctddev.kamenev.arrayset.ArraySet $(salt)


hw1: hw1_easy hw1_hard

hw1_easy: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done) info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.kamenev.walk.RecursiveWalk $(salt)	

hw1_hard: jar
	java -cp .$(shell for i in $(libs); do echo -n :$$i; done) info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.kamenev.walk.RecursiveWalk $(salt)	


build_dir:
	mkdir -p build
lib_dir:
	mkdir -p lib
compile: build_dir $(sources)
	find -name '*.java' | tr '\n' ' ' | xargs javac -cp .$(shell for i in $(libs); do echo -n :$$i; done) -d build/
jar: compile lib_dir
	jar cf src.jar -C build .
	mv src.jar lib/
	rm -rf build

clean: lib_dir
	rm -f lib/src.jar
