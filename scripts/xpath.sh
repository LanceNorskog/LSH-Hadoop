#/usr/bin/env


SELECT=.

if [ $# = 0 ]
then
	echo "xpath.sh -s select xpath file(s)"
	exit 1
fi

if [ "$1" = "-s" ]
then
	SELECT="$2"
	shift 2
fi

if [ $# -lt 2 ]
then
	echo "xpath.sh -s select xpath file(s)"
	exit 1
fi

XPATH="$1"
shift


TMPFILE=/tmp/xpath.$$
cat > $TMPFILE <<EOF
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0"
>
<xsl:output version="1.0" indent="no" method="text" omit-xml-declaration="yes" />
		<xsl:template match="/">
				<xsl:for-each select="$XPATH" >
					<xsl:value-of select="normalize-space($SELECT)" />
					<xsl:value-of select="'&#10;'" />
				</xsl:for-each>
		</xsl:template>
</xsl:stylesheet>
EOF

xsltproc $TMPFILE $*

rm $TMPFILE
