# <p style="width: 100%; text-align: center">Четвертая лабораторная работа</p>

### Содержание

[1. Постановка задачи](#setTask)

[2. Решение](#decision)

## <a id="setTask" style="color: lightgrey">1. Постановка задачи</a>

### <p style="width: 100%; text-align: center">Реализация запуска приложения в Kubernetes</p>

- #### Пройти "Interactive Tutorial" по Kubernetes/Minicube
- #### Создать yaml файлы для работы с Kubernetes
- #### Управлять развертыванием контейнеров с использованием kubectl

## <a id="decision" style="color: lightgrey">2. Решение</a>

Для работы было использовано веб-приложение, разработанное в рамках лабораторных работ №2, №3

Реализация работы заключалась в подключении двух файлов

- **service.yaml**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: test

spec:
  type: LoadBalancer
  selector:
    app: test
  ports:
    - protocol: TCP
      name: http-traffic
      port: 8080
      targetPort: 8080
```

- **deployment.yaml**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test

spec:
  replicas: 1
  selector:
    matchLabels:
      app: test
  template:
    metadata:
      labels:
        app: test
    spec:
      containers:
        - name: test
          image: kichnotna/test
          ports:
            - containerPort: 8080
```

Команды для работы:

- **запуск minicube**

```cmd
minikube start
```

- **остановка minicube**

```cmd
minikube stop
```

- **применение файлов**

```cmd
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

- **просмотр логов**

```cmd
kubectl logs <pod-name>
```

- **работа с локальными запросами**

```cmd
minikube tunnel
```
