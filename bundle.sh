#!/usr/bin/env bash

###
# A script for manually building bundles to be 
# uploaded to Sonatype public Nexus
###

VERSION=0.7.0

echo "Creating bundle_build directory"

mkdir "bundle_build" || echo "bundle_build already exists"

echo "Copying all artifacts to bundle"

cp target/*.pom bundle_build/
cp target/*.asc bundle_build/

cp javalin-jwt/target/*.pom bundle_build/
cp javalin-jwt/target/*.jar bundle_build/
cp javalin-jwt/target/*.asc bundle_build/

echo "Switching to the bundle_build directory"

cd bundle_build || return 1

echo "Building the parent jar bundle"

jar -cvf javalin-jwt-parent-${VERSION}-bundle.jar \
          javalin-jwt-parent-${VERSION}.pom \
          javalin-jwt-parent-${VERSION}.pom.asc

echo "Parent jar content:"

jar tf javalin-jwt-parent-${VERSION}-bundle.jar

echo "Building the library jar bundle"

jar -cvf javalin-jwt-${VERSION}-bundle.jar \
          javalin-jwt-${VERSION}.pom javalin-jwt-${VERSION}.pom.asc \
          javalin-jwt-${VERSION}.jar javalin-jwt-${VERSION}.jar.asc \
          javalin-jwt-${VERSION}-javadoc.jar javalin-jwt-${VERSION}-javadoc.jar.asc \
          javalin-jwt-${VERSION}-sources.jar javalin-jwt-${VERSION}-sources.jar.asc

echo "Library jar content:"

jar tf javalin-jwt-${VERSION}-bundle.jar

echo "Going back to parent"

cd ..

echo "Done"
