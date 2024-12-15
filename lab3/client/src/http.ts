import { RcFile } from "antd/es/upload";
import axios, { AxiosRequestConfig } from "axios";

const DEFAULT_URL = "http://192.168.0.17:8080";

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

export const detectFile = async (fileName: string) => {
  return await $api.get<void>(`${DEFAULT_URL}/cv/detect/${fileName}`);
};

export const deleteFile = async (fileName: string) => {
  return await $api.delete<void>(`/s3/files/${fileName}`);
};
