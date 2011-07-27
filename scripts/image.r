csv = read.csv("/tmp/lsh_hadoop/gl10k_200_items_distance_ratio_matrix.csv")
csv = as.matrix(csv)
require(grDevices)
image(csv)

