# SYSC 3303 Elevator Project

Forked from [quinn-parrott/sysc-elevator](https://github.com/quinn-parrott/sysc3303-elevator)

## Description

A multi-threaded Java project that simulates the behaviour of an elevator. Serves as an introduction to Java synchronization and multi-threading.

## How to run

- Clone repository to a folder
- Open folder in Eclipse IDE
- Navigate to Run -> Run Configurations in Eclipse
- Click on Main -> Arguments
- Paste the following arguments in the "Program arguments" box
  -  `scheduler floor open_close_door=1500 load_unload=6000 between_floors=7000 elevator elevator elevator elevator`
  -  This runs a scheduler, floor request feeder, and 4 elevators
  -  Time is measured in milliseconds
-  Run Main.main() in Eclipse
-  Open the Swing application

## Contributors
Kamsi Ekweozor, Tao Lufula, Quinn Parrott, Ibrahim Said, Hamza Zafar

