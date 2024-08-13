import {
  ComponentRef,
  Directive,
  ElementRef, EmbeddedViewRef,
  HostListener,
  Input, OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild,
  ViewContainerRef
} from "@angular/core";
import {FileInfo} from "../../core/webdav-client/webdav.model";
import {Router} from "@angular/router";
import {FilePopupComponent} from "./file-popup.component";

@Directive({
  selector: '[file-popup]',
})
export class FilePopupDirective implements OnInit, OnDestroy {

  @Input()
  fileInfo!: FileInfo;


  constructor(private _viewContainer: ViewContainerRef,
          //    private templateRef: TemplateRef<any>
  ) {

  }


  ngOnInit(): void {
   // const viewRef = this._viewContainer.createEmbeddedView(this.templateRef);
   // (viewRef.rootNodes[0] as HTMLElement).addEventListener("contextmenu", (e) => this.click(e as PointerEvent) )

  }

  cmpRef?: ComponentRef<FilePopupComponent>;
  @Input('file-popup') filePopup!: undefined;


  @HostListener('document:click')
  @HostListener("document:contextmenu", ['$event'])
  @HostListener("document:keydown.escape", ['$event'])
  close() {
    if (this.cmpRef) {
      this.cmpRef.destroy();
      this.cmpRef = undefined;
      return;
    }
  }

  @HostListener("contextmenu", ['$event'])
  async click(event: PointerEvent)  {
    event.preventDefault();
    if (this.cmpRef) {
      this.cmpRef.destroy();
      this.cmpRef = undefined;
      return;
    }
    // // @ts-ignore
    const { FilePopupComponent } = await import(`./file-popup.component`);

    this.cmpRef = this._viewContainer.createComponent(FilePopupComponent);
    this.cmpRef.instance.event = event;
    // event.clientX

  }

  ngOnDestroy(): void {
    if (this.cmpRef) {
      this.cmpRef.destroy();
      this.cmpRef = undefined;
    }
  }


}
