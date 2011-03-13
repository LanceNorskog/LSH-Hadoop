Semantic Vectors implemented in Mahout for Taste.
Create high-dimensional vector space for recommendations using Random Projection.

Users and Items are randomly placed onto separate vectors.
All user->item preferences "tug" items toward users.

Idea stolen from "semantic vectors" project on google code.
There it is used for collocation of terms in a text database.

This implementation uses the RandomVector and RandomMatrix implementations to do a one-pass map. 
It is not partitionable. Could be partitionable as multiple products for each item summed in the reduce.

M/R pass:
1 - emit key=item user/item/pref
2 - reduce - emit Item vectors to SequenceFile

RandomMatrix and RandomVector are set as configuration values and never copied anywhere.