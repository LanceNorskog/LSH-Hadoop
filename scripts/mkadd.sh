
# create a script to add a file
# Is original upload 

[ $# -eq 0 ] && echo "Need at least one file or directory" && exit 1

git diff -U5 --no-prefix 3437c32c20472f928ff422cf2173d8475aa87e31 HEAD $*
