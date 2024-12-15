import { useCallback, useEffect, useState } from "react";
import { getDownloadUrl, getFiles, uploadFile } from "./http";
import Upload, { RcFile } from "antd/es/upload";
import { Anchor, Button, Form, Space } from "antd";
import { v4 as uuidv4 } from "uuid";
import s from "./App.module.scss";

interface FileModel {
  id: string;
  fileName: string;
}

function App() {
  const [files, setFiles] = useState<FileModel[]>([]);

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

  return (
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
  );
}

export default App;
