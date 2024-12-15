import { useCallback, useEffect, useState } from "react";
import {
  deleteFile,
  detectFile,
  getDownloadUrl,
  getFiles,
  uploadFile,
} from "./http";
import { v4 as uuidv4 } from "uuid";
import Upload, { RcFile } from "antd/es/upload";
import { Anchor, Button, Form, Radio, Space } from "antd";
import s from "./App.module.scss";

interface FileModel {
  id: string;
  fileName: string;
}

function App() {
  const [files, setFiles] = useState<FileModel[]>([]);
  const [choosenValue, setChoosenValue] = useState<string | null>(null);

  const getFilesRequest = useCallback(
    () =>
      getFiles().then((response) => {
        const { data } = response;

        const updatedFiles: FileModel[] = data.map((fileName) => ({
          fileName,
          id: uuidv4(),
        }));

        setFiles(updatedFiles);
      }),
    []
  );

  useEffect(() => {
    getFilesRequest();
  }, [getFilesRequest]);

  const uploadImageToS3 = async (file: RcFile): Promise<string> => {
    await uploadFile(file).then(getFilesRequest);

    return "";
  };

  const onDetermineClickHandler = () => {
    const choosenFile = files.find(({ id }) => id === choosenValue);

    if (!choosenFile) {
      return;
    }

    detectFile(choosenFile.fileName).then(getFilesRequest);
  };

  const onDeleteClickHandler = () => {
    const choosenFile = files.find(({ id }) => id === choosenValue);

    if (!choosenFile) {
      return;
    }

    deleteFile(choosenFile.fileName).then(getFilesRequest);
  };

  return (
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
  );
}

export default App;
