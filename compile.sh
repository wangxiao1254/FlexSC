find . -name "*.java" > source.txt;
javac -cp bin/:lib/junit.jar:lib/commons-io-2.4.jar -d bin @source.txt;
