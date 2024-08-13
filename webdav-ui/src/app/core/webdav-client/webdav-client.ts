import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from "@angular/common/http";

import {filter, map, Observable} from "rxjs";
import {Inject, Injectable, Injector, Optional} from "@angular/core";
import {FileInfo} from "./webdav.model";

class WebDavConnection {
  baseUrl: string = ""
  user: string = "";
  password: string = "";
}

@Injectable()
export class WebDavClient {
  baseUrl: string = "http://localhost:3505"
  user: string = "alekseisosnovskikh";
  password: string = "hqsfkdtmarovalyu";
  constructor(
    private http: HttpClient,
    @Optional()
    private cfg: WebDavConnection
  ) {
      console.log("cfg  ", cfg)
  }

  public propfind(path :string, depth: '1'| '0'): Observable<Array<FileInfo>> {


    //header.append('Destination', '${url}/newfile_directory');
    //header.append('Depth', '300');

    let xmlBody =
      "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
      " <D:propfind xmlns:D=\"DAV:\">\n" +
      "  <D:prop>\n";
      xmlBody +=
      "<D:creationdate/>\n" +
      "<D:displayname/>\n" +
      "<D:getcontentlength/>\n" +
      "<D:getcontenttype/>\n" +
      "<D:getetag/>\n" +
      "<D:getlastmodified/>\n" +
      "<D:resourcetype/>\n";

    xmlBody+=
      "  </D:prop>\n" +
      " </D:propfind>";

      return this.sendRequest({
          method: "PROPFIND",
          uri: path,
          contentType: 'text/xml',
          responseType: "text",
          headers: {
            'Depth': depth
          },
          body: xmlBody
      }).pipe(
        map((httpEvent ) => {

          const  httpResponse = httpEvent as HttpResponse<any>;
          const domParser = new DOMParser();
          const htmlElement = domParser.parseFromString(httpResponse.body, 'text/xml');
          let multistatus = htmlElement.firstElementChild;
          if (multistatus == null) return [];
          const listFiles:Array<any> = [];
          multistatus.childNodes.forEach((itNode) => {
            if (!itNode.nodeName.endsWith("response")) return;
            const nodeObject = this.toObject(itNode);
            nodeObject['current'] = nodeObject.href.replaceAll(/\/$/g, '') == path.replaceAll(/\/$/g, '')
            listFiles.push(nodeObject);
          })
          return listFiles.map(itFile => this.toFileInfo(itFile));

        })
      )

  }

  sendRequest(params: {method: string, uri: string, contentType?: string, responseType?: string, headers?: any, body: any}) {
    let headers = new HttpHeaders()
    if (this.cfg?.password && this.cfg?.user) {
      headers = headers.append('Authorization', 'Basic '  + btoa(this.cfg?.user + ':' + this.cfg?.password));
    }

    if (params.contentType) {
      headers = headers.append('Content-Type', params.contentType);
    }

    for (const key in params.headers) {
        headers = headers.append(key, params.headers[key]);
    }

    return this.http.request(new HttpRequest(params.method, (this.cfg?.baseUrl || '') + params.uri, params.body,  {
      responseType: "text",
      headers: headers,
    })).pipe(
        filter(httpEvent => httpEvent.type !== 0)
    )
  }
  toFileInfo(file: any): FileInfo {
/*
    creationdate      :      "2023-10-21T16:54:26+05:00"
    displayname      :      "25_17 - 2023 - Радость встреч и расставаний.tar.gz"
    getcontentlength      :      "101968225"
    getcontenttype      :      "application/gzip"
    getetag      :      ""

 */
    // noinspection JSUnresolvedReference
    return {
      href: file.href,
      status: file.propstat.status,
      contentLength:  parseInt(file.propstat.prop.getcontentlength || '0'),
      mimeType:  file.propstat.prop.getcontenttype || '',
      displayName:  file.propstat.prop.displayname,
      etag: file.propstat.prop.getetag,
      isFolder: file.propstat.prop.resourcetype?.collection != null,
      current: file.current || false,
      // status: file.propstat.prop.d
    } as unknown as FileInfo;
  }

  toObject(nodeChild: ChildNode): any {
    const res = {} as any;
    if (nodeChild.childNodes.length <= 1) {
      return nodeChild.textContent;
    }


    nodeChild.childNodes.forEach((itPropStat) => {
        if (itPropStat.nodeName.endsWith("#text")) return;

        let nodeName = itPropStat.nodeName.split(":")[1];
        if (itPropStat.childNodes.length <= 1) {
          res[nodeName] = itPropStat.textContent;
        } else {
          res[nodeName] = this.toObject(itPropStat);
        }

    });

    return res;

  }

  move(target: string, destination: string){

  }

  delete(target: string){

  }

  copy(target: string, destination: string){

  }

  public get(path: string): Observable<any> {
    let headers = new HttpHeaders()
    headers = headers.append('Authorization', 'Basic '  + btoa('ivanov:123456'))
  //  headers.append('Content-Type', 'text/xml')

    return this.http.request(new HttpRequest("GET", this.baseUrl + "/"+path,null, {headers: headers} ))
      .pipe(
        filter(httpEvent => httpEvent.type !== 0)
      )

  }
}
