# Genesis Core

The Genesis Core is a trigger-response system. It enables Genesis to summon its mental experts to solve the problems posted by the environment, a human, or its own introspection process.

The trigger is any text input into Genesis system. It can be an instruction, a question, or a story. It's similar to the streams of information acquired by our perception system.

The response is the sequence of information flow within and among selected Genesis Mental Experts that are associated with the trigger.

The association between the trigger and response can be learned and modified through experience.

Trigger  | Response  |  Note
--|---|--
Genesis reads one story that has a similar pattern as another story  |  Generate concept patterns, inference rules, or procedural knowledge |  At any time, Genesis may have several active Mental Experts running for acquiring knowledge, inspecting moral behavior, or documenting its day
Human asks questions about stories |  Depending on the type of questions asked, process the story with different mental experts  |  The type of a question can be detected by keywords such as "What if" (hypothetical reasoning), "Summarize" (story summary), and "Compare" (story similarity)
Human asks Genesis to perform some tests  |  Depending on the domain of task instructed, decompose the tasks with different sets of domain knowledge | The domain of task can be determined by the objects or goals involved. Common domains include robot assembly, web browsing, memory manipulation, and graphic design



## Input from Perception Interfaces

Genesis perception interfaces pass the output of perception systems to Genesis Core for reasoning. On the Genesis System Interface, each perception interface takes the form of a text input box for easier testing.

The following perception systems may be integrated.

Perception System | Input | Example
--|--|--
Speech Recognition  |  Chatting inputs from human actors.  |  Alice started speaking. I am hungry. Do you know where is the cook book written by Bob?
Visual Recognition  |  Objective descriptions of the world and human actors. The input may come from a combination of vision systems, such as by `Attribute detector`, `Relation detector`, `Face detector`, and `Action detector`.  |   Alice walked into the room. She wears a white shirt. She stopped besides the book shelf
Text Recognition  |  Texts from a variety of sources, e.g., stories, articles, contents on webpages. The input may come from a corpora or a web scrapper.  |  Alice went into wonderland ...
