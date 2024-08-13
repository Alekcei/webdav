import {Component, Input, signal} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FileInfo} from "@webdav-client/webdav.model";
import {Router} from "@angular/router";

@Component({
  selector: 'left-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './left-panel.component.html',
  styleUrl: './left-panel.component.less'
})
export class LeftPanelComponent {
  @Input()
  current?: FileInfo;

  constructor(private route: Router) {

  }
  toHome() {
    if (!this.current?.isFolder){
      return;
    }
    if (this.current?.href == '/') {
      return;
    }
    this.route.navigateByUrl("/");

  }

  toUp() {
    if (!this.current?.isFolder){
      return;
    }
    if (this.current.href == '/') {
      return;
    }

    this.route.navigateByUrl(this.current.href.replaceAll(/\/[^/]*\/?$/g, ''))

  }
}
