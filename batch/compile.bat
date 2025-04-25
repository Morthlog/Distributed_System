@echo off
cd ..\src
if exist Worker.class del Worker.class
javac  -cp .;jar/json-simple-1.1.1.jar  Worker.java

//uncomment this for the cmd window to stay open
//pause