import {Component, ElementRef, HostBinding, HostListener, Input, OnInit, ViewChild} from "@angular/core";
import {FileInfo} from "../../core/webdav-client/webdav.model";
import {Router} from "@angular/router";

@Component({
  selector: 'file-item',
  templateUrl: './file-item.component.html',
  styleUrl: './file-item.component.less'
})
export class FileItmComponent implements OnInit {
  title = 'webdav';

  @Input()
  fileInfo!: FileInfo;

  @Input()
  baseUrl: string | undefined;

  @ViewChild('preview', { read: ElementRef, static: true })
  preview!: ElementRef;

  @HostBinding('class.focus')
  focus?: boolean;

  constructor(private route: Router) {

  }

  ngOnInit(): void {
    let iconRe = (this.baseUrl||'') + "/mimetype" + '?icon-file' + "&mimetype=" + (this.fileInfo.isFolder? 'folder': encodeURIComponent(this.fileInfo.mimeType));

    this.preview.nativeElement.src=iconRe;
  }
;


  @HostListener("dblclick")
  dblclick(){
    if (this.fileInfo.isFolder) {
        this.route.navigateByUrl(this.fileInfo.href);
        return;
    }

    var url= this.fileInfo.href;
    window.open((this.baseUrl||'') + url, 'Download');

  }

  getFileUrl(): string {
    return (this.baseUrl||'') +  this.fileInfo.href;
  }

  onFocus(){
    this.focus = true;
  }

  onFocusOut(){
    this.focus = false;
  }

  errorLoapPreview(){

    this.preview.nativeElement.src="favicon.ico";
  }
}
