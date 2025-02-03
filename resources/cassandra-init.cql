CREATE KEYSPACE IF NOT EXISTS sparkdata WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
USE sparkdata;
drop table cust_data;
CREATE TABLE IF NOT EXISTS cust_data (
    show_id text PRIMARY KEY,
    cast text,
    country text,
    date_added text,
    description text,
    director text,
    duration text,
    listed_in text,
    rating text,
    release_year text,
    title text,
    type text,
    logg text
);
select * from cust_data;