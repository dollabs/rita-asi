# Step 1: Go to Code directory

# Step 2: Build image from ritaUI.Dockerfile:
    # docker build -t imageName -f docker/ritaUI.Dockerfile .
    # Where imageName can be anything you want such as rita-ui-app

#Step 3: Run the image locally:
    # docker run -p 3000:3000 imageName
    # example: 
        # docker run -p 3000:3000 rita-ui-app
        # docker run -p 5000:3000 rita-ui-app (here, the app is running on port 5000)

# Define from what base image we want to build from. 
# We will use Node version 12 available from the Docker Hub:
FROM node:12

# Set environment variables
ENV appDir /Rita_ui

# Create app directory
WORKDIR $appDir

# Install app dependencies
# COPY to ensure both package.json AND package-lock.json are copied
COPY rita_ui/package*.json ./ 
RUN npm install

# Bundle app source
COPY rita_ui/. .

EXPOSE 3000

CMD [ "npm", "start" ]