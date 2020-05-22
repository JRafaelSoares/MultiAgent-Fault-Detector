## Graphical Interface

Install plugin (you only have to run this once in a given computer):

    mvn install:install-file -Dfile=lib/JavaFXSmartGraph-0.9.jar -DgroupId=com.brunomnsilva -DartifactId=smartgraph -Dversion=0.9 -Dpackaging=jar -DgeneratePom=true

Generate resources:
   
    mvn clean install -DskipTests
    
Run project with graphical interface:

    mvn compile exec:java

## Statistics

To obtain statistics, generate resources as shown above and then run:
    
    mvn compile exec:java@statistics -DagentType=agentName -DstatisticsProperties=fileName
    
_agentName_ must correspond to an _agentName_.properties file, built as specified [here](src/main/resources/agents/README.md).
If this parameter is not specified, the baseline agent is selected.

_fileName_ must correspond to a _fileName_.properties file
Check the [statistics properties README](src/main/resources/StatisticsPropertiesREADME.md) to see how a run's properties can be customized