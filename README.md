Install plugin (you only have to run this once is a given computer):

    mvn install:install-file -Dfile=lib/JavaFXSmartGraph-0.9.jar -DgroupId=com.brunomnsilva -DartifactId=smartgraph -Dversion=0.9 -Dpackaging=jar -DgeneratePom=true

Generate resources:
   
    mvn clean install -DskipTests
    
Run project:

    mvn compile exec:java