Semantic Vectors implemented in Mahout for Taste.
Create high-dimensional vector space for recommendations using Random Projection.

Users and Items are randomly placed onto separate vectors.
All user->item preferences "tug" items toward users.

Idea stolen from "semantic vectors" project on google code.
There it is used for collocation of terms in a text database.

Uses (User/Dimension, Pref/Item) mapper: 
org.apache.mahout.cf.taste.hadoop.ToEntityPrefsMapper