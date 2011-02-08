src/core
	Main LSH code + SVG generator
src/hadoop
	Hadoop code using the core library.

No libraries: depends on externally stored hadoop-xx.x checkout for libraries with source and docs.

Only uses core Hadoop - no distributed support at this point.

There are 2 numerical spaces: Point Space and Grid Space.

Point Space is the N-dimensional space of the input data.

Grid Space is counted in integers, and is the nearest lower-left corner to a point: grid(point) = Math.floor(point) for all N. The grid size is set with a floating-point size in Point Space.

Grid Space does not use floating points because it has to be transmitted via text, and floating point numbers do not stay constant that way. Bitwise comparisons of floating point values are the road to madness.

A Hasher translates between Grid and Point spaces. The simpler one, Orthonormal, is the right-triangle-based "orthonormal" space in Neylon's paper. If the Grid Space box size is 2.0, then (0.1,3.2) -> (0,1) in Grid Space. VertexTransitive hashing is merely Orthonormal hashing with an affine transform that turns the right-angled triangles into equilateral triangles.

sample/ contains SVG displays of the same point set, hashed with Orthonormal and VertexTransitive Hashers with a grid size of 2.

The above "triangles" are in fact N-dimensional space-filling "simplices". In 3 dimensions, each simplex is an equal-sided tetrahedron.

