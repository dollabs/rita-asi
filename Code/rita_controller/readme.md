# RITA Controller

## Step to run the Controller locally:

1. Make sure you have NodeJS and NPM installed: <br> [How to install NodeJS and NPM using Brew](https://dyclassroom.com/howto-mac/how-to-install-nodejs-and-npm-on-mac-using-homebrew)
2. Make sure you have RabbitMQ installed and running in the background. To check RabbitMQ status, type the following command in your terminal: `rabbitmqctl status`.
3. After NodeJS and rabbitMQ are set up properly, open a terminal and:
   - cd to `Code/rita_controller` directory
   - Do a `npm install`. This command will install all needed dependencies automatically.
   - Do a `npm start` or `node server.js`

The webpage is up and running at `http://localhost:8080`.

## Usages

### Pull the latest version: `Git Pull`

- if encounter login issue, use: https://itsmycode.com/solved-remote-bitbucket-cloud-recently-stopped-supporting-account-passwords-for-git-authentication/

### The Complete build

1. Click `Build the Logger and the Player`.
2. Click `Compile The Mission Model (for SE)`. We can ignore this step if you run only MIT components.
3. Click `Build ALL Components`.
4. Click `Run ALL Components (DOLL & MIT)`.
5. After all components are built and started properly, we can now set up and run the logger and the player:
   - Give the output file a name and select `Save output (Logger)`. The output file is located in the `rita_controller/public/data/output` directory.
   - Set the speed for the player and click `Set player speed`. The default is 10x speed. _Warning: higher speed might cause time lag errors._
   - Locate the HSR file and click `Select the file`.
   - Run Experiment Control (check EC for more information). We can ignore this step if you run only MIT components. _Warning: to run the EC, we need exp-00x folder and HSR files exist in the `rita_controller/public/data/ECdata` directory._
   - The last step is to run the RMQ player and collect results.

### Build and Run Each Component Separately (Good for testing)

1. Click `Build the Logger and the Player`.
2. Click `Compile The Mission Model (for SE)`. We can ignore this step if you run only MIT components.
3. The following steps are unordered:

   - Click `Build ALL DOLL Components` or build a specific component.
   - After components are successfully built, we can run them separately.
   - You can also stop each component separately

4. Set up and run the logger and the player:
   - Give the output file a name and select `Save output (Logger)`. The output file is located in the `rita_controller/public/data/output` directory.
   - Set the speed for the player and click `Set player speed`. The default is 10x speed. _Warning: higher speed might cause time lag errors._
   - Locate the HSR file and click `Select the file`.
   - Run Experiment Control (check EC for more information). We can ignore this step if you run only MIT components. _Warning: to run the EC, we need exp-00x folder and HSR files exist in the `rita_controller/public/data/ECdata` directory._
   - The last step is to run the RMQ player and collect results.

## Optional: you can use the RITA UI in parallel to see predictions and the accuracy level of each run.
