# Spring Security Course

---
Docker configuration:

Download [mysql](https://hub.docker.com/_/mysql) image
```bash
docker pull mysql
```

---

Container creation
```bash
docker run -d --name my-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 -e MYSQL_DATABASE=myapp mysql:latest
```

---

Access the MySQL Console
```bash
docker exec -it my-mysql mysql -uroot -p

docker exec -it my-mysql mysql -uroot -p1234 myapp
```

another way
```bash
docker exec -it my-mysql sh
mysql -u root -p
```
---

Enter to the database
```sql
SHOW DATABASES;
USE myapp;
SHOW TABLES;
-- \q exit
```

video reference [here]()