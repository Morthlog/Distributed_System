@echo off
cd ..\src
del stubUser.class
javac stubUser.java
for /l %%x in (1, 1, 1) do (
   start cmd /k java stubUser User-%%x
)