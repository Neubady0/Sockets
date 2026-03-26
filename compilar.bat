@echo off
cd src
dir /s /B *.java > sources.txt
javac -d ..\bin @sources.txt
echo Compilacion terminada.
