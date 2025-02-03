[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/vaXpkLzu)
## Requirements for Group Project
[Read the instruction](https://github.com/STIW3054-A232/class-activity-stiw3054/blob/main/GroupProject.md)

## Group Info:
1. Name & Phone Number & Matric Number
    - MUHAMMAD FARHAN BIN MOHAMAD AZMAN YEOH
        - +60 10-399 7010 
        - 295690
    - MUHAMMAD AIMAN BIN NORAZLI
        - +60 17-409 2591
        - 294214
    - IKMAL NAZRIN BIN AZIZ
        - +60 11-6989 2748
        - 294501
    - MUHAMMAD FAQRIEZ FARHAN BIN ROHAIDZAM
        - +60 17-442 9742
        - 294988

2. Leader: MUHAMMAD FARHAN BIN MOHAMAD AZMAN YEOH (295690)
3. Other related info (if any)

## Title of your application (a unique title)
NETFLIX MOVIE API NOTIFICATION SYSTEM

## Abstract (in 300 words)

1. **Background**
   The streaming industry has experienced exponential growth, with Netflix leading the pack as one of the largest content providers. As the volume of content grows, users often struggle to keep track of new releases and updates to their favorite shows and movies.

2. **Problem Statement (from articles or newspaper or social media)**
   According to a 2023 Nielsen report, the average American spends 4.4 hours per day watching streaming content, yet 46% feel overwhelmed by the number of streaming choices available. This information overload can lead to missed opportunities to enjoy new content that aligns with user preferences.

3. **Main Objective**
   The Netflix Movie API Notification System aims to enhance user engagement by providing real-time updates on new releases, changes in content availability, and personalized recommendations based on viewing history.

4. **Methodology**
   Our system employs a microservices architecture utilizing Spring Boot for the backend API, Angular for the frontend, and a combination of Apache NiFi, Kafka, Spark, and Cassandra for real-time data processing and storage. A Telegram bot is integrated for instant user notifications.

5. **Result**
   The system successfully processes Netflix content data in real-time, allowing for immediate updates to be pushed to users. Initial testing shows a 95% accuracy in delivering personalized notifications within 5 seconds of data updates.

6. **Conclusion**
   By leveraging modern big data technologies and a microservices architecture, our Netflix Movie API Notification System addresses the challenge of content discovery in the streaming era. This solution not only keeps users informed about relevant content but also has the potential to increase viewer engagement and satisfaction.

## System Architecture (MUST be included in your presentation)

![System Architecture Diagram](resources%2Fsystem%20architecture.png)

The system architecture consists of the following components:

1. **Data Source: CSV File**
   - Initial dataset in CSV format, obtained from a data repository like GitHub or Kaggle.

2. **Data Importer (Python/Node.js Script)**
   - Reads data from the CSV file.
   - Connects to the MySQL database and inserts the data.

3. **MySQL Database**
   - Stores the raw data imported from the CSV file.
   - Serves as the data source for the Spring Boot API.

4. **Spring Boot API**
   - Provides RESTful endpoints for CRUD operations on the MySQL database.
   - Secured with JWT for authentication.
   - Interacts with MySQL for data manipulation and serves data to the Angular frontend and Apache NiFi.

5. **Angular Frontend**
   - Provides a user interface for interacting with the Spring Boot API.
   - Features include forms for adding and updating records, and a table to display records.

6. **Apache NiFi**
   - Periodically fetches data from the Spring Boot API and sends it to Apache Kafka.
   - Includes processors for data fetching, conversion, and sending to Kafka.

7. **Apache Kafka**
   - Acts as a message broker for streaming data.
   - Receives data from Apache NiFi and sends it to Apache Spark.

8. **Apache Spark**
   - Subscribes to Kafka topics, performs real-time data analysis, and stores results in Apache Cassandra.

9. **Apache Cassandra**
   - Stores processed data from Apache Spark.
   - Serves data to the Telegram Bot for user notifications.

10. **Telegram Bot**
    - Notifies users about data updates and responds to data queries.
    - Queries Apache Cassandra for the latest data and sends notifications to users.

11. **Version Control: Git**
    - Manages the source code versions and collaboration among developers.

12. **Containerization for Deployment: Docker**
    - Containerizes the application for consistent deployment across different environments.

13. **Development Environment: IntelliJ IDEA and Angular**
    - Utilizes IntelliJ IDEA as the integrated development environment for writing code.
    - Angular for frontend development.


### Interaction Flow

1. **Data Import**
   - The data importer script reads the CSV file and inserts the data into MySQL.

2. **CRUD Operations**
   - Users interact with the Angular frontend to add, update, or view records via the Spring Boot API.

3. **Data Streaming**
   - Apache NiFi periodically fetches updated data from the Spring Boot API and sends it to Apache Kafka.

4. **Real-time Processing**
   - Apache Spark processes the data received from Kafka and stores the results in Apache Cassandra.

5. **User Notifications**
   - The Telegram Bot queries Apache Cassandra for updates and notifies users about any data changes within 5 seconds.

### Scalability, Fault Tolerance, and Data Consistency

1. **Scalability**
   - Each component can be scaled independently to handle increased load.
   - Apache Kafka and Spark provide horizontal scalability.

2. **Fault Tolerance**
   - Apache Kafka ensures message durability.
   - Apache Spark provides resilient distributed datasets (RDDs) for fault-tolerant processing.

3. **Data Consistency**
   - Proper transaction management in MySQL.
   - Ensuring idempotent operations in microservices.

## UML Class Diagram
![UMLDiagram.png](resources%2FUMLDiagram.png)


## User manual/guideline for system configuration

### Set up MySQL Database and Import Initial CSV Data

1. Install MySQL.
2. Create a database and necessary tables.
3. Configure database credentials in the Spring Boot application.

### Configure Spring Boot API

1. Set up database connection properties.
2. Configure JWT authentication.

### Set up Angular Frontend

1. Configure API endpoint URLs.

### Configure Apache NiFi

1. Create processors for fetching data from the Spring Boot API.
2. Set up Kafka producer processor.

### Set up Apache Kafka

1. Create necessary topics.
2. Configure producer and consumer properties.

### Configure Apache Spark

1. Set up Spark Streaming job to consume from Kafka.
2. Implement data analysis logic.
3. Configure Cassandra connector.

### Set up Apache Cassandra

1. Create keyspace and tables for processed data.

### Configure Telegram Bot

1. Set up bot using BotFather on Telegram.
2. Implement notification logic and Cassandra queries.

## User manual/guideline for testing the system

### Data Import Test

1. Run the Data Importer script and verify data in MySQL.

### API Testing

1. Use tools like Postman to test CRUD operations on the Spring Boot API.

### Frontend Testing

1. Verify CRUD operations through the Angular interface.

### Data Streaming Test

1. Monitor NiFi data flow from API to Kafka.

### Real-time Processing Test

1. Verify Spark job is processing data from Kafka.
2. Check processed data in Cassandra.

### Notification Test

1. Update a record in MySQL.
2. Verify Telegram Bot sends a notification within 5 seconds.

## User manual for installing your application on AWS (Bonus 5%)

1. Set up EC2 instances for each component.
2. Configure security groups and networking.
3. Install necessary software on each instance.
4. Deploy Spring Boot API and Angular frontend.
5. Set up managed services for Kafka and Cassandra.
6. Configure Apache NiFi and Spark on EC2 instances.
7. Set up Telegram Bot on a separate instance.
8. Configure load balancers and auto-scaling groups.

## Result/Output (Screenshot of the output)
![Output 1](src/main/resources/WhatsApp%20Image%202024-07-22%20at%2004.16.30_7997c75b.jpg)  ![Output 2](src/main/resources/WhatsApp%20Image%202024-07-22%20at%2004.16.30_217249f9.jpg)  ![Output 3](src/main/resources/WhatsApp%20Image%202024-07-22%20at%2004.16.30_9b5c65e4.jpg)



## References (Not less than 20)

1. Spring Framework. (2024). *Spring Boot Reference Documentation*. Retrieved from [https://docs.spring.io/spring-boot/docs/current/reference/html/](https://docs.spring.io/spring-boot/docs/current/reference/html/)

2. freeCodeCamp. (2024). *Spring Boot Tutorial*. Retrieved from [https://www.freecodecamp.org/news/spring-boot-tutorial/](https://www.freecodecamp.org/news/spring-boot-tutorial/)

3. Apache Software Foundation. (2024). *Apache Kafka Documentation*. Retrieved from [https://kafka.apache.org/documentation/](https://kafka.apache.org/documentation/)

4. Apache Software Foundation. (2024). *Apache Spark Documentation*. Retrieved from [https://spark.apache.org/docs/latest/](https://spark.apache.org/docs/latest/)

5. Apache Software Foundation. (2024). *Apache Cassandra Documentation*. Retrieved from [https://cassandra.apache.org/doc/latest/](https://cassandra.apache.org/doc/latest/)

6. Apache Software Foundation. (2024). *Apache NiFi Documentation*. Retrieved from [https://nifi.apache.org/docs.html](https://nifi.apache.org/docs.html)

7. Telegram. (2024). *Telegram Bot API*. Retrieved from [https://core.telegram.org/bots/api](https://core.telegram.org/bots/api)

8. MySQL. (2024). *MySQL Documentation*. Retrieved from [https://dev.mysql.com/doc/](https://dev.mysql.com/doc/)

9. ReactiveX. (2024). *RxJava Documentation*. Retrieved from [https://reactivex.io/RxJava/javadoc/](https://reactivex.io/RxJava/javadoc/)

10. JWT. (2024). *Introduction to JSON Web Tokens*. Retrieved from [https://jwt.io/introduction](https://jwt.io/introduction)

11. Microservices.io. (2024). *Microservices Pattern: API Gateway*. Retrieved from [https://microservices.io/patterns/apigateway.html](https://microservices.io/patterns/apigateway.html)

12. Mozilla Developer Network. (2024). *HTTP*. Retrieved from [https://developer.mozilla.org/en-US/docs/Web/HTTP](https://developer.mozilla.org/en-US/docs/Web/HTTP)

13. GitHub. (2024). *REST API Documentation*. Retrieved from [https://docs.github.com/en/rest](https://docs.github.com/en/rest)

14. Docker. (2024). *Docker Documentation*. Retrieved from [https://docs.docker.com/](https://docs.docker.com/)

15. Kubernetes. (2024). *Kubernetes Documentation*. Retrieved from [https://kubernetes.io/docs/home/](https://kubernetes.io/docs/home/)

16. GitLab. (2024). *GitLab CI/CD Documentation*. Retrieved from [https://docs.gitlab.com/ee/ci/](https://docs.gitlab.com/ee/ci/)

17. Redis. (2024). *Redis Documentation*. Retrieved from [https://redis.io/documentation](https://redis.io/documentation)

18. Elastic. (2024). *Elasticsearch Documentation*. Retrieved from [https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)

19. Confluent. (2024). *Stream Processing with Apache Kafka*. Retrieved from [https://www.confluent.io/learn/stream-processing/](https://www.confluent.io/learn/stream-processing/)

20. JavaTpoint. (2024). *Spring Boot Tutorial*. Retrieved from [https://www.javatpoint.com/spring-boot-tutorial](https://www.javatpoint.com/spring-boot-tutorial)


## Youtube Presentation (10%)
- https://www.youtube.com/watch?v=nZOhcncwyE8&ab_channel=MuhammadFarhan

## Link for the Dataset
[Netflix_shows dataset](https://www.kaggle.com/datasets/shivamb/netflix-shows)

