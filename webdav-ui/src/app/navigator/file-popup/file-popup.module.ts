import {NgModule} from "@angular/core";
import {RouterOutlet} from "@angular/router";
import {CommonModule} from "@angular/common";

import {FilePopupDirective} from "./file-popup.directive";
import {FilePopupComponent} from "./file-popup.component";

@NgModule({
  imports: [
    CommonModule,
    RouterOutlet,

  ],
  providers: [],
  declarations: [
    FilePopupDirective,
    //FilePopupComponent
  ],
  exports: [
    FilePopupDirective
  ]
})
export class FilePopupModule {


}
