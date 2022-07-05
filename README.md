## Описание ##
Это учебный проект REST-сервиса чат с комнатами.
#### Технологии проекта ####
![badge](https://img.shields.io/badge/PostgreSQL-12-blue)
![badge](https://img.shields.io/badge/Java-11-green)
![badge](https://img.shields.io/badge/Maven-3.6-green)
![badge](https://img.shields.io/badge/SpringBot-2.6-yellow)

## Запуск через Docker Compose
* Все команды выполнять в терминале
1. Скопировать проект 
```
git clone https://github.com/oryngalikarimzhan/job4j_chat
```
2. Перейти в корень проекта 
```
2. cd job4j_chat
```

3. 3.Cобрать приложение командой: 
```
mvn install -Dmaven.test.skip=true
```

4. 4.Собирать образ в docker 
```
docker build -t chat .
```


5. Запустить сервисы 
```
docker-compose up
```