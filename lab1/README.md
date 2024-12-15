# <p style="width: 100%; text-align: center">Первая лабораторная работа</p>

### Содержание

[1. Постановка задачи](#setTask)

[2. Решение](#decision)

## <a id="setTask" style="color: lightgrey">1. Постановка задачи</a>

### <p style="width: 100%; text-align: center">Реализация запуска приложения с использованием Docker</p>

- #### Пройти "Interactive Tutorial" по Docker
- #### Создать веб-сервис с помощью Spring Boot
- #### Создать Dockerfile и запустить приложение с помощью Docker
- #### Создать docker-compose.yaml и запустить несколько контейнеров с использованием docker-compose

## <a id="decision" style="color: lightgrey">2. Решение</a>

В рамках работы реализован простой "Counter", который при нажатии увеличивает значение

```java
@RestController
public class CounterController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/counter")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "{\"result\":" + counter.incrementAndGet() + "}";
    }
}
```

Для работы с Docker созданы два файла: Dockerfile и docker-compose.yaml

```docker
// Dockerfile

FROM openjdk:17-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```yaml
// docker-compose.yaml

services:
  web:
    build: .
    ports:
      - "9000:8080"
```
