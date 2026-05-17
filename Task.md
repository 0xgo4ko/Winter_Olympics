Project development assignment
Winter Olympics

Assignment
To implement an application for managing a Winter Olympics, in which two types of competitions are held: Ski Slalom and Biathlon. The system must allow registration of participants and competitions, entry of results and automatic calculation of rankings and medals. The application supports organizers in data processing and ensures transparency and accuracy in reporting results.

1. Athletes:
   id;
   Name;
   Country;
   Gender;
   Date of birth.

2. Competition Rules: Competitions are organized separately for men and women and there is a minimum age that a competitor must be in order to participate. Competitors' times are measured in seconds to the third decimal place.

Ski slalom
Each competitor participates in the first run.
After the completion of the first run, the participants in the second run are determined (for example, the first 30 in time).
Only the ranked competitors participate in the second round. The order of their descent is according to the time of the first run. Those who are slowest in the first run start first.
The final classification is determined by the sum of the times of the two heats.
The competitor with the lowest total time is the winner.
If a competitor does not finish a race, he does not participate in the final ranking.
To realize:
1. Entering and storing time from the first run;
2. Sorting and determining the participants in the second run;
3. Entering time from the second run;
4. Automatic calculation of final ranking;
5. Output ranking.

Biathlon
Each competitor completes a certain number of laps.
After certain laps there is a shootout (eg 2 or 4 shots).
For each miss, a penalty is charged: add penalty time (eg +1 minute per miss).
A competitor who does not finish does not qualify.
To realize:
1. Entering time for cross-country skiing;
2. Entering the number of misses when shooting;
3. Calculation of penalties;
4. Calculation of final time (basic time + penalties);
5. Ranking by best finishing time.

Olympics. To realize:
1. Keeping information about the competitions that take place at the Olympics;
2. Final rankings, according to competitions;
3. Determination of medal winners (1st, 2nd, 3rd place);
4. Number of medals by country;
5. Average age of participants;
6. Youngest/Oldest Medalist

Users and rights in the system
1. Athletes - enter, edit and delete data about themselves and register for
   participation in a competition;
2. System administrators - enter and edit the data for the competitions and the performance of the participants;
3. The summarized data (ranking, number of medals by country, etc.) during the competitions are publicly available, no login is required.

Technological requirements
To develop a web application with Spring Boot based on RESTful web services with database and user interface. The application must provide input capability,
edit, delete and display the data described in the requirements. It is necessary to implement validation of the data being worked with, a management mechanism
exceptions, testing and user management.