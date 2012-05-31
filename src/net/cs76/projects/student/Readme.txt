Philip Regan
http://www.oatmealandcoffee.com

Readme: CS-76 Android Student Project Documentation
==============================================================================
About
=====
RPG Kit an application that allows you to create and play simple role-playing games on an Android mobile device.

NOTE BENE (NB): Implementation of database management and interaction with activities grew to a larger scale of work than predicted. As such, there are certain features mentioned below that have not been implemented yet, but I would like to continue in iOS.

* Creation of new game objects, addition and deletion of game objects; only editing of currently existing game objects is allowed.
* Equipment and Attributes of any kind
* Linking and delinking of Equipment and Tasks to other Tasks and Players
* Home (for passing time) and Stores (for purchasing and selling Equipment)

==============
Playing a Game
==============

Games are essentially composed of three objects:
* A playerÑyour character in the game
* TasksÑwhich can be your home, a store, a race, or just about anything. Tasks linked to other tasks as siblings are Locations. Tasks that are linked to locations as things to do are Accomplishments. The Player travels from one Location to another performing the Accomplishments put forth by the task.
* EquipmentÑwhich can be food, a car, or (again) just about anything that is owned by the player or stored in a Location

The application starts off with two sample games to play and edit:
* A Day at the Races: Like Gran Turismo, but without the hassle of actually driving the car.
* Back At The Office: Meanwhile, in the real world, this week is your turn in the Office Donut Club.

Game play is based on Locations within the game. Essentially, you go to a LocationÑall games start at the Home LocationÑwhere you can either perform the Accomplishments listed in the Location, or go to another Location made accessible from the Location where you are currently.

If you are at Home, there are no Accomplishments, but you may pass time to either gain income and/or repair any damage, heal, and the like. Home should always be a safe haven from everywhere else in the game.

When you are in a Store, there is a list of Equipment that you can buy, and you can sell Equipment when in a Store. Equipment available to be purchased is listed where Accomplishments normally are shown.

Accomplishments
===============

Accomplishments are types of Tasks that the Player is pitted against at a given Location. In order to perform the Accomplishments, simply press the "Do Accomplishments" button to have the game see how well you do. Each Accomplishment has a set of rewards and penalties that are applied to the Player based on how well they do. There are no ties, and if a Player "loses" before all of the Accomplishments are completed, then game will stop and return the Player to the Location.

Time
====
Every time the player moves from one location to another or performs a tasks within a game, the time unit is incremented by 1 (one).

==============
Editing a Game
==============

There are four areas where a game can be edited as displayed in the Game Components screen:
* Game Information
* Player Information
* Tasks
* Equipment

When editing any information, it is important to press the Save button at the bottom of the screen. Pressing Cancel or going backwards will cause you to lose your changes.

Game Information
================

The game information pertains to the world or environment that the game takes place. You can edit the following items:
* Title
* Introduction
* Time UnitÑHour, Day, Week, and Month would be the most common, but any time unit is allowed.
* Money UnitÑDollars, Credits, Gold Pieces are obvious choice, but like Time, any unit is allowed.

Player information
==================

The player has three points of information to edit:
* Name
* Bio
* Equipment

There has to be no more or less than one player in a game. They can hold any number of Equipment items.

Tasks
=====

Tasks are a little complicated but not too much so. A Task is a generic object that can be used as either a Location or Accomplishment. A Task that links to other Tasks are known as Locations. These are Game-level objects that the player can travel to and from. Tasks that are listed as Accomplishments are things the player can do when in a given Location. A Task can link to any number of Accomplishments and Locations.

Accomplishments
===============

Accomplishments are types of Tasks that the Player is pitted against at a given Location. Accomplishments have a variety of Attributes that are used to reward or punish a Player based on how well they did at a given Accomplishment.

================
Technical Manual
================

Content Storage (Model)
=======================

RPG Kit's content storage is based entirely in SQLite. While would have been technically possible to create object classes and have all of the content loaded at runtime, that would not have worked with large games. All data is pulled and pushed to the database on an as-needed basis by a given Activity to save on memory. The database opening and closing is managed within the method actually making queries to the database.

Content in the database is spread across a variety of tables to ensure a lean database size and separation of responsibilities. Content relationships are managed using a loosely-couple parent-child relationship where each piece of content is owned by a parent class. With each piece of content, the parent's ID and Type are recorded. Most DB queries start from with "SELECT column_name FROM class_table WHERE parent_id = n AND parent_type = class_name" and build out from there.

The class OCDbController provides the core interaction with the database needed to manage it while other classes carry out tasks specific to the accompanying activity. This is typically done with private classes within the activities that subclass from OCDbController to gain their functionality and bridge the gap between the database and the activity, which in turn interact with the user. OCDbController also provides the initial samples from which the user can build their own games.

Activities (Controllers)
========================

OCCoreActivity provides a number of common strings and a couple helper methods common to other activities. Most activities are specifically tailored to the given task, but OCGameObjectSelectionActivity provides a simple list selection method to help manage large lists of items that would not otherwise normally fit well with other content on screen, like selecting a Task to edit.

Layouts (Views)
===============
Layouts use standard Android widgets and great care has been taken in given all interface elements IDs that are unique across the entire application by using a namespace_class_activity_purpose naming convention. A bit verbose but it also ensure there will not be any naming collisions across the application.