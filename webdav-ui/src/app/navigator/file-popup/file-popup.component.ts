import {Component, ElementRef, HostBinding, HostListener, Input, OnInit, ViewChild} from "@angular/core";
import {FileInfo} from "@webdav-client/webdav.model";
import {Router} from "@angular/router";

// noinspection AngularMissingOrInvalidDeclarationInModule
@Component({
  selector: 'file-popup-component',
  templateUrl: './file-popup.component.html',
  styleUrl: './file-popup.component.less',
  standalone: true
})
export class FilePopupComponent implements OnInit {
  title = 'webdav';
  public _event!: PointerEvent;


  @Input()
  fileInfo!: FileInfo;

  @Input()
  baseUrl: string | undefined;

  @ViewChild('preview', { read: ElementRef, static: true })
  preview!: ElementRef;

  constructor(private route: Router) {
      console.log("FilePopupComponent  create")
  }

  ngOnInit(): void {

  }
  public set event(e: PointerEvent){
    this.clientX = e.clientX;
    this.clientY = e.clientY;
  }

  @HostBinding('style.left.px')
  clientX?: number;

  @HostBinding('style.top.px')
  clientY?: number;

  @HostListener('click', ['$event'])
  click(event: Event){
    event.preventDefault();
    if (this.fileInfo.isFolder) {
        this.route.navigateByUrl(this.fileInfo.href);
        return;
    }

    var url= this.fileInfo.href;
    window.open((this.baseUrl||'') + url, 'Download');

  }

}
