# <p style="width: 100%; text-align: center">Третья лабораторная работа</p>

### Содержание

[1. Постановка задачи](#setTask)

[2. Решение](#decision)

<a href="#decision-server" style="margin-left: 15px">2.1. Серверная часть</a>

<a href="#decision-client" style="margin-left: 15px">2.2. Клиентская часть</a>

## <a id="setTask" style="color: lightgrey">1. Постановка задачи</a>

### <p style="width: 100%; text-align: center">Реализация веб-приложения с технологией computer-vision</p>

- #### Создать веб-приложение, позволяющее отобразить отобразить информацию об объектах на предоставляемом изображении

## <a id="decision" style="color: lightgrey">2. Решение</a>

### <a id="decision-server" style="color: lightgrey">2.1. Серверная часть</a>

За основу взято веб-приложение, разработанное в рамках лабораторной работы №2

Для корректной работы _computer-vision_ необходимо добавить новые параметры в соответствующий файл _*application.properties*_

```
...
detect-url=https://smarty.mail.ru/api/v1/objects/detect?oauth_token=UKcBvTA7jBxejrRQ2ZF5X33CZ4ZrQR2KcmYdtvFJB3hXp3Y69&oauth_provider=mcs
cv.meta-info={"mode": ["object2"],"images": [{"name": "file"}]}
```

Также, был реализован **_DetectController_**. В данном классе есть несколько методов:

- **detectOnImagу**: метод для распознания объектов на предоставленном изображении

```java
private Label detectOnImage(ByteArrayResource file) throws JsonProcessingException {
  var headers = new HttpHeaders();
  headers.setContentType(MediaType.MULTIPART_FORM_DATA);
  headers.setAccept(List.of(MediaType.APPLICATION_JSON));

  var map = new LinkedMultiValueMap<String, Object>();
  map.add("file", file);
  map.add("meta", this.meta);

  var request = new HttpEntity<>(map, headers);
  var response = restTemplate.postForEntity(
    this.url,
    request,
    String.class
  );

  var node = objectMapper.readTree(response.getBody());
  var labelData = node
    .get("body")
    .get("object_labels")
    .get(0)
    .get("labels")
    .get(0);

  return objectMapper.treeToValue(labelData, Label.class);
}
```

Для удобства, было принято решение об использовании и рисовании только наибольшей группы объектов, найденной на изображении

- **drawDetect**: метод для выделения найденных объектов

```java
private byte[] drawDetect(ByteArrayResource file, List<Integer> measures, String extension) throws IOException {
  var image = ImageIO.read(file.getInputStream());
  var graphics = image.createGraphics();

  graphics.setColor(Color.RED);
  graphics.setStroke(stroke);
  graphics.drawRect(
    measures.get(0),
    measures.get(1),
    measures.get(2),
    measures.get(3)
  );
  graphics.dispose();

  var outputStream = new ByteArrayOutputStream();
  ImageIO.write(image, extension, outputStream);

  return outputStream.toByteArray();
}
```

- **detectOnImage**: метод-endpoint для стороны клиента. GET запрос по URL /cv/detect/${fileName}

```java
public ResponseEntity<Void> detectOnImage(@PathVariable("fileName") String fileName) throws IOException {
  var file = this.bucketService.getFile(fileName);
  var extension = fileName.split("\\.")[1];
  var label = this.detectOnImage(file);
  var result = this.drawDetect(file, label.coordination(), extension);
  this.bucketService.uploadFile(result, this.imageName(label, extension));

  return ResponseEntity.accepted().build();
}
```

По итогу, после выполнения этого метода в bucket будет залит ещё один файл - копия переданного изображения, но с выделенными объектами и с новым названием (приписка - detected)

### <a id="decision-client" style="color: lightgrey">2.2. Клиентская часть</a>

За основу взято веб-приложение, разработанное в рамках лабораторной работы №2

Для отправки запроса реализован новый метод: **detectFile**

```TypeScript
export const detectFile = async (fileName: string) => {
  return await $api.get<void>(`${DEFAULT_URL}/cv/detect/${fileName}`);
};
```

Также, чуть-чуть изменился главный компонент. В нем появились radio-кнопки для того, чтобы была возможность выбора необходимого файла для удаления или распознавания

```TypeScript XML
<div className={s["app-container"]}>
  <h1 className={s["app-container__title"]}>Работа с S3</h1>
  <Form>
    <h1 className={s["app-container__text"]}>Файлы из хранилища</h1>
    <Radio.Group
      name="selected-file"
      className={s["app-container__radio-group"]}
      onChange={(e) => setChoosenValue(e.target.value)}
    >
      {files.map(({ fileName, id }) => {
        const hrefToDownload = getDownloadUrl(fileName);

        return (
          <Space key={id}>
            <Form.Item>
              <Anchor.Link
                title={fileName}
                href={hrefToDownload}
                className={s["app-container__file-name"]}
              />
            </Form.Item>
            <Form.Item>
              <Radio value={id} />
            </Form.Item>
          </Space>
        );
      })}
    </Radio.Group>
    <Space className={s["app-container__buttons"]}>
      <Upload multiple={false} action={uploadImageToS3}>
        <Button>Загрузить новый</Button>
      </Upload>
      <Button disabled={!choosenValue} onClick={onDeleteClickHandler}>
        Удалить
      </Button>
      <Button disabled={!choosenValue} onClick={onDetermineClickHandler}>
        Определить лица
      </Button>
    </Space>
  </Form>
</div>
```

После удаления, добавления и обнаружения снова отправляется запрос на получение всех файлов из хранилища
