source-to-image:
	cd eperusteet && mvn clean install -DskipTests -Dbranch=${BRANCH} -Drevision=${REVISION} -DbuildNumber=${BUILD_NUMBER}
