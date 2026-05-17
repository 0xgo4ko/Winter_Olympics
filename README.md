## CSCB869 Java Web Services

### Winter Olympics

The assignment is a Winter Olympics management app with two competition types:
    • Ski Slalom
        ◦ first run time
        ◦ determine second-run participants, for example best 30
        ◦ second run order is reverse by first-run time, slowest qualified starts first
        ◦ final time is run 1 + run 2
        ◦ DNF competitors are excluded from final ranking
    • Biathlon
        ◦ base skiing time
        ◦ shooting misses
        ◦ penalty time per miss, for example +60 seconds
        ◦ final time is base time + penalties
        ◦ DNF competitors are excluded from ranking

Core data and reports:
    • athletes: name, country, gender, date of birth
    • competitions separated by men/women
    • minimum age rule per competition
    • times measured to 3 decimal places
    • final rankings per competition
    • medal winners: 1st, 2nd, 3rd
    • medal count by country
    • average participant age
    • youngest and oldest medalist

User roles:
    • Athletes can manage their own athlete data and register for competitions.
    • Admins can manage competitions and participant performance/results.
    • Public users can view summarized data without login.

Technical requirements:
    • Spring Boot web app
    • RESTful services, database, and UI
    • CRUD for required data
    • validation
    • exception handling
    • tests
    • user management