# RITA Control Panel

### Step to run the Control Panel locally:

1. Make sure you have NodeJS and NPM installed: <br> [How to install NodeJS and NPM using Brew](https://dyclassroom.com/howto-mac/how-to-install-nodejs-and-npm-on-mac-using-homebrew)
2. Make sure you have RabbitMQ installed and running in the background. To check RabbitMQ status, type the following command in your terminal: `rabbitmqctl status`.
3. After NodeJS and rabbitMQ are set up properly, open a terminal and:
   - cd to `RITA_UI` directory
   - Do a `npm install`. This command will install all needed dependencies automatically.
   - Do a `npm start` or `node server.js`

The UI webpage is up and running at `http://localhost:3000`. You can now run RITA components and start the rmq-player to see results (either via terminal commands or RITA Controller tool).

### Folders / Files breakdown

1. server.js file: Where it all starts
   - connects to rabbitMQ
   - connects to Mongo DB
   - starts Express app
   - starts SocketIO (backend)
2. app.js file: Express app (a Node.js web app framework).
3. routes folder: connects the viewController (in controllers folder) to the correct web addresses.
4. controllers folder:
   - Backend:
     - componentDBController.js: renders views which displays the JSON format of RITA components (ex: Genesis, SE, PG);
     - viewsController.js: renders all UI views (non-JSON related views)
     - errorController.js: provides a Global Error Handler functionality
   - Frontend (include front-end socketIO):
     - communicates/exchanges data with the backend socketIO.
     - changes UIs dynamically based on incoming data.
5. rabbitMQ folder:
   - receives messages from the testbed, prediction generator, and state estimator.
   - filters out unwanted messages and digests messages (if needed). Then sends processed messages to other parts of the webpage system (ex: SocketIO) using Node.js Event Emitter.
6. socketIO (back-end socketIO) folder:
   - receives processed messages/data from RabbitMQ.
   - digests those messages.
   - sends signals and messages to the frontend controllers.
7. models/backend folder: currently has only ritaComponentModel.js which contains a MongoDB schema model for RITA components (UI display).
8. public folder: includes all front-end materials such as CSS files and images.
9. views folder: includes PUG files (similar to HTML but cleaner and easier to use).
10. utils folder: includes supporting classes and functions.
