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

select u.username, r.role_name, p.name as permission_name
from users u
         inner join user_roles ur on u.id = ur.user_id
         inner join roles r on ur.role_id = r.id
         inner join role_permissions rp on r.id = rp.role_id
         inner join permissions p on rp.permission_id = p.id;
```

video reference [here]()