import {Observable} from "rxjs";


interface WebdavModel {

  listFiles(path: String): Observable<Array<FileInfo>>;
  delete(path: String): void;
  move(path: String, destination: String): void;
}

export interface FileInfo {
    href: string;
    creationDate: Date;
    displayName: string;
    status: string;
    contentLength: number;
    mimeType: string;
    etag: string;
    isFolder: boolean
    current: boolean
}
