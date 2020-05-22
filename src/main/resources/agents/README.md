## Agent Properties

Agent properties are defined in _agentName_.properties

This file is of the format:
    
    property=value
    
These files are automatically loaded and the properties specified 
within them are input into the agent (to add a new agent the 
FaultDetector class must be changed to accept that type of agent)