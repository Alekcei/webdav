import { Component } from '@angular/core';
import {APP_BASE_HREF, CommonModule} from '@angular/common';
import {RouterModule, RouterOutlet} from '@angular/router';

import {HttpClientModule} from "@angular/common/http";

import {NavigatorComponent} from "./navigator/navigator.component";
import {WebDavClient} from "./core/webdav-client/webdav-client";
import {NavigatorModule} from "./navigator/navigator.module";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    NavigatorModule,
    HttpClientModule,
    RouterModule,
 //   RouterModule.forChild( [{path:'**', component:NavigatorComponent}] )
  ],
  providers: [
    { provide: APP_BASE_HREF, useValue: '/ui/' },
    { provide: WebDavClient, useClass: WebDavClient },
  ],
  templateUrl: './app.component.html',
  styleUrl: './navigator.component.less'
})
export class AppComponent {

}
