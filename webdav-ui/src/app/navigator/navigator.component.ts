import {Component, ElementRef, HostListener, Input, OnDestroy, ViewChild} from '@angular/core';

import {
  NavigationEnd,
  Router,
} from '@angular/router';
import {WebDavClient} from "../core/webdav-client/webdav-client";
import {FileInfo} from "../core/webdav-client/webdav.model";
import {Subscription} from "rxjs";


@Component({
  selector: 'navigator',
  templateUrl: './navigator.component.html',
  styleUrl: './navigator.component.less'
})
export class NavigatorComponent implements OnDestroy {
  title = 'webdav';
  baseUrl: string = "";

  routeSubscription: Subscription;
  @ViewChild('filesPanel', {read: ElementRef, static: true})
  filesPanel!: ElementRef<any> ;

  path: string = '/';

  hideHiddenFile = true;

  @Input()
  current!: FileInfo;

  constructor(private route: Router, private webDav: WebDavClient, private router: Router) {
    this.baseUrl = webDav.baseUrl;
    this.hideHiddenFile = (localStorage.getItem("hideHiddenFile") == "true");
    this.routeSubscription = this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.path = event.url;
        this.propfind()
      }
    });
  }

  files: Array<FileInfo> = [];
  viewsFiles: Array<FileInfo> = [];

  @HostListener("contextmenu", ['$event'])
  click(e: Event){
    e.preventDefault();
  }

  propfind(){

    this.webDav.propfind(this.path, '1').subscribe(res=> {
      this.files = res.filter(it => {
        if (it.current){
          this.current = it;
          return false;
        }
        return true;
      });
      this.resetViewsFiles();
    });
  }
  get(){
    this.webDav.get("test/").subscribe();
  }

  resetViewsFiles(){
    this.viewsFiles = this.files.filter(it => {
      if (this.hideHiddenFile && it.displayName.startsWith(".")) {
        return false;
      }
      return true;
    });
  }

  ngOnDestroy(): void{
    this.routeSubscription?.unsubscribe();
  }

  @HostListener('document:keydown.backspace', ['$event'])
  onBackspace(event: Event) {
    this.toUp();
  }

  @HostListener('wheel', ['$event'])
  onMouseWheel(event: WheelEvent) {
    if (event.deltaY == 0) {
      return;
    }
    const element =  (this.filesPanel.nativeElement as HTMLElement);
    element.scrollTo( element.scrollLeft + event.deltaY, 0)

  }


  @HostListener('window:keydown.control.h', ['$event'])
  bigFont(event: KeyboardEvent) {
    event.preventDefault();
    this.hideHiddenFile = !this.hideHiddenFile;
    localStorage.setItem("hideHiddenFile", String(this.hideHiddenFile));
    this.resetViewsFiles();
  }

  @HostListener('selectionchange', ['$event'])
  onMouseSelected(event: WheelEvent) {
    console.log("", document.getSelection());
  }

  toUp() {
    if (!this.current.isFolder){
      return;
    }
    if (this.current.href == '/') {
      return;
    }

    this.route.navigateByUrl(this.current.href.replaceAll(/\/[^/]*\/?$/g, ''))

  }
}
