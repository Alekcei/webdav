import {NgModule} from "@angular/core";

import {FileItmComponent} from "./file-item/file-item.component";
import {CommonModule} from "@angular/common";
import {RouterOutlet} from "@angular/router";
import {LeftPanelComponent} from "./left-panel2/left-panel.component";
import {HttpClientModule} from "@angular/common/http";
import {WebDavClient, WebDavConnection} from "../core/webdav-client/webdav-client";
import {NavigatorComponent} from "./navigator.component";
import {FilePopupModule} from "./file-popup/file-popup.module";

@NgModule({
  imports: [
    CommonModule,
    RouterOutlet,
    LeftPanelComponent,
    HttpClientModule,

    FilePopupModule
  ],
  providers: [
    { provide: WebDavConnection, useValue: { baseUrl: "http://localhost:8080"} },
    { provide: WebDavClient, useClass: WebDavClient }
  ],
  declarations: [NavigatorComponent, FileItmComponent],
})
export class NavigatorModule { }
