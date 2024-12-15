# <p style="width: 100%; text-align: center">Вторая лабораторная работа</p>

### Содержание

[1. Постановка задачи](#setTask)

[2. Решение](#decision)

<a href="#decision-server" style="margin-left: 15px">2.1. Серверная часть</a>

<a href="#decision-client" style="margin-left: 15px">2.2. Клиентская часть</a>

## <a id="setTask" style="color: lightgrey">1. Постановка задачи</a>

### <p style="width: 100%; text-align: center">Реализация веб-приложения с использованием протокола S3</p>

- #### Создать ресурс, поддерживающий протокол S3 у любого облачного провайдера
- #### Реализовать веб-приложение, которое позволяет работать с bucket и его данными

## <a id="decision" style="color: lightgrey">2. Решение</a>

Для работы выбран VK Cloud - платформа с широким набором облачных сервисов для разработки и работы с данными

### <a id="decision-server" style="color: lightgrey">2.1. Серверная часть</a>

Серверная часть реализована с помощью Spring Boot.
Для работы с VK Cloud необходимы введены некоторые параметры (ключ доступа, секретный ключ, ендпоинт, регион и название bucket), которые записаны в отдельном файле: **_application.properties_**

```
spring.cloud.aws.credentials.access-key=gAe6ZA8ugbTBZxx7J2HZqB
spring.cloud.aws.credentials.secret-key=3nwe7FU7E9dVS59r9P1nagWX4qSvPD6hz3gJMwgC3WTz
spring.cloud.aws.endpoint=https://hb.ru-msk.vkcloud-storage.ru
spring.cloud.aws.region.static=ru-msk

bucket-name=s3-test-bucket
```

Для непосредственной работы с bucket был создан класс: **_BucketService_**, который содержит следующие методы:

- **getListFiles**: метод для получения наименований файлов из "бакета"

```java
public List<String> getListFiles() {
  var request = ListObjectsV2Request
    .builder()
    .bucket(this.bucketName)
    .build();
  var response = this.client.listObjectsV2Paginator(request);

  return response
    .stream()
    .map(ListObjectsV2Response::contents)
    .flatMap(List::stream)
    .map(S3Object::key)
    .toList();
}
```

- **getFile**: метод для получения файлов из "бакета" в формате byteArray

```java
public ByteArrayResource getFile(String key) throws IOException {
  var request = GetObjectRequest
    .builder()
    .bucket(this.bucketName)
    .key(key)
    .build();

  var responseInputStream = client.getObject(request);

  return new ByteArrayResource(IoUtils.toByteArray(responseInputStream)) {
    @Override
    public String getFilename() {
      return key;
    }
  };
}
```

- **uploadFile**: метод для загрузки файла в "бакет"

```java
public void uploadFile(byte[] file, String fileName) throws IOException {
  var request = PutObjectRequest.builder()
    .bucket(this.bucketName)
    .key(fileName)
    .build();

  this.client.putObject(request, RequestBody.fromBytes(file));
}
```

- **deleteFile**: метод для удаления файла из "бакета" по его названию

```java
public void deleteFile(String fileName) {
  var request = DeleteObjectRequest.builder()
    .bucket(this.bucketName)
    .key(fileName)
    .build();

  this.client.deleteObject(request);
}
```

Для "общения" со стороной клиента реализован **_FilesController_**, который содержит почти идентичные методы:

- **getListFiles**: GET запрос по URL s3/files

```java
@GetMapping()
public ResponseEntity<List<String>> getListFiles() {
  return ResponseEntity.ok(this.bucketService.getListFiles());
}
```

- **getFile**: GET запрос по URL s3/files/${fileName}

```java
@GetMapping("/{fileName}")
public ResponseEntity<Resource> getFile(@PathVariable("fileName") String fileName) throws IOException {
  var byteArray = this.bucketService.getFile(fileName);

  var headers = new HttpHeaders();
  headers.add(
    HttpHeaders.CONTENT_DISPOSITION,
    String.format("attachment; filename=\"%s\"", fileName)
  );

  return ResponseEntity.ok()
    .headers(headers)
    .contentLength(byteArray.contentLength())
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .body(byteArray);
}
```

- **deleteFile**: DELETE запрос по URL s3/files/{fileName}

```java
@DeleteMapping("/{fileName}")
public ResponseEntity<Void> deleteFile(@PathVariable("fileName") String fileName) {
  this.bucketService.deleteFile(fileName);

  return ResponseEntity.accepted().build();
}
```

- **uploadFile**: POST запрос по URL s3/files/upload

```java
@PostMapping("/upload")
public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
  this.bucketService.uploadFile(file);

  return ResponseEntity.accepted().build();
}
```

### <a id="decision-client" style="color: lightgrey">2.2. Клиентская часть</a>

Клиентская часть реализована с помощью библиотеки react. Также, для удобной работы были использованы следующие библиотеки:

- antd: набор UI-компонентов
- axios: удобная работа с асинхронными запросами к серверу
- sass: препроцессор для стилей
- uuid: библиотека для генерации uuid
- typescipt: работа с типами

Все настройки для запросов и сами запросы к серверу описаны в отдельном файле **_http.ts_**

```TypeScript
const $api = axios.create({ baseURL: DEFAULT_URL });

export const getFiles = async () => {
  return await $api.get<string[]>("/s3/files");
};

export const getDownloadUrl = (fileName: string) => {
  return `${DEFAULT_URL}/s3/files/${fileName}`;
};

export const uploadFile = async (file: RcFile) => {
  const config: AxiosRequestConfig = {
    headers: { "Content-Type": "multipart/form-data" },
  };

  const formData = new FormData();

  formData.append("file", file);

  return await $api.post("/s3/files/upload", formData, config);
};

export const deleteFile = async (fileName: string) => {
  return await $api.delete<void>(`/s3/files/${fileName}`);
};
```

$api - настроенный instance axios (из используемых настроек только baseUrl)

Методы:

- **getFiles**: асинхронный метод, который возвращает запрос к серверу на получение всех наименований файлов в bucket
- **getDownloadUrl**: синхронный метод, который по fileName создает URL запроса на скачивание определенного файла
- **uploadFile**: асинхронный метод, который возвращает POST запрос с телом в виде FormData
- **deleteFile**: асинхронный метод на удаление файла из bucket по его названию

TSX вид компонента выглядит следующим образом:

```TypeScript XML
<div className={s["app-container"]}>
  <h1 className={s["app-container__title"]}>Работа с S3</h1>
  <Form>
    <h1 className={s["app-container__text"]}>Файлы из хранилища</h1>
    {files.map(({ fileName, id }) => {
      const hrefToDownload = getDownloadUrl(fileName);

      return (
        <Form.Item key={id}>
          <Anchor.Link
            title={fileName}
            href={hrefToDownload}
            className={s["app-container__file-name"]}
          />
        </Form.Item>
      );
    })}
    <Space className={s["app-container__buttons"]}>
      <Upload multiple={false} action={uploadImageToS3}>
        <Button>Загрузить новый</Button>
      </Upload>
    </Space>
  </Form>
</div>
```
