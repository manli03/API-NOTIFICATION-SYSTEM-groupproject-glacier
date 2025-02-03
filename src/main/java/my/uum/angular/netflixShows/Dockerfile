FROM node:18

# Install Angular CLI
RUN npm install -g @angular/cli@latest

# Set the working directory to /app
WORKDIR /app

# Copy project files
COPY . /app

# Install dependencies
RUN npm install

# Build the Angular application
RUN ng build --configuration production

# Serve the Angular application
CMD ["ng", "serve", "--host", "0.0.0.0"]
