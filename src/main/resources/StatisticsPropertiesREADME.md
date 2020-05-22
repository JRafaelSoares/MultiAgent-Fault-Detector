## Statistics Properties

The file [defaultStatistics.properties](defaultStatistics.properties) contains
default values for obtaining statistics.

It is of the format:
    
    property=value
    
New files can be created to be specified when running the project
in statistics mode.

Currently, the existence of these variables is mandatory:

    runs
    ticksPerRun
    numPairs
    quorumSize
    invulnerabilityTime
    server.minTimeToAnswer
    server.maxTimeToAnswer
    server.infectedDelay
    server.workFrequency
    server.probInsideInfection
    fd.probInsideInfection
    probOutsideInfection