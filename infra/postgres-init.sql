-- Runs once when the postgres container's data volume is first initialized
-- (mounted into /docker-entrypoint-initdb.d/). One database, one schema per
-- service - each service's own Flyway migration creates its schema (via
-- spring.flyway.create-schemas=true) and its tables inside it on startup, so
-- this script only needs to create the shared database itself.
CREATE DATABASE hostel_platform;
