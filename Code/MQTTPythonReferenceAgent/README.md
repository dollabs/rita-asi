# Reference Agent Instructions

## Building Docker container
- Navigate to the root (top level) of the MQTTPythonReferenceAgent directory, this is the same level that the dockerfile appears in
- Open a shell window and run *docker build -t reference_agent .*

## Start and Stopping the Agent

- The reference agent will start and stop when start and stop messages are sent from the MalmoControl front end.

## Configuration
- You must configure the MQTT Host before using the container. THis can be done in the Local/ReferenceAgent/ConfigFolder/config.json file. In the field labeled "host", enter the IP address of the machine running the ELk Stack in quotes. 