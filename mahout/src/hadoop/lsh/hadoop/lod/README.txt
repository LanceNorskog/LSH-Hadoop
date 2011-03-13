Level-of-Detail reducer for LSH.

Map all points to a grid and a level-of-detail number.
LOD is powers of 2 in grid coords, in all dimensions.

Rule: The # points in Grid&LOD passes a rule: 
   Subdividing top-down rule: at least 3 within distance N.
   Grouping bottom-up rule: (not sure can do bottom-up)
   maximum distance N 
   at most 20 points within distance N, 
   


Values:
D - # of dimensions
N - Number of grid units
    LOD ranges from 1 to N.
    Grid point@LOD = point modulo LOD
    All LOD in following includes module grid
P - # of points

Grid values are mapped by an LSH.
Grids are modulo to LOD, therefore N*P grid/LOD points 

Algorithm for limiting: subdivide via iterative M/R
As long as a rule passes the next-smallest LOD, emit next-smallest LOD

From raw points:
Mapper: map point to grid LOD=N/ point
From chain:
IdentityMapper

Iterative reducer:
Reducer:
	Configured with a Rule
	grid,LOD / points
	partition points per LOD-1
	for X in LOD-1
   		Do all LOD-1 pass rule?
	If no, 
		persist grid,LOD/points
	else 
		for X in LOD-1		
			collect (grid@X,X)/partition@X
			lather/rinse/repeat

-----------------------------

Algorithm for requiring: join neighbors via iterative M/R
Mapper: 
	map point to grid,LOD=2/point
Iterative Reducer:
Reducer:
	Configured with a rule:
	grid,LOD/point
	if point set fails rule
		partition points
		for X in LOD - 1
			persist grid@X,X/partition@X
	
	
----------------------------
Join above to point output:
Mapper:
    emit point, grid, LOD
Reducer
    emit point, grid, LOD
	

 