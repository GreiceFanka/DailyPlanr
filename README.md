DailyPlanr

DailyPlanr is a task management application based on the Kanban style, offering a visual and intuitive approach to activity control. Users can easily register their tasks, assign priorities, add detailed descriptions, and set deadlines for each one. Through the interactive Kanban board, users can track the status of their tasks, moving them from column to column as they progress though the workflow.
This streamlined approach makes task management more efficient and organized, allowing the user to maintain full control over their activities and achieve their goals more productively.



Project status

Under construction.



Functionalities

-User registration: Allow users to register in the application using a username and password.

-Creating tasks: Allow users to create new daily tasks by providing fields for title, description and term.

-Task Prioritization: Allow users to assign priorities to daily tasks, indicating their importance or urgency.

-Definition of schedules: Allow users to assign specific times for their daily tasks.

-Notifications and reminders: Send notifications or reminders to users to remind them of daily tasks and designated times.

-Progress Tracking: Allow users to mark tasks as completed as they go that are accomplished, providing an overview of daily progress.

-Organization by categories: Allow users to organize their daily tasks into categories or tags, facilitating filtering and specific visualization of groups of tasks.

-Task History: Keep a historical record of completed daily tasks, allowing users to users review work done earlier.


Installation

All the necessary dependencies are inside the pom.xml file, you just need to configure your properties file and install your preferred SQL database.
Necessary settings in the .properties file:


Ex :

spring.datasource.url=jdbc:DBSQL://localhost:DBPORT/DBNAME

spring.datasource.username= your DB username

spring.datasource.password= your DB password

spring.jpa.properties.hibernate.jdbc.time_zone=Europe/Rome

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

server.port=8081





Environment variable required for email sending functionality

app.passwordmail = your email password


You will have to modify the Mail class with data from your email server.



Technologies

1.Back-end

Java 17 

Spring Boot 3.1.0

Spring JPA

Maven 4.0

Mysql

2.Front-end

Javascript

Jquery

CSS

HTML

Thymeleaf



Contributing people:

Greice Fanka 

