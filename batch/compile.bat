@echo off
cd ..\src
del Worker.class
javac  -cp .;jar/json-simple-1.1.1.jar  Worker.java