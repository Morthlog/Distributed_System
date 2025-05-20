@echo off
cd ..\src
del *.class
javac -cp ".;jar\json-simple-1.1.1.jar" *.java